@file:Suppress("unused", "UNCHECKED_CAST")

package cc.meteormc.yourmiui.xposed

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

fun XC_MethodHook.MethodHookParam.invokeSuper(): Any? {
    return XposedBridge.invokeOriginalMethod(method, thisObject, args)
}

fun <T> XC_MethodHook.MethodHookParam.findArg(type: Class<T>): T? {
    return this.findArgs(type).firstOrNull()
}

fun <T> XC_MethodHook.MethodHookParam.findArgs(type: Class<T>): List<T> {
    return this.args.filterIsInstance(type)
}

fun <T> XC_MethodHook.MethodHookParam.getThisObject(type: Class<T>): T = this.thisObject as T

fun <T> XC_MethodHook.MethodHookParam.getResult(type: Class<T>): T? = this.result as T?

fun XC_MethodHook.MethodHookParam.getBooleanResult() = this.result as Boolean

fun XC_MethodHook.MethodHookParam.getByteResult() = this.result as Byte

fun XC_MethodHook.MethodHookParam.getCharResult() = this.result as Char

fun XC_MethodHook.MethodHookParam.getDoubleResult() = this.result as Double

fun XC_MethodHook.MethodHookParam.getFloatResult() = this.result as Float

fun XC_MethodHook.MethodHookParam.getIntResult() = this.result as Int

fun XC_MethodHook.MethodHookParam.getLongResult() = this.result as Long

fun XC_MethodHook.MethodHookParam.getShortResult() = this.result as Short

fun XC_MethodHook.MethodHookParam.getStringResult() = this.result as String