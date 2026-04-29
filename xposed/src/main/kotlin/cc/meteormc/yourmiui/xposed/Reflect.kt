@file:Suppress("unused")

package cc.meteormc.yourmiui.xposed

import cc.meteormc.yourmiui.core.util.compareParameterTypes
import cc.meteormc.yourmiui.core.util.getClass
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method

fun <T : Any> operator(clazz: Class<T>): ReflectOperator<T> {
    return ReflectOperator(clazz)
}

fun operator(classLoader: ClassLoader, className: String): ReflectOperator<Any>? {
    val clazz = getClass(classLoader, className, false)
    return if (clazz != null) {
        @Suppress("UNCHECKED_CAST")
        ReflectOperator(clazz as Class<Any>)
    } else {
        XposedBridge.log("[YourMIUI] Class not found: $className!")
        null
    }
}

fun XposedFeature.operator(className: String): ReflectOperator<Any>? {
    return operator(classLoader, className)
}

fun <T : Any, R> operator(clazz: Class<T>, operator: ReflectOperator<T>.() -> R): R {
    return operator(clazz).run(operator)
}

fun <R> operator(classLoader: ClassLoader, className: String, operator: ReflectOperator<Any>.() -> R): R? {
    return operator(classLoader, className)?.run(operator)
}

fun <R> XposedFeature.operator(className: String, operator: ReflectOperator<Any>.() -> R): R? {
    return operator(className)?.run(operator)
}

@Suppress("UNCHECKED_CAST")
class ReflectOperator<T : Any>(val delegate: Class<T>) {
    companion object {
        private val constructorCache = mutableMapOf<String, ConstructorWrapper<*>>()
        private val fieldCache = mutableMapOf<String, FieldWrapper<*>>()
        private val methodCache = mutableMapOf<String, MethodWrapper<*>>()
    }

    fun constructor(vararg paramTypes: Class<*>): ConstructorWrapper<T>? {
        val fullName = "${delegate.getName()}(${getParametersString(*paramTypes)})"
        if (constructorCache.containsKey(fullName)) {
            return constructorCache[fullName] as? ConstructorWrapper<T>
        }

        return runCatching {
            ConstructorWrapper<T>(delegate.getDeclaredConstructor(*paramTypes)).apply { constructorCache[fullName] = this }
        }.onFailure {
            XposedBridge.log("[YourMIUI] Constructor not found: $fullName!")
        }.getOrNull()
    }

    fun constructors(): List<ConstructorWrapper<*>> {
        return delegate.constructors.map { ConstructorWrapper(it) }
    }

    fun declaredConstructors(): List<ConstructorWrapper<T>> {
        return (delegate.declaredConstructors as? Array<Constructor<T>>)?.map { ConstructorWrapper(it) } ?: emptyList()
    }

    fun field(name: String): FieldWrapper<T>? {
        val fullName = "${delegate.getName()}#$name"
        if (fieldCache.containsKey(fullName)) {
            return fieldCache[fullName] as? FieldWrapper<T>
        }

        val field = findRecursive {
            runCatching { it.getDeclaredField(name) }.getOrNull()
        }
        return if (field != null) {
            FieldWrapper<T>(field).apply { fieldCache[fullName] = this }
        } else {
            XposedBridge.log("[YourMIUI] Field not found: $fullName!")
            null
        }
    }

    fun fields(type: Class<*>): List<FieldWrapper<T>> {
        val result = mutableListOf<FieldWrapper<T>>()
        var superClass: Class<*> = delegate
        do {
            for (field in superClass.declaredFields) {
                if (!type.isAssignableFrom(field.type)) continue
                result.add(FieldWrapper(field))
            }
        } while ((superClass.getSuperclass()?.also { superClass = it }) != null)
        return result
    }

    fun fields(): List<FieldWrapper<T>> {
        return delegate.fields.map { FieldWrapper(it) }
    }

    fun declaredFields(): List<FieldWrapper<T>> {
        return delegate.declaredFields.map { FieldWrapper(it) }
    }

