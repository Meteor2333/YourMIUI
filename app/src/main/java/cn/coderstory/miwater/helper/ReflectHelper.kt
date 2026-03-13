@file:Suppress("unused")

package cn.coderstory.miwater.helper

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass

class ReflectHelper<T : Any>(private val delegate: Class<T>) {
    companion object {
        fun <T : Any> fromJava(clazz: Class<T>) = ReflectHelper(clazz)

        fun <T : Any> fromKt(clazz: KClass<T>) = ReflectHelper(clazz.java)

        fun of(className: String, classLoader: ClassLoader?): ReflectHelper<Any>? {
            return try {
                @Suppress("UNCHECKED_CAST")
                ReflectHelper(XposedHelpers.findClass(className, classLoader ?: ClassLoader.getSystemClassLoader()) as Class<Any>)
            } catch (_: XposedHelpers.ClassNotFoundError) {
                null
            }
        }
    }

    fun getDelegate(): Class<T> {
        return delegate
    }

    fun <R> operate(block: ReflectScope<T>.() -> R): R {
        return ReflectScope(delegate).run(block)
    }
}

class ReflectScope<T : Any>(private val clazz: Class<T>) {
    fun constructor(vararg paramTypes: Class<*>): ConstructorOps<T>? {
        @Suppress("UNCHECKED_CAST")
        return (XposedHelpers.findConstructorExactIfExists(clazz, *paramTypes) as Constructor<T>?)?.let { ConstructorOps(it) }
    }

    fun constructors(): List<ConstructorOps<*>> {
        return clazz.constructors.map { ConstructorOps(it) }
    }

    fun declaredConstructors(): List<ConstructorOps<T>> {
        @Suppress("UNCHECKED_CAST")
        return (clazz.declaredConstructors as Array<Constructor<T>>).map { ConstructorOps(it) }
    }

    fun field(name: String): FieldOps<T>? {
        return XposedHelpers.findFieldIfExists(clazz, name)?.let { FieldOps(it) }
    }

    fun field(type: Class<*>): List<FieldOps<T>> {
        val result = mutableListOf<FieldOps<T>>()
        var superClass: Class<*> = clazz
        do {
            for (field in superClass.declaredFields) {
                if (type.isAssignableFrom(field.type)) {
                    field.isAccessible = true
                    result.add(FieldOps(field))
                }
            }
        } while ((superClass.getSuperclass().also { superClass = it }) != null)
        return result
    }

    fun fields(): List<FieldOps<T>> {
        return clazz.fields.map { FieldOps(it) }
    }

    fun declaredFields(): List<FieldOps<T>> {
        return clazz.declaredFields.map { FieldOps(it) }
    }

    fun method(name: String, vararg paramTypes: Class<*>): MethodOps<T>? {
        return try {
            MethodOps(XposedHelpers.findMethodBestMatch(clazz, name, *paramTypes))
        } catch (_: NoSuchMethodError) {
            null
        }
    }

    fun methods(): List<MethodOps<T>> {
        return clazz.methods.map { MethodOps(it) }
    }

    fun declaredMethods(): List<MethodOps<T>> {
        return clazz.declaredMethods.map { MethodOps(it) }
    }
}

class ConstructorOps<T : Any>(private val ctor: Constructor<T>) {
    fun new(vararg args: Any?): T {
        return ctor.apply { isAccessible = true }.newInstance(*args)
    }

    fun hook(callback: XC_MethodHook): ConstructorOps<T> {
        XposedBridge.hookMethod(ctor, callback)
        return this
    }
}

class FieldOps<T : Any?>(private val field: Field) {
    operator fun get(obj: T?): Any? {
        return field.apply { isAccessible = true }[obj]
    }

    operator fun set(obj: T?, value: Any?): FieldOps<T> {
        field.apply { isAccessible = true }[obj] = value
        return this
    }
}

class MethodOps<T : Any?>(private val method: Method) {
    fun call(obj: T, vararg args: Any?): Any? {
        return method.apply { isAccessible = true }.invoke(obj, *args)
    }

    fun hook(callback: XC_MethodHook): MethodOps<T> {
        XposedBridge.hookMethod(method, callback)
        return this
    }
}