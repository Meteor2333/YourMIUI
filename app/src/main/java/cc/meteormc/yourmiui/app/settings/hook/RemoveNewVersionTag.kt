package cc.meteormc.yourmiui.app.settings.hook

import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.callbacks.XC_LoadPackage

object RemoveNewVersionTag: Hook(
    name = R.string.settings_remove_new_version_tag_name,
    description = R.string.settings_remove_new_version_tag_description,
    testEnvironment = R.string.settings_remove_new_version_tag_test_environment
) {
    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        ReflectHelper.of("com.android.settings.device.MiuiAboutPhoneUtils", lpparam.classLoader)?.operate {
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