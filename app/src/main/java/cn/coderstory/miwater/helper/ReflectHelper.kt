@file:Suppress("unused")

package cn.coderstory.miwater.helper

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.Boolean
import java.lang.Byte
import java.lang.Double
import java.lang.Float
import java.lang.Long
import java.lang.Short
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.Any
import kotlin.Array
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.also
import kotlin.apply
import kotlin.arrayOf
import kotlin.let
import kotlin.reflect.KClass
import kotlin.run
import kotlin.to

class ReflectHelper<T : Any>(private val delegate: Class<T>) {
    companion object {
        fun <T : Any> fromJava(clazz: Class<T>) = ReflectHelper(clazz)

        fun <T : Any> fromKt(clazz: KClass<T>) = ReflectHelper(clazz.java)

        fun of(className: String, classLoader: ClassLoader?): ReflectHelper<Any>? {
            return try {
                @Suppress("UNCHECKED_CAST")
                ReflectHelper(XposedHelpers.findClass(className, classLoader ?: ClassLoader.getSystemClassLoader()) as Class<Any>)
            } catch (_: XposedHelpers.ClassNotFoundError) {
                XposedBridge.log("Class not found: $className")
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
        } while ((superClass.getSuperclass()?.also { superClass = it }) != null)
        return result
    }

    fun fields(): List<FieldOps<T>> {
        return clazz.fields.map { FieldOps(it) }
    }

    fun declaredFields(): List<FieldOps<T>> {
        return clazz.declaredFields.map { FieldOps(it) }
    }

    fun method(name: String, vararg paramTypes: Class<*>): MethodOps<T>? {
        val fullMethodName = clazz.getName() + '#' + name + getParametersString(*paramTypes)

        if (methodCache.containsKey(fullMethodName)) {
            @Suppress("UNCHECKED_CAST")
            return (methodCache[fullMethodName] as MethodOps<T>?) ?: throw NoSuchMethodError(fullMethodName)
        }

        try {
            val method = MethodOps<T>(XposedHelpers.findMethodExact(clazz, name, *paramTypes))
            methodCache[fullMethodName] = method
            return method
        } catch (_: NoSuchMethodError) {
        }

        var bestMatch: Method? = null
        var clz: Class<*> = clazz
        var considerPrivateMethods = true
        do {
            try {
                bestMatch = clz.getDeclaredMethod(name, *paramTypes)
                break
            } catch (_: NoSuchMethodException) {
            }

            for (method in clz.getDeclaredMethods()) {
                // don't consider private methods of superclasses
                if (!considerPrivateMethods && Modifier.isPrivate(method.modifiers)) continue

                // compare name and parameters
                if (method.name == name && (bestMatch == null || compareParameterTypes(
                        method.getParameterTypes(),
                        bestMatch.getParameterTypes(),
                        paramTypes) < 0)) {
                    // get accessible version of method
                    bestMatch = method
                }
            }
            considerPrivateMethods = false
        } while ((clz.getSuperclass()?.also { clz = it }) != null)

        return if (bestMatch != null) {
            bestMatch.isAccessible = true
            val method = MethodOps<T>(bestMatch)
            methodCache[fullMethodName] = method
            method
        } else {
            methodCache.remove(fullMethodName)
            null
        }
    }

    private fun getParametersString(vararg clazzes: Class<*>?): String {
        val sb = StringBuilder("(")
        var first = true
        for (clazz in clazzes) {
            if (first) first = false
            else sb.append(",")

            if (clazz != null) sb.append(clazz.getCanonicalName())
            else sb.append("null")
        }
        sb.append(")")
        return sb.toString()
    }

    /* --------- commons-lang start --------- */

    companion object {
        private val methodCache = mutableMapOf<String, MethodOps<*>>()

        /**
         * Maps primitive `Class`es to their corresponding wrapper `Class`.
         */
        private val primitiveWrapperMap = mapOf(
            Boolean.TYPE to Boolean::class.java,
            Byte.TYPE to Byte::class.java,
            Character.TYPE to Character::class.java,
            Short.TYPE to Short::class.java,
            Integer.TYPE to Integer::class.java,
            Long.TYPE to Long::class.java,
            Double.TYPE to Double::class.java,
            Float.TYPE to Float::class.java,
            Void.TYPE to Void::class.java,
        )

        /**
         * Maps wrapper `Class`es to their corresponding primitive types.
         */
        private val wrapperPrimitiveMap = mapOf(
            Boolean::class.java to Boolean.TYPE,
            Byte::class.java to Byte.TYPE,
            Character::class.java to Character.TYPE,
            Short::class.java to Short.TYPE,
            Integer::class.java to Integer.TYPE,
            Long::class.java to Long.TYPE,
            Double::class.java to Double.TYPE,
            Float::class.java to Float.TYPE,
            Void::class.java to Void.TYPE,
        )

        /** Array of primitive number types ordered by "promotability"  */
        private val orderedPrimitiveTypes = arrayOf(
            Byte.TYPE,
            Short.TYPE,
            Character.TYPE,
            Integer.TYPE,
            Long.TYPE,
            Float.TYPE,
            Double.TYPE
        )
    }

    /**
     * Compares the relative fitness of two sets of parameter types in terms of
     * matching a third set of runtime parameter types, such that a list ordered
     * by the results of the comparison would return the best match first
     * (least).
     *
     * @param left the "left" parameter set
     * @param right the "right" parameter set
     * @param actual the runtime parameter types to match against
     * `left`/`right`
     * @return int consistent with `compare` semantics
     */
    fun compareParameterTypes(left: Array<Class<*>>, right: Array<Class<*>>, actual: Array<out Class<*>>): Int {
        val leftCost = getTotalTransformationCost(actual, left)
        val rightCost = getTotalTransformationCost(actual, right)
        return if (leftCost < rightCost) -1 else if (rightCost < leftCost) 1 else 0
    }

    /**
     * Returns the sum of the object transformation cost for each class in the
     * source argument list.
     * @param srcArgs The source arguments
     * @param destArgs The destination arguments
     * @return The total transformation cost
     */
    private fun getTotalTransformationCost(srcArgs: Array<out Class<*>>, destArgs: Array<out Class<*>>): kotlin.Float {
        var totalCost = 0.0f
        for (i in srcArgs.indices) {
            val srcClass: Class<*> = srcArgs[i]
            val destClass: Class<*> = destArgs[i]
            totalCost += getObjectTransformationCost(srcClass, destClass)
        }
        return totalCost
    }

    /**
     * Gets the number of steps required needed to turn the source class into
     * the destination class. This represents the number of steps in the object
     * hierarchy graph.
     * @param srcClass The source class
     * @param destClass The destination class
     * @return The cost of transforming an object
     */
    private fun getObjectTransformationCost(srcClass: Class<*>, destClass: Class<*>): kotlin.Float {
        if (destClass.isPrimitive) {
            return getPrimitivePromotionCost(srcClass, destClass)
        }

        var cost = 0.0f
        var srcClass: Class<*>? = srcClass
        while (srcClass != null && destClass != srcClass) {
            if (destClass.isInterface && isAssignable(srcClass, destClass)) {
                // slight penalty for interface match.
                // we still want an exact match to override an interface match,
                // but
                // an interface match should override anything where we have to
                // get a superclass.
                cost += 0.25f
                break
            }
            cost++
            srcClass = srcClass.getSuperclass()
        }
        /*
         * If the destination class is null, we've travelled all the way up to
         * an Object match. We'll penalize this by adding 1.5 to the cost.
         */
        if (srcClass == null) {
            cost += 1.5f
        }
        return cost
    }

    /**
     *
     * Checks if one `Class` can be assigned to a variable of
     * another `Class`.
     *
     *
     * Unlike the [Class.isAssignableFrom] method,
     * this method takes into account widenings of primitive classes and
     * `null`s.
     *
     *
     * Primitive widenings allow an int to be assigned to a long, float or
     * double. This method returns the correct result for these cases.
     *
     *
     * `Null` may be assigned to any reference type. This method
     * will return `true` if `null` is passed in and the
     * toClass is non-primitive.
     *
     *
     * Specifically, this method tests whether the type represented by the
     * specified `Class` parameter can be converted to the type
     * represented by this `Class` object via an identity conversion
     * widening primitive or widening reference conversion. See
     * *[The Java Language Specification](http://java.sun.com/docs/books/jls/)*,
     * sections 5.1.1, 5.1.2 and 5.1.4 for details.
     *
     * @param cls  the Class to check, may be null
     * @param toClass  the Class to try to assign into, returns false if null
     * @return `true` if assignment possible
     */
    fun isAssignable(cls: Class<*>?, toClass: Class<*>?): kotlin.Boolean {
        var cls = cls
        if (toClass == null) {
            return false
        }
        // have to check for null, as isAssignableFrom doesn't
        if (cls == null) {
            return !toClass.isPrimitive
        }
        //autoboxing:
        if (cls.isPrimitive && !toClass.isPrimitive) {
            cls = primitiveWrapperMap[cls]
            if (cls == null) {
                return false
            }
        }
        if (toClass.isPrimitive && !cls.isPrimitive) {
            cls = wrapperPrimitiveMap[cls]
            if (cls == null) {
                return false
            }
        }
        if (cls == toClass) {
            return true
        }
        if (cls.isPrimitive) {
            if (!toClass.isPrimitive) {
                return false
            }
            when (cls) {
                Integer.TYPE -> {
                    return Long.TYPE == toClass || Float.TYPE == toClass || Double.TYPE == toClass
                }
                Long.TYPE -> {
                    return Float.TYPE == toClass || Double.TYPE == toClass
                }
                Boolean.TYPE -> {
                    return false
                }
                Double.TYPE -> {
                    return false
                }
                Float.TYPE -> {
                    return Double.TYPE == toClass
                }
                Short.TYPE, Character.TYPE -> {
                    return Integer.TYPE == toClass
                            || Long.TYPE == toClass
                            || Float.TYPE == toClass
                            || Double.TYPE == toClass
                }
                Byte.TYPE -> {
                    return Short.TYPE == toClass
                            || Integer.TYPE == toClass
                            || Long.TYPE == toClass
                            || Float.TYPE == toClass
                            || Double.TYPE == toClass
                }
            }
            // should never get here
            return false
        }
        return toClass.isAssignableFrom(cls)
    }


    /**
     * Gets the number of steps required to promote a primitive number to another
     * type.
     * @param srcClass the (primitive) source class
     * @param destClass the (primitive) destination class
     * @return The cost of promoting the primitive
     */
    private fun getPrimitivePromotionCost(srcClass: Class<*>, destClass: Class<*>): kotlin.Float {
        var cost = 0.0f
        var cls = srcClass
        if (!cls.isPrimitive) {
            // slight unwrapping penalty
            cost += 0.1f
            cls = wrapperPrimitiveMap[cls] ?: return 0.0f
        }
        var i = 0
        while (cls != destClass && i < orderedPrimitiveTypes.size) {
            if (cls == orderedPrimitiveTypes[i]) {
                cost += 0.1f
                if (i < orderedPrimitiveTypes.size - 1) {
                    cls = orderedPrimitiveTypes[i + 1]
                }
            }
            i++
        }
        return cost
    }

    /* ---------- commons-lang end ---------- */

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