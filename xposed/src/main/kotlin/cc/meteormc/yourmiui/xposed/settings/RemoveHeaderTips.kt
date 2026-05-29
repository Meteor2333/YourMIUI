package cc.meteormc.yourmiui.xposed.settings

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.SETTINGS,
    "@string/feature_settings_remove_header_tips_name",
    "@string/feature_settings_remove_header_tips_description"
)
@RequiredScope("com.android.settings")
object RemoveHeaderTips : Feature(
    "remove_header_tips",
    nameRes = R.string.feature_settings_remove_header_tips_name,
    descriptionRes = R.string.feature_settings_remove_header_tips_description,
    testEnvironmentRes = R.string.feature_settings_remove_header_tips_test_environment
), FeatureHooker {
    override fun hook(packageName: String) {
        operator("com.android.settings.SettingsFragment") {
            // modifier: private | signature: updateTips(ZLjava/lang/String;IIILandroid/view/View$OnClickListener;)V
            method("updateTips")?.hookDoNothing()
        }
    }
}