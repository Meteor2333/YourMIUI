package cc.meteormc.yourmiui.ui.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.databinding.ItemSettingGroupBinding
import cc.meteormc.yourmiui.ui.data.SettingGroup

class SettingGroupAdapter(
    groups: Array<SettingGroup>
) : BaseAdapter<ItemSettingGroupBinding, SettingGroup>(
    groups,
    { inflater, parent -> ItemSettingGroupBinding.inflate(inflater, parent, false) }
) {
    override fun newHolder(binding: ItemSettingGroupBinding): BaseAdapter<ItemSettingGroupBinding, SettingGroup>.BaseViewHolder {
        return object : BaseViewHolder(binding, binding.root) {
            override fun onBind(item: SettingGroup) {
                binding.groupName.setText(item.titleRes)
                binding.settingList.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = SettingItemAdapter(Array(item.settings.size) { i -> item.settings[i] })
                }
            }
        }
    }
}