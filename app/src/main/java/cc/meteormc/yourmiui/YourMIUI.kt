package cc.meteormc.yourmiui

import cc.meteormc.yourmiui.annotation.DontObfuscate
import cc.meteormc.yourmiui.app.android.Android
import cc.meteormc.yourmiui.app.market.Market
import cc.meteormc.yourmiui.app.packageinstaller.PackageInstaller
import cc.meteormc.yourmiui.app.securitycenter.SecurityCenter
import cc.meteormc.yourmiui.app.settings.Settings
import cc.meteormc.yourmiui.app.superwallpaper.SuperWallpaper
import cc.meteormc.yourmiui.helper.BridgeHelper
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

@DontObfuscate
class YourMIUI: IXposedHookLoadPackage {
    companion object {
        private val apps = listOf(
            Android,
            Market,
            PackageInstaller,
            SecurityCenter,
            Settings,
            SuperWallpaper
        ).flatMap { app ->
            app.packages.map { it to app }
        }.toMap()

        fun log(message: String, debug: Boolean = false) {
            if (!debug) XposedBridge.log("[YourMIUI] $message")
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        if (packageName == BuildConfig.APPLICATION_ID) {
            log("Module is active with version ${BuildConfig.VERSION_NAME}(code: ${BuildConfig.VERSION_CODE})!", true)

            ReflectHelper.of(BridgeHelper.Companion::class.java.name, lpparam.classLoader)?.operate {
                method("getApiName")?.hook(XC_MethodReplacement.returnConstant(
                    ReflectHelper.fromJava(XposedBridge::class.java).operate {
                        field("TAG")?.get(null) as String?
                    }
                ))
                method("getApiVersion")?.hook(XC_MethodReplacement.returnConstant(XposedBridge.getXposedVersion()))
                method("isModuleActive")?.hook(XC_MethodReplacement.returnConstant(true))
            }
        } else {
            apps[packageName]?.init(lpparam)
        }
    }
}
