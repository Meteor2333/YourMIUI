package cc.meteormc.yourmiui.app

import de.robv.android.xposed.callbacks.XC_LoadPackage

abstract class Hook(
    val name: Int,
    val description: Int,
    val warning: Int? = null,
    val testEnvironment: Int? = null,
    val originalAuthor: String? = null
) {
    abstract fun init(lpparam: XC_LoadPackage.LoadPackageParam)
}