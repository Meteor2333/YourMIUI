package cn.coderstory.miwater.app

import android.util.Log
import cn.coderstory.miwater.MiWater
import de.robv.android.xposed.callbacks.XC_LoadPackage

abstract class App(
    val name: String,
    vararg val packages: String
) {
    private var enabled = false

    fun isEnabled(): Boolean = enabled

    open fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!packages.contains(lpparam.packageName)) {
            return
        }

        this.getHooks().forEach {
            try {
                it.init(lpparam)
            } catch (t: Throwable) {
                MiWater.log("Failed to initialize hook '${it.name}' in app '$name':\n${Log.getStackTraceString(t)}")
            }
        }
        enabled = true
    }

    abstract fun getHooks(): Iterable<Hook>
}
