@file:Suppress("unused", "UNCHECKED_CAST")

package cc.meteormc.yourmiui.xposed

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

fun XC_MethodHook.MethodHookParam.invokeSuper(): Any? {
    return XposedBridge.invokeOriginalMethod(method, thisObject, args)
}

fun <T> XC_MethodHook.MethodHookParam.findArg(type: Class<T>) = findArgsByClass(type).firstOrNull()

fun XC_MethodHook.MethodHookParam.findBooleanArg() = findArgsByGenerics<Boolean>().firstOrNull()

fun XC_MethodHook.MethodHookParam.findByteArg() = findArgsByGenerics<Byte>().firstOrNull()

fun XC_MethodHook.MethodHookParam.findCharArg() = findArgsByGenerics<Char>().firstOrNull()

fun XC_MethodHook.MethodHookParam.findDoubleArg() = findArgsByGenerics<Double>().firstOrNull()

fun XC_MethodHook.MethodHookParam.findFloatArg() = findArgsByGenerics<Float>().firstOrNull()

fun XC_MethodHook.MethodHookParam.findIntArg() = findArgsByGenerics<Int>().firstOrNull()

fun XC_MethodHook.MethodHookParam.findLongArg() = findArgsByGenerics<Long>().firstOrNull()

fun XC_MethodHook.MethodHookParam.findShortArg() = findArgsByGenerics<Short>().firstOrNull()

fun <T> XC_MethodHook.MethodHookParam.findArgsByClass(type: Class<T>) = this.args.filterIsInstance(type)

inline fun <reified T> XC_MethodHook.MethodHookParam.findArgsByGenerics() = this.args.filterIsInstance<T>()

fun <T> XC_MethodHook.MethodHookParam.getThisObject(type: Class<T>) = this.thisObject as T

fun <T> XC_MethodHook.MethodHookParam.getResult(type: Class<T>) = this.result as T?

fun XC_MethodHook.MethodHookParam.getBooleanResult() = this.result as Boolean

fun XC_MethodHook.MethodHookParam.getByteResult() = this.result as Byte

fun XC_MethodHook.MethodHookParam.getCharResult() = this.result as Char

fun XC_MethodHook.MethodHookParam.getDoubleResult() = this.result as Double

fun XC_MethodHook.MethodHookParam.getFloatResult() = this.result as Float

fun XC_MethodHook.MethodHookParam.getIntResult() = this.result as Int

fun XC_MethodHook.MethodHookParam.getLongResult() = this.result as Long

fun XC_MethodHook.MethodHookParam.getShortResult() = this.result as Short

fun XC_MethodHook.MethodHookParam.getStringResult() = this.result as String