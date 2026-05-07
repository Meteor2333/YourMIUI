package cc.meteormc.yourmiui.ui.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.InsetDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.YourMIUI
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.bridge.Bridge
import cc.meteormc.yourmiui.common.bridge.ResponseCallback
import cc.meteormc.yourmiui.common.data.AppInfo
import cc.meteormc.yourmiui.common.data.RestartMethod
import cc.meteormc.yourmiui.common.util.getObject
import cc.meteormc.yourmiui.databinding.FragmentScopeBinding
import cc.meteormc.yourmiui.ui.adapter.FeatureAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textview.MaterialTextView
import java.util.concurrent.atomic.AtomicInteger


class ScopeFragment : BaseFragment<FragmentScopeBinding>({ inflater, container ->
    FragmentScopeBinding.inflate(inflater, container, false)
}) {
    private val name: String
        get() = arguments?.getString("name") ?: "Unknown Scope"
    private val scope: Scope?
        get() = arguments?.getObject("scope")
    private val apps: List<AppInfo>
        get() = arguments?.getObject("apps") ?: emptyList()

    override fun onCreate(): View {
        val scopeToolbar = binding.scopeToolbar
        scopeToolbar.title = name
        scopeToolbar.setNavigationOnClickListener {
            it.findNavController().navigateUp()
        }

        scopeToolbar.inflateMenu(R.menu.menu_scope)
        scopeToolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.item_restart) {
                onClickRestart()
                return@setOnMenuItemClickListener true
            }

            return@setOnMenuItemClickListener false
        }

        if (scope != null) {
            val featureList = binding.featureList
            featureList.adapter = FeatureAdapter(scope!!.getFeatures())
            featureList.layoutManager = LinearLayoutManager(requireContext())
        }

        return binding.root
    }

    private fun Int.dp() = (this * requireContext().resources.displayMetrics.density).toInt()

    private fun onClickRestart() {
        val context = requireContext()
        val dialog = BottomSheetDialog(context)

        val title = MaterialTextView(context).apply {
            gravity = Gravity.CENTER
            textSize = 18f
            setText(R.string.restart_scope_title)
        }
        val content = MaterialTextView(context).apply {
            gravity = Gravity.CENTER
            textSize = 16f
            text = getString(R.string.restart_scope_content, apps.joinToString("\n") { it.packageName })

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = 16.dp()
            params.bottomMargin = 24.dp()
            layoutParams = params
        }

        val cancel = MaterialButton(context).apply {
            setText(R.string.dialog_cancel)
            setOnClickListener { dialog.dismiss() }
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(
                MaterialColors.getColor(this, android.R.attr.colorPrimary)
            )
            strokeWidth = 1.dp()
            strokeColor = ColorStateList.valueOf(
                ColorUtils.setAlphaComponent(
                    MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface),
                    50
                )
            )
        }
        val spacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(16.dp(), 0)
        }
        val ok = MaterialButton(context).apply {
            setText(R.string.dialog_ok)
            setOnClickListener {
                dialog.dismiss()
                executeRestart()
            }
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val buttons = LinearLayout(context).apply {
            setPadding(12.dp(), 0, 12.dp(), 0)
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        buttons.addView(cancel)
        buttons.addView(spacer)
        buttons.addView(ok)

        val container = LinearLayout(context).apply {
            setPadding(18.dp(), 36.dp(), 18.dp(), 36.dp())
            gravity = Gravity.CENTER_HORIZONTAL
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(title)
        container.addView(content)
        container.addView(buttons)

        dialog.setContentView(container)
        dialog.setOnShowListener {
            val sheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            if (sheet != null) {
                val model = ShapeAppearanceModel.builder(
                    context,
                    0,
                    com.google.android.material.R.style.ShapeAppearanceOverlay_MaterialAlertDialog_Material3
                ).build()
                val background = MaterialShapeDrawable(model)
                background.fillColor = ColorStateList.valueOf(
                    MaterialColors.getColor(
                        sheet,
                        com.google.android.material.R.attr.colorSurfaceContainer
                    )
                )
                sheet.background = InsetDrawable(background, 16.dp())
            }
        }
        dialog.show()
    }

    private fun executeRestart() {
        val restartMethod = scope?.getRestartMethod() ?: return
        when (restartMethod) {
            RestartMethod.DoNothing -> requestRestart()
            RestartMethod.Reboot -> requestReboot()
            is RestartMethod.ViaComponent -> requestRestart {
                restartMethod.components.forEach { component ->
                    val intent = Intent().apply { this.component = component }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }
    }

    private fun requestRestart(callback: (successd: Boolean) -> Unit = { }) {
        val onResponse = object : ResponseCallback<Unit> {
            private var successd = false
            private val count = AtomicInteger(apps.size)

            override fun onSuccess(data: Unit) {
                successd = true
                onCallback()
            }

            override fun onFailure() {
                onCallback()
            }

            private fun onCallback() {
                if (count.decrementAndGet() > 0) return
                requireView().postDelayed({ callback(successd) }, 500)
                Toast.makeText(
                    requireContext(),
                    if (successd) R.string.restart_scope_success else R.string.restart_scope_failure,
                    if (successd) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                ).show()
            }
        }

        apps.forEach {
            YourMIUI.get().moduleBridge.request(
                Bridge.RESTART_SCOPE_CHANNEL,
                it.packageName,
                onResponse
            )
        }
    }

    private fun requestReboot() {
        Toast.makeText(requireContext(), "[Soon]", Toast.LENGTH_SHORT).show()
    }
}