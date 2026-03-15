package cn.coderstory.miwater.app

import de.robv.android.xposed.callbacks.XC_LoadPackage

abstract class Hook(
    val name: String,
    val description: String,
    val warning: String? = null,
    val testEnvironment: String? = null,
    val originalAuthor: String? = null
) {
    abstract fun init(lpparam: XC_LoadPackage.LoadPackageParam)
}