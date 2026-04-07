package cc.meteormc.yourmiui.xposed.contentextension.feature

import android.content.Intent
import androidx.core.net.toUri
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature

object FixLinkHandling : XposedFeature(
    key = "contentextension_fix_link_handling",
    nameRes = R.string.feature_contentextension_fix_link_handling_name,
    descriptionRes = R.string.feature_contentextension_fix_link_handling_description,
    testEnvironmentRes = R.string.feature_contentextension_fix_link_handling_test_environment
) {
    override fun init() {
        helper("com.miui.contentextension.utils.AppsUtils") {
            // modifier: private static | signature: getIntentWithBrowser(Ljava/lang/String;)Landroid/content/Intent;
            method("getIntentWithBrowser")?.hookAfter {
                val url = it.args[0] as String
                val result = it.result as Intent
                result.data = url.toUri()
                it.result = result
            }
        }
    }
}