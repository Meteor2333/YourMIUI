package cc.meteormc.yourmiui.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.YourMIUI
import cc.meteormc.yourmiui.core.bridge.Bridge
import cc.meteormc.yourmiui.core.bridge.ResponseCallback
import cc.meteormc.yourmiui.databinding.FragmentHomeBinding
import cc.meteormc.yourmiui.helper.SysVersion
import cc.meteormc.yourmiui.service.UpdateChecker
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>({ inflater, container ->
    FragmentHomeBinding.inflate(inflater, container, false)
}) {
    override fun onCreate(): View {
        bindModuleStatus()
        bindModuleUpdate()
        bindDeviceInfo()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun bindModuleStatus() {
        val isActivated = true
        val statusIconRes = if (isActivated) R.drawable.ic_check_24dp else R.drawable.ic_cross_24dp
        val statusTextRes = if (isActivated) R.string.status_active else R.string.status_inactive
        val statusVersionText = getString(R.string.status_version, "${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}")

        binding.statusIcon.setImageResource(statusIconRes)
        binding.statusText.setText(statusTextRes)
        binding.statusVersion.text = statusVersionText

        fun updateApiStatus(name: String?, version: Int?) {
            binding.statusApi.text = buildString {
                if (isActivated) {
                    append("Activated")
                    if (name != null) append(" by $name")
                    if (version != null) append(" (API $version)")
                } else {
                    append("Not activated")
                }
            }
        }

        var apiName: String? = null
        var apiVersion: Int? = null
        YourMIUI.INSTANCE.moduleBridge.request(
            Bridge.API_NAME_CHANNEL,
            BuildConfig.APPLICATION_ID,
            object : ResponseCallback<String> {
                override fun onSuccess(data: String) {
                    apiName = data
                    updateApiStatus(apiName, apiVersion)
                }

                override fun onFailure() {
                    updateApiStatus(apiName, apiVersion)
                }
            }
        )
        YourMIUI.INSTANCE.moduleBridge.request(
            Bridge.API_VERSION_CHANNEL,
            BuildConfig.APPLICATION_ID,
            object : ResponseCallback<Int> {
                override fun onSuccess(data: Int) {
                    apiVersion = data
                    updateApiStatus(apiName, apiVersion)
                }

                override fun onFailure() {
                    updateApiStatus(apiName, apiVersion)
                }
            }
        )
    }

    private fun bindModuleUpdate() {
        lifecycleScope.launch {
            UpdateChecker.fetch(requireContext())
            if (!UpdateChecker.hasUpdate) return@launch

            val card = binding.updateCard
            card.visibility = View.VISIBLE
            card.setOnClickListener {
                UpdateChecker.downloadUrl?.let {
                    val uri = it.toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
        }
    }

    private fun bindDeviceInfo() {
        binding.infoDeviceBrand.text = Build.BRAND
        binding.infoDeviceModel.text = Build.MODEL
        binding.infoSystemCode.text = Build.DEVICE
        binding.infoAndroidVersion.text = Build.VERSION.RELEASE
        binding.infoMiuiVersion.text = SysVersion.getCurrent().code.toString()
        binding.infoCpuAbi.text = Build.SUPPORTED_ABIS.firstOrNull() ?: System.getProperty("os.arch", Build.UNKNOWN)
    }
}