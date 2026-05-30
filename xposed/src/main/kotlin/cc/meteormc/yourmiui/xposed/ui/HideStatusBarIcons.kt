package cc.meteormc.yourmiui.xposed.ui

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.ListOptionRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.xposed.hookBefore
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.UI,
    "@string/feature_ui_hide_status_bar_icons_name",
    "@string/feature_ui_hide_status_bar_icons_description"
)
@RequiredScope("com.android.systemui")
object HideStatusBarIcons : FeatureHooker {
    @ListOptionRegister(
        "@string/option_ui_hide_status_bar_icons_hidden_icons_name",
        "@string/option_ui_hide_status_bar_icons_hidden_icons_summary",
        [
            "privacy_mode",
            "nfc",
            "zen",
            "cast",
            "bluetooth",
            "bluetooth_handsfree_battery",
            "stealth",
            "volume",
            "alarm_clock",
            "vpn",
            "airplane",
            "hotspot",
            "wifi",
        ],
        [
            "option_ui_hide_status_bar_icons_hidden_icons_privacy",
            "option_ui_hide_status_bar_icons_hidden_icons_nfc",
            "option_ui_hide_status_bar_icons_hidden_icons_zen",
            "option_ui_hide_status_bar_icons_hidden_icons_cast",
            "option_ui_hide_status_bar_icons_hidden_icons_bluetooth",
            "option_ui_hide_status_bar_icons_hidden_icons_bluetooth_battery",
            "option_ui_hide_status_bar_icons_hidden_icons_stealth",
            "option_ui_hide_status_bar_icons_hidden_icons_volume",
            "option_ui_hide_status_bar_icons_hidden_icons_alarm_clock",
            "option_ui_hide_status_bar_icons_hidden_icons_vpn",
            "option_ui_hide_status_bar_icons_hidden_icons_airplane",
            "option_ui_hide_status_bar_icons_hidden_icons_hotspot",
            "option_ui_hide_status_bar_icons_hidden_icons_wifi"
        ]
    )
    private lateinit var hiddenIcons: Set<String>

    override fun hook(packageName: String) {
        setOf(
            "StatusBarIconControllerImpl",
            "MiuiDripLeftStatusBarIconControllerImpl"
        ).forEach {
            operator("com.android.systemui.statusbar.phone.$it") {
                // modifier: public | signature: setIconVisibility(Ljava/lang/String;ZI)V
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
}