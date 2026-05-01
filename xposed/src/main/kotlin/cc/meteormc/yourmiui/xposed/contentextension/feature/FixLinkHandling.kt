package cc.meteormc.yourmiui.xposed.contentextension.feature

import android.content.Intent
import androidx.core.net.toUri
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.findArg
import cc.meteormc.yourmiui.xposed.getResult
import cc.meteormc.yourmiui.xposed.operator

object FixLinkHandling : Feature(
    key = "fix_link_handling",
    nameRes = R.string.feature_contentextension_fix_link_handling_name,
    descriptionRes = R.string.feature_contentextension_fix_link_handling_description,
    testEnvironmentRes = R.string.feature_contentextension_fix_link_handling_test_environment
) {
    override fun onLoadPackage() {
        operator("com.miui.contentextension.utils.AppsUtils") {
            // modifier: private static | signature: getIntentWithBrowser(Ljava/lang/String;)Landroid/content/Intent;
            method("getIntentWithBrowser")?.hookAfter {
                it.getResult(Intent::class.java)?.apply {
                    data = it.findArg(String::class.java)?.toUri()
                }
            }
        }
    }
}