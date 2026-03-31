package cc.meteormc.yourmiui.xposed.settings.feature

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import de.robv.android.xposed.XC_MethodReplacement

object RemoveNewVersionTag : XposedFeature(
    nameRes = R.string.feature_settings_remove_new_version_tag_name,
    descriptionRes = R.string.feature_settings_remove_new_version_tag_description,
    testEnvironmentRes = R.string.feature_settings_remove_new_version_tag_test_environment
) {
    override fun init() {
        helper("com.android.settings.device.MiuiAboutPhoneUtils")?.operate {
            // modifier: public static | signature: getUpdateInfo(Landroid/content/Context;)Ljava/lang/String;
            method("getUpdateInfo")?.hook(
                // 这里可以直接在hook时修改系统全局属性 以做到更彻底的禁用
                // 但是为了避免关闭此功能后仍然没有更新标记 所以暂时弃用此方案
//                object : XC_MethodHook() {
//                    override fun beforeHookedMethod(param: MethodHookParam) {
//                        val context = param.args[0] as Context
//                        Settings.Global.putString(context.contentResolver, "miui_new_version", null)
//                        param.result = null
//                    }
//                }
                XC_MethodReplacement.returnConstant(null)
            )
        }
    }
}