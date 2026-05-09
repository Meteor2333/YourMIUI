package cc.meteormc.yourmiui.ui.adapter

import android.view.View
import cc.meteormc.yourmiui.databinding.ItemSettingBinding
import cc.meteormc.yourmiui.ui.data.SettingItem
import cc.meteormc.yourmiui.ui.data.SwitchableSettingItem

class SettingItemAdapter(
    items: Array<SettingItem>
) : BaseAdapter<ItemSettingBinding, SettingItem>(
    items,
    { inflater, parent -> ItemSettingBinding.inflate(inflater, parent, false) }
) {
    override fun newHolder(binding: ItemSettingBinding): BaseAdapter<ItemSettingBinding, SettingItem>.BaseViewHolder {
        return object : BaseViewHolder(binding, binding.root) {
            override fun onBind(item: SettingItem) {
                binding.settingIcon.setImageResource(item.iconRes)
                binding.settingTitle.setText(item.titleRes)
                binding.settingSummary.setText(item.summaryRes)

                itemView.setOnClickListener { item.onClick(binding) }
                if (item is SwitchableSettingItem) {
                    itemView.isClickable = false
                    itemView.isFocusable = false

                    val switch = binding.settingSwitch
                    switch.isChecked = item.value
                    switch.visibility = View.VISIBLE
                    switch.setOnCheckedChangeListener { _, isChecked ->
                        item.onSwitch(isChecked)
                    }
                }
            }
        }
    }
}