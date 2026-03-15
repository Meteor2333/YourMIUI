package cn.coderstory.miwater

import androidx.annotation.Keep
import cn.coderstory.miwater.app.App
import cn.coderstory.miwater.helper.ReflectHelper
import cn.coderstory.miwater.helper.XposedHelper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.callbacks.XC_LoadPackage

@Keep
class MiWater: IXposedHookLoadPackage {
    companion object {
        private const val APP_VERSION = BuildConfig.VERSION_NAME
        private const val APP_PACKAGE = BuildConfig.APPLICATION_ID
    }

    private val apps = listOf<App>(
        // ...
    ).flatMap { app -> app.getPackages().map { it to app } }.toMap()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        if (packageName == APP_PACKAGE) {
            ReflectHelper.of(XposedHelper.Companion::class.java.name, lpparam.classLoader)?.operate {
                method("isXposedActive")?.hook(XC_MethodReplacement.returnConstant(true))
            }
        } else {
            apps[packageName]?.init(lpparam)
        }
    }
}
