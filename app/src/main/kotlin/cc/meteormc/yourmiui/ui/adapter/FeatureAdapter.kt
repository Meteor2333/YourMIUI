package cc.meteormc.yourmiui.ui.adapter

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.YourMIUI
import cc.meteormc.yourmiui.api.data.FeatureInfo
import cc.meteormc.yourmiui.common.prefs.SharedPreferences
import cc.meteormc.yourmiui.databinding.ItemFeatureBinding
import cc.meteormc.yourmiui.helper.ResourceParser

class FeatureAdapter(
    features: List<FeatureInfo>
) : BaseAdapter<ItemFeatureBinding, FeatureInfo>(
    features.toTypedArray(),
    { inflater, parent -> ItemFeatureBinding.inflate(inflater, parent, false) }
) {
    override fun newHolder(binding: ItemFeatureBinding): BaseAdapter<ItemFeatureBinding, FeatureInfo>.BaseViewHolder {
        return ViewHolder(binding)
    }

    private inner class ViewHolder(
        binding: ItemFeatureBinding
    ) : BaseAdapter<ItemFeatureBinding, FeatureInfo>.BaseViewHolder(
        binding,
        binding.root
    ) {
        private lateinit var prefs: SharedPreferences.Feature

        override fun onBind(item: FeatureInfo) {
            val context = itemView.context
            binding.featureName.setText(ResourceParser.parseResName(context, item.name))
            binding.featureDescription.setText(ResourceParser.parseResName(context, item.description))
            item.warning?.let {
                val view = binding.featureWarning
                view.visibility = View.VISIBLE

                val context = view.context
                val parsedId = ResourceParser.parseResName(context, it)
                view.text = context.getString(R.string.feature_warning, context.getString(parsedId))
            }
            item.originalAuthor?.let {
                val view = binding.featureOriginalAuthor
                view.visibility = View.VISIBLE
                view.text = view.context.getString(R.string.feature_original_author, it)
            }

            this.prefs = YourMIUI.get().prefs.getFeature(item)

            // 本来想做一个开关时的折叠动画 但能力有限
            // 折腾了两个晚上之后效果也不满意 遂放弃 以后再说
            val list = binding.optionList
            val switch = binding.featureSwitch
            switch.isChecked = prefs.enabled
            switch.setOnCheckedChangeListener { _, isChecked ->
                prefs.enabled = isChecked
                if (isChecked) list.visibility = View.VISIBLE
                else list.visibility = View.GONE
            }

            val options = item.options
            if (options.isEmpty()) return

            list.adapter = OptionAdapter(options, prefs)
            list.layoutManager = LinearLayoutManager(context)
            if (prefs.enabled) list.visibility = View.VISIBLE
        }

        // todo: 未来会做成长按功能弹出菜单 然后能够执行如 重启所需作用域 等功能
//        private fun executeForceStop(feature: FeatureInfo) {
//            val scopes = feature.scopes
//            val context = itemView.context
//
//            fun request(packageName: String) {
//                YourMIUI.get().moduleBridge.request(
//                    Bridge.RESTART_SCOPE_CHANNEL,
//                    packageName,
//                    object : ResponseCallback<Unit> {
//                        private var successd = false
//                        private val count = AtomicInteger(scopes.size)
//
//                        override fun onSuccess(data: Unit) {
//                            successd = true
//                            onCallback()
//                        }
//
//                        override fun onFailure() {
//                            onCallback()
//                        }
//
//                        private fun onCallback() {
//                            if (count.decrementAndGet() > 0) return
//                            Toast.makeText(
//                                context,
//                                if (successd) R.string.restart_scope_success else R.string.restart_scope_failure,
//                                if (successd) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    }
//                )
//            }
//
//            BottomSheet.Builder(context)
//                .setTitle(R.string.restart_scope_title)
//                .setContent(
//                    R.string.restart_scope_content,
//                    scopes.joinToString("\n")
//                )
//                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
//                .setPositiveButton(android.R.string.ok) { dialog, _ ->
//                    dialog.dismiss()
//                    scopes.forEach { request(it) }
//                }
//                .build()
//                .show()
//        }
    }
}