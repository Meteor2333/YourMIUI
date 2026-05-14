package cc.meteormc.yourmiui.xposed.contentextension

import android.content.Intent
import androidx.core.net.toUri
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.operator

object FixLinkHandling : Feature() {
    override fun onLoadPackage() {
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