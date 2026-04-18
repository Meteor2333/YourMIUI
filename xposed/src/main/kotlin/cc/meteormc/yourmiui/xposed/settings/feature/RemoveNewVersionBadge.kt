package cc.meteormc.yourmiui.xposed.settings.feature

import android.content.Context
import android.provider.Settings
import cc.meteormc.yourmiui.core.Option
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedOption

object RemoveNewVersionBadge : XposedFeature(
    key = "settings_remove_new_version_badge",
    nameRes = R.string.feature_settings_remove_new_version_badge_name,
    descriptionRes = R.string.feature_settings_remove_new_version_badge_description,
    testEnvironmentRes = R.string.feature_settings_remove_new_version_badge_test_environment
) {
    private const val PROPERTY_MIUI_NEW_VERSION = "miui_new_version"

    private var propertyModification = true

    override fun onLoadPackage() {
        helper("com.android.settings.device.MiuiAboutPhoneUtils") {
            // modifier: public static | signature: getUpdateInfo(Landroid/content/Context;)Ljava/lang/String;
            method("getUpdateInfo")?.hookBefore {
                if (propertyModification) {
                    val context = it.args[0] as Context
                    Settings.Global.putString(
                        context.contentResolver,
                        PROPERTY_MIUI_NEW_VERSION,
                        null
                    )
                }

                it.result = null
            }
        }
    }

    override fun getOptions(): Iterable<XposedOption<Boolean>> {
        return listOf(
            XposedOption(
                "property_modification",
                R.string.option_settings_remove_new_version_badge_property_modification_name,
                R.string.option_settings_remove_new_version_badge_property_modification_summary,
                Option.Type.SWITCH(),
                false
            ) { propertyModification = it }
        )
    }
}