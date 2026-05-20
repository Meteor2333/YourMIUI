package cc.meteormc.yourmiui.xposed.settings

import android.content.Context
import android.provider.Settings
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

object RemoveNewVersionBadge : Feature() {
    private const val PROPERTY_MIUI_NEW_VERSION = "miui_new_version"

    private var modifyProperty = false

    override fun onLoadPackage(packageName: String) {
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

    override fun getOptions(): List<Option<Boolean>> {
        return listOf(
            Option(
                "modify_property",
                R.string.option_settings_remove_new_version_badge_modify_property_name,
                R.string.option_settings_remove_new_version_badge_modify_property_summary,
                Option.Type.Switch(),
                modifyProperty
            ) { modifyProperty = it }
        )
    }
}