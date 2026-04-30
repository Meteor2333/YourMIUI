package cc.meteormc.yourmiui.ui.fragment

import android.content.ComponentName
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
import cc.meteormc.yourmiui.common.bridge.Bridge
import cc.meteormc.yourmiui.common.bridge.ResponseCallback
import cc.meteormc.yourmiui.databinding.FragmentScopeBinding
import cc.meteormc.yourmiui.ui.adapter.FeatureAdapter
import cc.meteormc.yourmiui.ui.data.AppInfo
import cc.meteormc.yourmiui.ui.data.FeatureNavConfig
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textview.MaterialTextView

class ScopeFragment : BaseFragment<FragmentScopeBinding>({ inflater, container ->
    FragmentScopeBinding.inflate(inflater, container, false)
}) {
    private val name: String
        get() = arguments?.getString("name") ?: "Unknown Scope"
    private val restartable: Boolean
        get() = arguments?.getBoolean("restartable") ?: false
    private val packages: List<AppInfo>
        get() = if (android.os.Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelableArrayList("apps", AppInfo::class.java)
        } else {
            @Suppress("DEPRECATION", "UNCHECKED_CAST")
            arguments?.getParcelableArrayList<AppInfo>("apps") as? List<AppInfo>
        } ?: emptyList()
    private val features: List<FeatureNavConfig>
        get() = if (android.os.Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelableArrayList("features", FeatureNavConfig::class.java)
        } else {
            @Suppress("DEPRECATION", "UNCHECKED_CAST")
            arguments?.getParcelableArrayList<FeatureNavConfig>("features") as? List<FeatureNavConfig>
        } ?: emptyList()

    override fun onCreate(): View {
        val scopeToolbar = binding.scopeToolbar
        scopeToolbar.title = name
        scopeToolbar.setNavigationOnClickListener {
            it.findNavController().navigateUp()
        }

        if (restartable) {
            scopeToolbar.inflateMenu(R.menu.menu_scope)
            scopeToolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.item_restart) {
                    onClickRestart()
                    return@setOnMenuItemClickListener true
                }

                return@setOnMenuItemClickListener false
            }
        }

        val featureList = binding.featureList
        featureList.adapter = FeatureAdapter(features)
        featureList.layoutManager = LinearLayoutManager(requireContext())

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
            text = getString(R.string.restart_scope_content, packages.joinToString("\n") { it.packageName })

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
        fun launchApp(app: AppInfo) {
            if (app.launchIntent == null) return
            requireView().postDelayed({
                val intent = Intent().apply {
                    component = ComponentName(app.packageName, app.launchIntent)
                }
                startActivity(intent)
            }, 1000)
        }

        packages.forEach {
            YourMIUI.get().moduleBridge.request(
                Bridge.RESTART_SCOPE_CHANNEL,
                it.packageName,
                object : ResponseCallback<Unit> {
                    private var failures = 0
                    private var resolved = false

                    override fun onSuccess(data: Unit) {
                        if (resolved) return
                        resolved = true
                        Toast.makeText(
                            requireContext(),
                            R.string.restart_scope_success,
                            Toast.LENGTH_SHORT
                        ).show()
                        launchApp(it)
                    }

                    override fun onFailure() {
                        if (resolved) return
                        if (++failures < packages.size) return
                        resolved = true
                        Toast.makeText(
                            requireContext(),
                            R.string.restart_scope_failure,
                            Toast.LENGTH_LONG
                        ).show()
                        launchApp(it)
                    }
                }
            )
        }
    }
}