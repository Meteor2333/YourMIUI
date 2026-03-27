package cc.meteormc.yourmiui.app

import android.util.Log
import cc.meteormc.yourmiui.YourMIUI
import de.robv.android.xposed.callbacks.XC_LoadPackage

abstract class App(
    vararg val packages: String
) {
    open fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!packages.contains(lpparam.packageName)) {
            return
        }

        val appName = this.javaClass.simpleName
        YourMIUI.log("Initializing app '$appName'")
        this.getHooks().forEach {
            val hookName = it.javaClass.simpleName
            try {
                YourMIUI.log("Initializing hook '$hookName' in app '$appName'")
                it.init(lpparam)
            } catch (t: Throwable) {
                val stackTrace = Log.getStackTraceString(t)
                YourMIUI.log("Failed to initialize hook '$hookName' in app '$appName':\n$stackTrace")
            }
        }
        YourMIUI.log("Initialized app '$appName'")
    }

    abstract fun getHooks(): Iterable<Hook>
}
