package cc.meteormc.yourmiui.ui.fragment

import android.content.Intent
import android.view.View
import android.widget.Toast
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
import cc.meteormc.yourmiui.ui.widget.BottomSheet
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
                BottomSheet.Builder(requireContext())
                    .setTitle(R.string.restart_scope_title)
                    .setContent(R.string.restart_scope_content, apps.joinToString("\n") { app -> app.packageName })
                    .setNegativeButton(R.string.dialog_cancel) { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton(R.string.dialog_ok) { dialog, _ ->
                        dialog.dismiss()
                        executeRestart()
                    }
                    .build()
                    .show()
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