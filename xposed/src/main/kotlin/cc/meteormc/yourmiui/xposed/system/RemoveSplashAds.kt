package cc.meteormc.yourmiui.xposed.system

import android.os.Binder
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.SYSTEM,
    "@string/feature_system_remove_splash_ads_name",
    "@string/feature_system_remove_splash_ads_description"
)
@RequiredScope("com.miui.systemAdSolution")
object RemoveSplashAds : FeatureHooker {
    private val emptyBinder = Binder()

    override fun hook(packageName: String) {
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