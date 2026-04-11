package cc.meteormc.yourmiui.ui.data

import cc.meteormc.yourmiui.databinding.ItemSettingBinding

open class SettingItem(
    val iconRes: Int,
    val titleRes: Int,
    val summaryRes: Int,
    val onClick: (binding: ItemSettingBinding) -> Unit = { }
)
