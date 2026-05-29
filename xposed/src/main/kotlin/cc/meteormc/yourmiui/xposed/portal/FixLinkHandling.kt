package cc.meteormc.yourmiui.xposed.portal

import android.content.Intent
import androidx.core.net.toUri
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.PORTAL,
    "@string/feature_portal_fix_link_handling_name",
    "@string/feature_portal_fix_link_handling_description"
)
@RequiredScope("com.miui.contentextension")
object FixLinkHandling : FeatureHooker {
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