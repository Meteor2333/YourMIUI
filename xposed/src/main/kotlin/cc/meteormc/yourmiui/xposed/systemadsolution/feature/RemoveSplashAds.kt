package cc.meteormc.yourmiui.xposed.systemadsolution.feature

import android.os.Binder
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature

object RemoveSplashAds : XposedFeature(
    key = "remove_splash_ads",
    nameRes = R.string.feature_systemadsolution_remove_splash_ads_name,
    descriptionRes = R.string.feature_systemadsolution_remove_splash_ads_description,
    testEnvironmentRes = R.string.feature_systemadsolution_remove_splash_ads_test_environment
) {
    private val emptyBinder = Binder()

    override fun onLoadPackage() {
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