package cc.meteormc.yourmiui.xposed.settings

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.api.data.HookContext
import cc.meteormc.yourmiui.xposed.hookDoNothing
import cc.meteormc.yourmiui.xposed.reflect

@FeatureRegister(
    Category.SETTINGS,
    "@string/feature_settings_remove_header_tips_name",
    "@string/feature_settings_remove_header_tips_description"
)
@RequiredScope("com.android.settings")
object RemoveHeaderTips : FeatureHooker {
    override fun hook(context: HookContext) {
        context.reflect("com.android.settings.SettingsFragment") {
            // modifier: private | signature: updateTips(ZLjava/lang/String;IIILandroid/view/View$OnClickListener;)V
            method("updateTips")?.hookDoNothing()
        }
    }
}