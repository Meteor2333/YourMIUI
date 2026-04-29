package cc.meteormc.yourmiui.ui.fragment

import android.content.Intent
import android.os.Build
import android.view.View
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.YourMIUI
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

    private fun bindModuleStatus() {
        fun View.createFade(block: () -> Unit) {
            alpha = 0f
            block()
            animate()
                .alpha(1f)
                .setDuration(100)
                .start()
        }

        YourMIUI.get().hostDataStore.observe {
            val icon = binding.statusIcon
            icon.createFade {
                icon.setImageResource(
                    if (isActivated) R.drawable.ic_check_24dp
                    else R.drawable.ic_cross_24dp
                )
            }

            val text = binding.statusText
            text.createFade {
                text.setText(
                    if (isActivated) R.string.status_active
                    else R.string.status_inactive
                )
            }

            val version = binding.statusVersion
            version.createFade {
                version.text = getString(
                    R.string.status_version,
                    "${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}"
                )
            }

            val api = binding.statusApi
            api.createFade {
                api.text = if (isActivated) {
                    "Activated by $apiName (API $apiVersion)"
                } else "Not activated"
            }
        }
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