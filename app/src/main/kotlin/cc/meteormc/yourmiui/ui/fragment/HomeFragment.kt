package cc.meteormc.yourmiui.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.databinding.FragmentHomeBinding
import cc.meteormc.yourmiui.databinding.ItemHomeHeaderBinding
import cc.meteormc.yourmiui.helper.UpdateChecker
import cc.meteormc.yourmiui.store.HostStore
import cc.meteormc.yourmiui.ui.adapter.BaseAdapter
import cc.meteormc.yourmiui.ui.adapter.CategoryAdapter
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.tanh

class HomeFragment : BaseFragment<FragmentHomeBinding>({ inflater, container ->
    FragmentHomeBinding.inflate(inflater, container, false)
}) {
    override fun onCreate(): View {
        val pageList = binding.pageList
        pageList.layoutManager = LinearLayoutManager(requireContext())
        HostStore.features.observe(viewLifecycleOwner) {
            pageList.adapter = ConcatAdapter(
                HeaderAdapter(),
                CategoryAdapter(it.keys.toList())
            )
        }
        return binding.root
    }

    private inner class HeaderAdapter : BaseAdapter<ItemHomeHeaderBinding, Unit?>(
        arrayOfNulls(1),
        { inflater, parent -> ItemHomeHeaderBinding.inflate(inflater, parent, false) }
    ) {
        override fun newHolder(binding: ItemHomeHeaderBinding): BaseAdapter<ItemHomeHeaderBinding, Unit?>.BaseViewHolder {
            return object : BaseViewHolder(binding, binding.root) {
                override fun onBind(item: Unit?) {
                    binding.bindModuleStatus()
                    binding.bindModuleUpdate()
                    binding.bindDeviceInfo()
                    binding.bindSearchView()
                }
            }
        }

        private fun ItemHomeHeaderBinding.bindModuleStatus() {
            var anchorX = 0f
            var anchorY = 0f
            @SuppressLint("ClickableViewAccessibility")
            statusCard.setOnTouchListener { view, event ->
                fun curve(v: Float): Float {
                    val sign = sign(v)
                    val abs = abs(v)
                    return sign * tanh(abs * 0.32f)
                }

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        view.parent.requestDisallowInterceptTouchEvent(true)
                        anchorX = event.x
                        anchorY = event.y
                        view.animate()
                            .scaleX(0.98f)
                            .scaleY(0.98f)
                            .setDuration(120)
                            .start()
                    }
                    MotionEvent.ACTION_CANCEL,
                    MotionEvent.ACTION_POINTER_DOWN,
                    MotionEvent.ACTION_UP -> {
                        view.parent.requestDisallowInterceptTouchEvent(false)
                        view.animate()
                            .translationX(0f)
                            .translationY(0f)
                            .rotationX(0f)
                            .rotationY(0f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(240)
                            .start()
                        view.performClick()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = curve((event.x - anchorX) / (view.width / 2f))
                        val dy = curve((event.y - anchorY) / (view.height / 2f))
                        view.translationX = dx * 24f
                        view.translationY = dy * 12f
                        view.rotationY = dx * 12f
                        view.rotationX = -dy * 6f
                    }
                    else -> {
                        return@setOnTouchListener false
                    }
                }

                true
            }

            HostStore.isActivated.observe(viewLifecycleOwner) {
                statusIcon.setImageResource(
                    if (it) R.drawable.ic_check_24dp
                    else R.drawable.ic_cross_24dp
                )

                statusText.setText(
                    if (it) R.string.status_active
                    else R.string.status_inactive
                )

                statusVersion.text = getString(
                    R.string.status_version,
                    "${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}"
                )

                statusApi.text = if (it) {
                    "Activated by ${HostStore.apiName.value} (API ${HostStore.apiVersion.value})"
                } else "Not activated"
            }
        }

        private fun ItemHomeHeaderBinding.bindModuleUpdate() {
            lifecycleScope.launch {
                UpdateChecker.fetch(requireContext())
                if (!UpdateChecker.hasUpdate) return@launch

                updateCard.visibility = View.VISIBLE
                updateCard.setOnClickListener {
                    UpdateChecker.downloadUrl?.let {
                        val uri = it.toUri()
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                    }
                }
            }
        }

        @SuppressLint("SetTextI18n")
        private fun ItemHomeHeaderBinding.bindDeviceInfo() {
//        binding.infoDeviceModel.text = PropertiesUtil.get("ro.product.marketname")
//        binding.infoSystemCode.text = Build.DEVICE
//        binding.infoAndroidVersion.text = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
//        binding.infoMiuiVersion.text = SysVersion.getCurrent().code.toString()
//        binding.infoCpuAbi.text = Build.SUPPORTED_ABIS.firstOrNull() ?: System.getProperty("os.arch", Build.UNKNOWN)
        }

        private fun ItemHomeHeaderBinding.bindSearchView() {

        }
    }
}