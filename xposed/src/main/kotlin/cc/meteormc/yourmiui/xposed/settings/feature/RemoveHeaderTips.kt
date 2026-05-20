package cc.meteormc.yourmiui.xposed.settings.feature

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

object RemoveHeaderTips : Feature(
    "remove_header_tips",
    nameRes = R.string.feature_settings_remove_header_tips_name,
    descriptionRes = R.string.feature_settings_remove_header_tips_description,
    testEnvironmentRes = R.string.feature_settings_remove_header_tips_test_environment
) {
    override fun onLoadPackage() {
        operator("com.android.settings.BaseSettingsController") {
            constructor(Context::class.java, TextView::class.java)?.hookAfter {
                val context = it.argByGenerics<Context>() ?: return@hookAfter
                val preferences = context.getSharedPreferences("${context.packageName}_preferences", 0)
                val log = preferences.all.map { entry -> "${entry.key}=${entry.value}" }.joinToString("\n", "\n")
                XposedBridge.log("RemoveMiCloudHeader: $log")
            }
        }
    }
}