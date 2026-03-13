package cn.coderstory.miwater

import de.robv.android.xposed.callbacks.XC_LoadPackage

abstract class App(private val packageName: String) {
    fun getPackageName(): String {
        return packageName
    }

    open fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != packageName) {
            return
        }

        this.getHooks().forEach { it.init(lpparam) }
    }

    abstract fun getHooks(): Iterable<Hook>
}
