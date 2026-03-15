package cn.coderstory.miwater

import cn.coderstory.miwater.annotation.DontObfuscate
import cn.coderstory.miwater.app.App
import cn.coderstory.miwater.helper.ReflectHelper
import cn.coderstory.miwater.helper.XposedHelper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

@DontObfuscate
class MiWater: IXposedHookLoadPackage {
    companion object {
        private const val APP_VERSION = BuildConfig.VERSION_NAME
        private const val APP_PACKAGE = BuildConfig.APPLICATION_ID

        fun log(message: String) {
            XposedBridge.log("[MiWater] $message")
        }

        fun log(t: Throwable) {
            XposedBridge.log(t)
        }
    }

    private val apps = listOf<App>(
        // ...
    ).flatMap { app -> app.packages.map { it to app } }.toMap()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        if (packageName == APP_PACKAGE) {
            log("Module is active with version $APP_VERSION!")
            ReflectHelper.of(XposedHelper.Companion::class.java.name, lpparam.classLoader)?.operate {
                method("isXposedActive")?.hook(XC_MethodReplacement.returnConstant(true))
            }
        } else {
            apps[packageName]?.init(lpparam)
        }
    }
}
