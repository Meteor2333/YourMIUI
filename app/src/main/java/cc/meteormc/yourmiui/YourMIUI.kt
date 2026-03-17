package cc.meteormc.yourmiui

import cc.meteormc.yourmiui.annotation.DontObfuscate
import cc.meteormc.yourmiui.app.App
import cc.meteormc.yourmiui.helper.ReflectHelper
import cc.meteormc.yourmiui.helper.XposedHelper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

@DontObfuscate
class YourMIUI: IXposedHookLoadPackage {
    companion object {
        private const val APP_VERSION = BuildConfig.VERSION_NAME
        private const val APP_PACKAGE = BuildConfig.APPLICATION_ID

        fun log(message: String) {
            XposedBridge.log("[YourMIUI] $message")
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
