package cc.meteormc.yourmiui.app.packageinstaller.hook

import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

object DisableSafeCheck: Hook(
    name = "禁用应用安全检测",
    description = "禁止安装应用时对应用进行安全检测",
    warning = "开启此功能可能导致安装无法察觉的恶意软件",
    testEnvironment = "5.0.6.2-20221107版本"
) {
    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        ReflectHelper.of("com.miui.packageInstaller.model.CloudParams", lpparam.classLoader)?.operate {
            declaredConstructors().forEach { ctor -> ctor.hook(object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    // name: safeType | type: java.lang.String
                    field("safeType")?.set(param.thisObject, "no_block")
                }
            }) }
        }
    }
}