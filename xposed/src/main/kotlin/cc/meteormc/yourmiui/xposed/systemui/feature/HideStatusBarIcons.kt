package cc.meteormc.yourmiui.xposed.systemui.feature

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

object HideStatusBarIcons : Feature(
    key = "hide_status_bar_icon",
    nameRes = R.string.feature_systemui_hide_status_bar_icon_name,
    descriptionRes = R.string.feature_systemui_hide_status_bar_icon_description,
    testEnvironmentRes = R.string.feature_systemui_hide_status_bar_icon_test_environment
) {
    private lateinit var hiddenIcons: Set<String>

    override fun onLoadPackage() {
        setOf(
            "StatusBarIconControllerImpl",
            "MiuiDripLeftStatusBarIconControllerImpl"
        ).forEach {
            operator("com.android.systemui.statusbar.phone.$it") {
                method(
                    "setIconVisibility",
                    String::class.java,
                    Boolean::class.javaPrimitiveType!!,
                    Int::class.javaPrimitiveType!!
                )?.hookBefore { param ->
                    if (!hiddenIcons.contains(param.stringArg())) {
                        return@hookBefore
                    }

                    param.booleanArg(false)
                }
            }
        }
    }

    override fun getOptions(): List<Option<*>> {
        return listOf(
            Option(
                "hidden_icons",
                R.string.option_systemui_hide_status_bar_icon_hidden_icons_name,
                R.string.option_systemui_hide_status_bar_icon_hidden_icons_summary,
                Option.Type.MultiChoiceList(
                    "privacy_mode" to R.string.option_systemui_hide_status_bar_icon_hidden_icons_privacy_mode,
                    "nfc" to R.string.option_systemui_hide_status_bar_icon_hidden_icons_nfc,
                    "zen" to R.string.option_systemui_hide_status_bar_icon_hidden_icons_zen,
                    "cast" to R.string.option_systemui_hide_status_bar_icon_hidden_icons_cast,
                    "bluetooth" to R.string.option_systemui_hide_status_bar_icon_hidden_icons_bluetooth,
                    "bluetooth_handsfree_battery" to R.string.option_systemui_hide_status_bar_icon_hidden_icons_bluetooth_handsfree_battery,
                    "stealth" to R.string.option_systemui_hide_status_bar_icon_hidden_icons_stealth,
                    "volume" to R.string.option_systemui_hide_status_bar_icon_hidden_icons_volume,
                    "alarm_clock" to R.string.option_systemui_hide_status_bar_icon_hidden_icons_alarm_clock,
                    "vpn" to R.string.option_systemui_hide_status_bar_icon_hidden_icons_vpn,
                    "airplane" to R.string.option_systemui_hide_status_bar_icon_hidden_icons_airplane,
                    "hotspot" to R.string.option_systemui_hide_status_bar_icon_hidden_icons_hotspot,
                    "wifi" to R.string.option_systemui_hide_status_bar_icon_hidden_icons_wifi
                ),
                emptySet()
            ) { hiddenIcons = it }
        )
    }
}