    fun method(name: String, vararg paramTypes: Class<*>): MethodWrapper<T>? {
        val fullName = "${delegate.getName()}#$name(${getParametersString(*paramTypes)})"
        if (methodCache.containsKey(fullName)) {
            return methodCache[fullName] as? MethodWrapper<T>
        }

        var result: Method? = null
        findRecursive {
            runCatching { it.getDeclaredMethod(name, *paramTypes) }.getOrNull()?.let { dm -> return@findRecursive dm }
            for (method in it.getDeclaredMethods()) {
                // compare name and parameters
                if (method.name == name && (result == null || compareParameterTypes(
                        method.parameterTypes,
                        result!!.parameterTypes,
                        paramTypes
                    ) < 0)) {
                    result = method
                }
            }
            return@findRecursive null
        }?.let { result = it }

        return if (result != null) {
            MethodWrapper<T>(result).apply { methodCache[fullName] = this }
        } else {
            XposedBridge.log("[YourMIUI] Method not found: $fullName)!")
            null
        }
    }

    fun methods(): List<MethodWrapper<T>> {
        return delegate.methods.map { MethodWrapper(it) }
    }

    fun declaredMethods(): List<MethodWrapper<T>> {
        return delegate.declaredMethods.map { MethodWrapper(it) }
    }

    private fun getParametersString(vararg clazzes: Class<*>): String {
        return clazzes.joinToString(",") { it.getName() }
    }

    private fun <R> findRecursive(func: (Class<*>) -> R?): R? {
        var superClass: Class<*> = delegate
        do {
            func(superClass)?.let { return it }
        } while ((superClass.getSuperclass()?.also { superClass = it }) != null)
        return null
    }
}

abstract class HookableWrapper(private val member: Member) {
    fun hookResult(result: Any?): HookableWrapper {
        XposedBridge.hookMethod(member, XC_MethodReplacement.returnConstant(result))
        return this
    }

    fun hookDoNothing(): HookableWrapper {
        XposedBridge.hookMethod(member, XC_MethodReplacement.DO_NOTHING)
        return this
    }

    fun hookDoNothing(condition: (param: XC_MethodHook.MethodHookParam) -> Boolean): HookableWrapper {
        this.hookBefore {
            if (condition(it)) it.result = null
        }
        return this
    }

    fun hookBefore(callback: (param: XC_MethodHook.MethodHookParam) -> Unit): HookableWrapper {
        XposedBridge.hookMethod(
            member,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    callback(param)
                }
            }
        )
        return this
    }

    fun overrideResult(block: (param: XC_MethodHook.MethodHookParam) -> Any?): HookableWrapper {
        this.hookBefore {
            val result = block(it)
            if (result !== Unit) it.result = result
        }
        return this
    }

    fun hookAfter(callback: (param: XC_MethodHook.MethodHookParam) -> Unit): HookableWrapper {
        XposedBridge.hookMethod(
            member,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    callback(param)
                }
            }
        )
        return this
    }

    fun replaceResult(block: (param: XC_MethodHook.MethodHookParam) -> Any?): HookableWrapper {
        this.hookAfter {
            val result = block(it)
            if (result !== Unit) it.result = result
        }
        return this
    }
}

class ConstructorWrapper<T : Any>(private val delegate: Constructor<T>) : HookableWrapper(delegate) {
    fun parameterTypes(): Array<Class<*>> = delegate.parameterTypes

    fun new(vararg args: Any?): T {
        return delegate.apply { isAccessible = true }.newInstance(*args)
    }
}

@Suppress("UNCHECKED_CAST")
class FieldWrapper<T : Any>(private val delegate: Field) {
    fun type(): Class<*> = delegate.type

    operator fun <R : Any> get(obj: T?): R? {
        return delegate.apply { isAccessible = true }[obj] as? R?
    }

    operator fun set(obj: T?, value: Any?): FieldWrapper<T> {
        delegate.apply { isAccessible = true }[obj] = value
        return this
    }
}

class MethodWrapper<T : Any>(private val delegate: Method) : HookableWrapper(delegate) {
    fun parameterTypes(): Array<Class<*>> = delegate.parameterTypes

    fun returnType(): Class<*> = delegate.returnType

    fun call(obj: T?, vararg args: Any?): Any? {
        return delegate.apply { isAccessible = true }.invoke(obj, *args)
    }
}