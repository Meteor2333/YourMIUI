package cc.meteormc.yourmiui.xposed.settings

import android.content.Context
import android.provider.Settings
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.api.annotation.SwitchOptionRegister
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.SETTINGS,
    "@string/feature_settings_remove_new_version_badge_name",
    "@string/feature_settings_remove_new_version_badge_description"
)
@RequiredScope("com.android.settings")
object RemoveNewVersionBadge : FeatureHooker {
    private const val PROPERTY_MIUI_NEW_VERSION = "miui_new_version"

    @SwitchOptionRegister(
        "@string/option_settings_remove_new_version_badge_modify_property_name",
        "@string/option_settings_remove_new_version_badge_modify_property_description"
    )
    private var modifyProperty = false

    override fun hook(packageName: String) {
        operator("com.android.settings.device.MiuiAboutPhoneUtils") {
            // modifier: public static | signature: getUpdateInfo(Landroid/content/Context;)Ljava/lang/String;
            method("getUpdateInfo")?.hookBefore {
                if (modifyProperty) {
                    val context = it.argByGenerics<Context>() ?: return@hookBefore
                    Settings.Global.putString(
                        context.contentResolver,
                        PROPERTY_MIUI_NEW_VERSION,
                        null
                    )
                }
            }?.hookDoNothing()
        }
    }
}