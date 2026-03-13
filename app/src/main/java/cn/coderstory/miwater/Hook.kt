package cn.coderstory.miwater

import de.robv.android.xposed.callbacks.XC_LoadPackage

fun interface Hook {
    fun init(lpparam: XC_LoadPackage.LoadPackageParam)
}