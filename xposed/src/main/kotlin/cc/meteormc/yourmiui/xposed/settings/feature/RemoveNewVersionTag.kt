package cc.meteormc.yourmiui.xposed.settings.feature

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature

object RemoveNewVersionTag : XposedFeature(
    key = "settings_remove_new_version_tag",
    nameRes = R.string.feature_settings_remove_new_version_tag_name,
    descriptionRes = R.string.feature_settings_remove_new_version_tag_description,
    testEnvironmentRes = R.string.feature_settings_remove_new_version_tag_test_environment
) {
    override fun init() {
        helper("com.android.settings.device.MiuiAboutPhoneUtils") {
            // 这里可以直接在hook时修改系统全局属性 以做到更彻底的禁用
            // 但是为了避免关闭此功能后仍然没有更新标记(?这不是好事嘛) 所以暂时弃用此方案
//            // modifier: public static | signature: getUpdateInfo(Landroid/content/Context;)Ljava/lang/String;
//            method("getUpdateInfo")?.hookBefore {
//                val context = it.args[0] as Context
//                Settings.Global.putString(context.contentResolver, "miui_new_version", null)
//                it.result = null
//            }

            // modifier: public static | signature: getUpdateInfo(Landroid/content/Context;)Ljava/lang/String;
            method("getUpdateInfo")?.hookResult(null)
        }
    }
}