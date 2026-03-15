package cn.coderstory.miwater.app

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

        try {
            this.getHooks().forEach { it.init(lpparam) }
            enabled = true
        } catch (t: Throwable) {
            MiWater.log(t)
        }
    }

    abstract fun getHooks(): Iterable<Hook>
}
