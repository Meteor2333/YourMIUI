@file:Suppress("unused")

package cc.meteormc.yourmiui.xposed

import cc.meteormc.yourmiui.api.data.HookParam
import cc.meteormc.yourmiui.api.util.ClassUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method

fun <T : Any> operator(clazz: Class<T>): Reflect<T> {
    return Reflect(clazz)
}

fun operator(className: String): Reflect<Any>? {
    val clazz = ClassUtil.getClass(XposedEntry.INSTANCE.classLoader, className, false)
    return if (clazz != null) {
        @Suppress("UNCHECKED_CAST")
        Reflect(clazz as Class<Any>)
    } else {
        XposedBridge.log("[YourMIUI] Class not found: $className!")
        null
    }
}

fun <T : Any, R> operator(clazz: Class<T>, operator: Reflect<T>.() -> R): R {
    return operator(clazz).run(operator)
}

fun <R> operator(className: String, operator: Reflect<Any>.() -> R): R? {
    return operator(className)?.run(operator)
}

@Suppress("UNCHECKED_CAST")
class Reflect<T : Any>(val delegate: Class<T>) {
    companion object {
        private val constructorCache = mutableMapOf<String, Constructor<*>>()
        private val fieldCache = mutableMapOf<String, Field>()
        private val methodCache = mutableMapOf<String, Method>()
    }

    fun constructor(vararg paramTypes: Class<*>): Constructor<T>? {
        val fullName = "${delegate.getName()}(${getParametersString(*paramTypes)})"
        if (constructorCache.containsKey(fullName)) {
            return constructorCache[fullName] as? Constructor<T>
        }

        return runCatching {
            val constructor = delegate.getDeclaredConstructor(*paramTypes).setAccessible()
            constructorCache[fullName] = constructor
            constructor
        }.onFailure {
            XposedBridge.log("[YourMIUI] Constructor not found: $fullName!")
        }.getOrNull()
    }

    fun constructors(): List<Constructor<*>> {
        return delegate.constructors.toAccessibleList()
    }

    fun declaredConstructors(): List<Constructor<T>> {
        return delegate.declaredConstructors.toAccessibleList() as? List<Constructor<T>> ?: emptyList()
    }

    fun field(name: String): Field? {
        val fullName = "${delegate.getName()}#$name"
        if (fieldCache.containsKey(fullName)) {
            return fieldCache[fullName]
        }

        val field = findRecursive {
            runCatching { it.getDeclaredField(name).setAccessible() }.getOrNull()
        }
        return if (field != null) {
            fieldCache[fullName] = field
            field
        } else {
            XposedBridge.log("[YourMIUI] Field not found: $fullName!")
            null
        }
    }

    fun fields(type: Class<*>): List<Field> {
        val result = mutableListOf<Field>()
        var superClass: Class<*> = delegate
        do {
            for (field in superClass.declaredFields) {
                if (!type.isAssignableFrom(field.type)) continue
                result.add(field.setAccessible())
            }
        } while ((superClass.getSuperclass()?.also { superClass = it }) != null)
        return result
    }

    fun fields(): List<Field> {
        return delegate.fields.toAccessibleList()
    }

    fun declaredFields(): List<Field> {
        return delegate.declaredFields.toAccessibleList()
    }

    fun method(name: String, vararg paramTypes: Class<*>): Method? {
        val fullName = "${delegate.getName()}#$name(${getParametersString(*paramTypes)})"
        if (methodCache.containsKey(fullName)) {
            return methodCache[fullName]
        }

        var result: Method? = null
        findRecursive {
            runCatching {
                it.getDeclaredMethod(name, *paramTypes)
            }.getOrNull()?.let { dm ->
                return@findRecursive dm
            }
            for (method in it.getDeclaredMethods()) {
                // compare name and parameters
                if (method.name == name && (result == null || ClassUtil.compareParameterTypes(
                        method.parameterTypes,
                        result!!.parameterTypes,
                        paramTypes
                    ) < 0)
                ) {
                    result = method
                }
            }
            return@findRecursive null
        }?.let { result = it.setAccessible() }

        return if (result != null) {
            methodCache[fullName] = result
            result
        } else {
            XposedBridge.log("[YourMIUI] Method not found: $fullName!")
            null
        }
    }

    fun methods(): List<Method> {
        return delegate.methods.toAccessibleList()
    }

    fun declaredMethods(): List<Method> {
        return delegate.declaredMethods.toAccessibleList()
    }

    private fun <T : AccessibleObject> T.setAccessible(): T {
        isAccessible = true
        return this
    }

    private fun <T : AccessibleObject> Array<T>.toAccessibleList(): List<T> {
        return this.map { it.setAccessible() }
    }

    private fun getParametersString(vararg clazzes: Class<*>): String {
        return clazzes.joinToString(",") { it.name }
    }

    private fun <R> findRecursive(func: (clazz: Class<*>) -> R?): R? {
        var superClass: Class<*> = delegate
        do {
            func(superClass)?.let { return it }
        } while ((superClass.getSuperclass()?.also { superClass = it }) != null)
        return null
    }
}

fun <T : Member> T.hookResult(result: Any?): T {
    XposedBridge.hookMethod(this, XC_MethodReplacement.returnConstant(result))
    return this
}

fun <T : Member> T.hookDoNothing(): T {
    XposedBridge.hookMethod(this, XC_MethodReplacement.DO_NOTHING)
    return this
}

fun <T : Member> T.hookDoNothing(condition: (param: HookParam) -> Boolean): T {
    this.hookBefore {
        if (condition(it)) it.result = null
    }
    return this
}

fun <T : Member> T.overrideResult(block: (param: HookParam) -> Any?): T {
    this.hookBefore {
        val result = block(it)
        if (result != Unit) it.result = result
    }
    return this
}

fun <T : Member> T.replaceResult(block: (param: HookParam) -> Any?): T {
    this.hookAfter {
        val result = block(it)
        if (result != Unit) it.result = result
    }
    return this
}

fun <T : Member> T.hookBefore(callback: (param: HookParam) -> Unit): T {
    XposedBridge.hookMethod(
        this,
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val internal = param.toInternal()
                callback(internal)
                internal.toExternal(param)
            }
        }
    )
    return this
}

fun <T : Member> T.hookAfter(callback: (param: HookParam) -> Unit): T {
    XposedBridge.hookMethod(
        this,
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val internal = param.toInternal()
                callback(internal)
                internal.toExternal(param)
            }
        }
    )
    return this
}

private fun MethodHookParam.toInternal(): HookParam {
    @Suppress("UNCHECKED_CAST")
    return HookParam(
        this.method,
        this.thisObject,
        this.args.copyOf(),
        this.result,
        this.throwable
    )
}

private fun HookParam.toExternal(param: MethodHookParam) {
    param.args = this.args.copyOf()
    if (this.resultChanged) param.result = this.result
    if (this.throwableChanged) param.throwable = this.throwable
}

fun <T> Constructor<T>.new(vararg args: Any?): T {
    return this.apply { isAccessible = true }.newInstance(*args)
}

operator fun <R : Any> Field.get(obj: Any?): R? {
    @Suppress("UNCHECKED_CAST")
    return this.apply { isAccessible = true }[obj] as? R?
}

operator fun Field.set(obj: Any?, value: Any?): Field {
    this.apply { isAccessible = true }[obj] = value
    return this
}

fun Method.call(obj: Any?, vararg args: Any?): Any? {
    return this.apply { isAccessible = true }.invoke(obj, *args)
}