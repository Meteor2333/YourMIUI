package cc.meteormc.yourmiui.xposed.contentextension.feature

import android.content.Intent
import androidx.core.net.toUri
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import de.robv.android.xposed.XC_MethodHook

object FixLinkOpen : XposedFeature(
    key = "contentextension_fix_link_open",
    nameRes = R.string.feature_contentextension_fix_link_open_name,
    descriptionRes = R.string.feature_contentextension_fix_link_open_description,
    testEnvironmentRes = R.string.feature_contentextension_fix_link_open_test_environment
) {
    override fun init() {
        helper("com.miui.contentextension.utils.AppsUtils")?.operate {
            method("getIntentWithBrowser")?.hook(
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val url = param.args[0] as String
                        val result = param.result as Intent
                        result.data = url.toUri()
                        param.result = result
                    }
                }
            )
        }
    }
}