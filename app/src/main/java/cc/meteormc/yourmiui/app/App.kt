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

        this.getHooks().forEach {
            try {
                it.init(lpparam)
            } catch (t: Throwable) {
                YourMIUI.log("Failed to initialize hook '${it.name}' " +
                        "in app '${this.javaClass.simpleName}':\n${Log.getStackTraceString(t)}")
            }
        }
    }

    abstract fun getHooks(): Iterable<Hook>
}
