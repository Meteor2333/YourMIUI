package cn.coderstory.miwater.app

import de.robv.android.xposed.callbacks.XC_LoadPackage

abstract class App(private vararg val packages: String) {
    fun getPackages() = packages.toList()

    open fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!packages.contains(lpparam.packageName)) {
            return
        }

        try {
            this.getHooks().forEach { it.init(lpparam) }
        } catch (t: Throwable) {
            MiWater.log(t)
        }
    }

    abstract fun getHooks(): Iterable<Hook>
}
