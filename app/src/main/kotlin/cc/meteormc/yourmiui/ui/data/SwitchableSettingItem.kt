package cc.meteormc.yourmiui.ui.data

class SwitchableSettingItem(
    iconRes: Int,
    titleRes: Int,
    summaryRes: Int,
    val value: Boolean,
    val onSwitch: (isChecked: Boolean) -> Unit = { }
) : SettingItem(
    iconRes,
    titleRes,
    summaryRes
)