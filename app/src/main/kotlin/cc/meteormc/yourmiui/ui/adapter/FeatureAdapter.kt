package cc.meteormc.yourmiui.ui.adapter

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.databinding.ItemFeatureBinding
import cc.meteormc.yourmiui.service.FeaturePreference

class FeatureAdapter(features: List<Feature>) : BaseAdapter<ItemFeatureBinding, Feature>(
    features.toTypedArray(),
    { inflater, parent -> ItemFeatureBinding.inflate(inflater, parent, false) }
) {
    override fun newHolder(binding: ItemFeatureBinding): BaseAdapter<ItemFeatureBinding, Feature>.BaseViewHolder {
        return ViewHolder(binding)
    }

    private inner class ViewHolder(
        binding: ItemFeatureBinding
    ) : BaseAdapter<ItemFeatureBinding, Feature>.BaseViewHolder(binding, binding.root) {
        private lateinit var prefs: FeaturePreference

        override fun onBind(item: Feature) {
            this.prefs = FeaturePreference.getPreference(item.key)
            binding.featureName.setText(item.nameRes)
            binding.featureDescription.setText(item.descriptionRes)
            item.warningRes?.let {
                val view = binding.featureWarning
                view.visibility = View.VISIBLE

                val context = view.context
                view.text = context.getString(R.string.feature_warning, context.getString(it))
            }
            item.testEnvironmentRes?.let {
                val view = binding.featureTestEnvironment
                view.visibility = View.VISIBLE

                val context = view.context
                view.text = view.context.getString(R.string.feature_test_environment, context.getString(it))
            }
            item.originalAuthor?.let {
                val view = binding.featureOriginalAuthor
                view.visibility = View.VISIBLE
                view.text = view.context.getString(R.string.feature_original_author, it)
            }

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

            val options = item.getOptions()
            if (options.isEmpty()) return

            val context = binding.root.context
            list.adapter = OptionAdapter(options, prefs)
            list.layoutManager = LinearLayoutManager(context)
            if (prefs.enabled) list.visibility = View.VISIBLE
        }
    }
}