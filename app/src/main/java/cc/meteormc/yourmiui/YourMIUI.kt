package cc.meteormc.yourmiui

import cc.meteormc.yourmiui.annotation.DontObfuscate
import cc.meteormc.yourmiui.app.android.Android
import cc.meteormc.yourmiui.app.market.Market
import cc.meteormc.yourmiui.app.packageinstaller.PackageInstaller
import cc.meteormc.yourmiui.app.securitycenter.SecurityCenter
import cc.meteormc.yourmiui.app.superwallpaper.SuperWallpaper
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

        val apps = listOf(
            Android,
            Market,
            PackageInstaller,
            SecurityCenter,
            SuperWallpaper
        ).flatMap { app -> app.packages.map { it to app } }.toMap()

        fun log(message: String) {
            XposedBridge.log("[YourMIUI] $message")
        }
    }

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
