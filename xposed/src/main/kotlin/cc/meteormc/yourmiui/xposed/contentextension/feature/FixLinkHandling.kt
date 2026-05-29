package cc.meteormc.yourmiui.xposed.contentextension.feature

import android.content.Intent
import androidx.core.net.toUri
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.PORTAL,
    "@string/feature_portal_fix_link_handling_name",
    "@string/feature_portal_fix_link_handling_description"
)
@RequiredScope("com.miui.contentextension")
object FixLinkHandling : Feature(
    key = "fix_link_handling",
    nameRes = R.string.feature_contentextension_fix_link_handling_name,
    descriptionRes = R.string.feature_contentextension_fix_link_handling_description,
    testEnvironmentRes = R.string.feature_contentextension_fix_link_handling_test_environment
), FeatureHooker {
    override fun hook(packageName: String) {
        operator("com.miui.contentextension.utils.AppsUtils") {
            // modifier: private static | signature: getIntentWithBrowser(Ljava/lang/String;)Landroid/content/Intent;
            method("getIntentWithBrowser")?.hookAfter {
                it.result<Intent>()?.apply {
                    data = it.stringArg()?.toUri()
                }
            }
        }
    }
}