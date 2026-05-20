package cc.meteormc.yourmiui.xposed.system

import android.os.Binder
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.operator

object RemoveSplashAds : Feature() {
    private val emptyBinder = Binder()

    override fun onLoadPackage(packageName: String) {
        setOf(
            "com.miui.systemAdSolution.splashAd.SystemSplashAdService",
            "com.miui.systemAdSolution.splashscreen.SplashScreenService",
            "com.miui.systemAdSolution.splashscreen.SplashScreenServiceV2"
        ).forEach {
            operator(it) {
                // modifier: onBind | signature: onBind(Landroid/content/Intent;)Landroid/os/IBinder;
                method("onBind")?.hookResult(emptyBinder)
            }
        }
    }
}