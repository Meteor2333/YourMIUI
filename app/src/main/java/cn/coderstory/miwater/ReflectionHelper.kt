package cn.coderstory.miwater

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

@Suppress("unused")
open class ReflectionHelper {
    companion object {
        fun findClass(
            className: String?,
            classLoader: ClassLoader?
        ): Class<*>? {
            try {
                return XposedHelpers.findClass(className, classLoader)
            } catch (e: Throwable) {
                XposedBridge.log(e)
            }
            return null
        }

        fun findClassWithoutLog(
            className: String?,
            classLoader: ClassLoader?
        ): Class<*>? {
            try {
                return className?.let { Class.forName(it, false, classLoader) }
            } catch (_: Exception) {
                // ignored
            }
            return null
        }

        fun hookConstructor(
            className: String?,
            classLoader: ClassLoader?,
            callback: XC_MethodHook,
            vararg parameterTypes: Class<*>?
        ) {
            try {
                val clazz = findClass(className, classLoader)
                if (parameterTypes.isEmpty()) {
                    XposedBridge.hookAllConstructors(clazz, callback)
                } else {
                    XposedHelpers.findAndHookConstructor(className, classLoader, *parameterTypes, callback)
                }
                XposedBridge.hookAllConstructors(clazz, callback)
            } catch (e: Throwable) {
                XposedBridge.log(e)
            }
        }

        fun hookMethod(
            className: String?,
            classLoader: ClassLoader?,
            callback: XC_MethodHook?,
            methodName: String?,
            vararg parameterTypes: Class<*>?
        ) {
            try {
                val clazz = findClass(className, classLoader)
                if (parameterTypes.isEmpty()) {
                    XposedBridge.hookAllMethods(clazz, methodName, callback)
                } else {
                    XposedHelpers.findAndHookMethod(className, classLoader, methodName, *parameterTypes, callback)
                }
            } catch (e: Throwable) {
                XposedBridge.log(e)
            }
        }
    }
}