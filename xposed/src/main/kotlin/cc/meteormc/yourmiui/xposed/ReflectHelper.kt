@file:Suppress("unused")

package cc.meteormc.yourmiui.xposed

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

class ReflectHelper<T : Any>(val delegate: Class<T>) {
    companion object {
        fun <T : Any> fromJava(clazz: Class<T>) = ReflectHelper(clazz)

        fun <T : Any> fromKt(clazz: KClass<T>) = ReflectHelper(clazz.java)

        fun of(className: String, classLoader: ClassLoader): ReflectHelper<Any>? {
            return try {
                @Suppress("UNCHECKED_CAST")
                ReflectHelper(XposedHelpers.findClass(className, classLoader) as Class<Any>)
            } catch (_: XposedHelpers.ClassNotFoundError) {
                XposedEntry.log("Class not found: $className!")
                null
            }
        }
    }

    fun <R> operate(block: ReflectScope<T>.() -> R): R {
        return ReflectScope(delegate).run(block)
    }
}

class ReflectScope<T : Any>(val delegate: Class<T>) {
    fun constructor(vararg paramTypes: Class<*>): ConstructorOps<T>? {
        @Suppress("UNCHECKED_CAST")
        return try {
            ConstructorOps((XposedHelpers.findConstructorExact(delegate, *paramTypes) as Constructor<T>))
        } catch (_: NoSuchMethodError) {
            XposedEntry.log("Constructor not found: ${delegate.getName()}#(${getParametersString(*paramTypes)})!")
            null
        }
    }

    fun constructors(): List<ConstructorOps<*>> {
        return delegate.constructors.map { ConstructorOps(it) }
    }

    fun declaredConstructors(): List<ConstructorOps<T>> {
        @Suppress("UNCHECKED_CAST")
        return (delegate.declaredConstructors as Array<Constructor<T>>).map { ConstructorOps(it) }
    }

    fun field(name: String): FieldOps<T>? {
        return try {
            FieldOps(XposedHelpers.findField(delegate, name))
        } catch (_: NoSuchFieldError) {
            XposedEntry.log("Field not found: ${delegate.getName()}#$name!")
            null
        }
    }

    fun field(type: Class<*>): List<FieldOps<T>> {
        val result = mutableListOf<FieldOps<T>>()
        var superClass: Class<*> = delegate
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
        return delegate.fields.map { FieldOps(it) }
    }

    fun declaredFields(): List<FieldOps<T>> {
        return delegate.declaredFields.map { FieldOps(it) }
    }

    fun method(name: String, vararg paramTypes: Class<*>): MethodOps<T>? {
        val fullMethodName = "${delegate.getName()}#${name}${getParametersString(*paramTypes)}"

        if (methodCache.containsKey(fullMethodName)) {
            @Suppress("UNCHECKED_CAST")
            return (methodCache[fullMethodName] as MethodOps<T>?) ?: throw NoSuchMethodError(fullMethodName)
        }

        try {
            val method = MethodOps<T>(XposedHelpers.findMethodExact(delegate, name, *paramTypes))
            methodCache[fullMethodName] = method
            return method
        } catch (_: NoSuchMethodError) {
        }

        var bestMatch: Method? = null
        var clz: Class<*> = delegate
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
            XposedEntry.log("Method not found: $fullMethodName!")
            null
        }
    }

    private fun getParametersString(vararg clazzes: Class<*>): String {
        return "(${clazzes.joinToString(",") { it.getName() }})"
    }

    /* --------- commons-lang start --------- */

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    companion object {
        private val methodCache = mutableMapOf<String, MethodOps<*>>()

        /**
         * Maps primitive `Class`es to their corresponding wrapper `Class`.
         */
        private val primitiveWrapperMap = mapOf(
            java.lang.Boolean.TYPE to Boolean::class.java,
            java.lang.Byte.TYPE to Byte::class.java,
            Character.TYPE to Character::class.java,
            java.lang.Short.TYPE to Short::class.java,
            Integer.TYPE to Integer::class.java,
            java.lang.Long.TYPE to Long::class.java,
            java.lang.Double.TYPE to Double::class.java,
            java.lang.Float.TYPE to Float::class.java,
            Void.TYPE to Void::class.java
        )

        /**
         * Maps wrapper `Class`es to their corresponding primitive types.
         */
        private val wrapperPrimitiveMap = mapOf(
            Boolean::class.java to java.lang.Boolean.TYPE,
            Byte::class.java to java.lang.Byte.TYPE,
            Character::class.java to Character.TYPE,
            Short::class.java to java.lang.Short.TYPE,
            Integer::class.java to Integer.TYPE,
            Long::class.java to java.lang.Long.TYPE,
            Double::class.java to java.lang.Double.TYPE,
            Float::class.java to java.lang.Float.TYPE,
            Void::class.java to Void.TYPE
        )

        /** Array of primitive number types ordered by "promotability"  */
        private val orderedPrimitiveTypes = arrayOf(
            java.lang.Byte.TYPE,
            java.lang.Short.TYPE,
            Character.TYPE,
            Integer.TYPE,
            java.lang.Long.TYPE,
            java.lang.Float.TYPE,
            java.lang.Double.TYPE
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
    private fun getTotalTransformationCost(srcArgs: Array<out Class<*>>, destArgs: Array<out Class<*>>): Float {
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
    private fun getObjectTransformationCost(srcClass: Class<*>, destClass: Class<*>): Float {
        if (destClass.isPrimitive) {
            return getPrimitivePromotionCost(srcClass, destClass)
        }

        var cost = 0.0f
        var superClass: Class<*>? = srcClass
        while (superClass != null && destClass != superClass) {
            if (destClass.isInterface && isAssignable(superClass, destClass)) {
                // slight penalty for interface match.
                // we still want an exact match to override an interface match,
                // but
                // an interface match should override anything where we have to
                // get a superclass.
                cost += 0.25f
                break
            }
            cost++
            superClass = superClass.getSuperclass()
        }
        /*
         * If the destination class is null, we've travelled all the way up to
         * an Object match. We'll penalize this by adding 1.5 to the cost.
         */
        if (superClass == null) {
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
     * @param clazz  the Class to check, may be null
     * @param toClass  the Class to try to assign into, returns false if null
     * @return `true` if assignment possible
     */
    fun isAssignable(clazz: Class<*>?, toClass: Class<*>?): Boolean {
        var cls = clazz
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
                    return java.lang.Long.TYPE == toClass
                            || java.lang.Float.TYPE == toClass
                            || java.lang.Double.TYPE == toClass
                }
                java.lang.Long.TYPE -> {
                    return java.lang.Float.TYPE == toClass
                            || java.lang.Double.TYPE == toClass
                }
                java.lang.Boolean.TYPE -> {
                    return false
                }
                java.lang.Double.TYPE -> {
                    return false
                }
                java.lang.Float.TYPE -> {
                    return java.lang.Double.TYPE == toClass
                }
                java.lang.Short.TYPE, Character.TYPE -> {
                    return Integer.TYPE == toClass
                            || java.lang.Long.TYPE == toClass
                            || java.lang.Float.TYPE == toClass
                            || java.lang.Double.TYPE == toClass
                }
                java.lang.Byte.TYPE -> {
                    return java.lang.Short.TYPE == toClass
                            || Integer.TYPE == toClass
                            || java.lang.Long.TYPE == toClass
                            || java.lang.Float.TYPE == toClass
                            || java.lang.Double.TYPE == toClass
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
    private fun getPrimitivePromotionCost(srcClass: Class<*>, destClass: Class<*>): Float {
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
        return delegate.methods.map { MethodOps(it) }
    }

    fun declaredMethods(): List<MethodOps<T>> {
        return delegate.declaredMethods.map { MethodOps(it) }
    }
}

class ConstructorOps<T : Any>(val delegate: Constructor<T>) {
    fun new(vararg args: Any?): T {
        return delegate.apply { isAccessible = true }.newInstance(*args)
    }

    fun hook(callback: XC_MethodHook): ConstructorOps<T> {
        XposedBridge.hookMethod(delegate, callback)
        return this
    }
}

class FieldOps<T : Any>(val delegate: Field) {
    operator fun get(obj: T?): Any? {
        return delegate.apply { isAccessible = true }[obj]
    }

    operator fun set(obj: T?, value: Any?): FieldOps<T> {
        delegate.apply { isAccessible = true }[obj] = value
        return this
    }
}

class MethodOps<T : Any>(val delegate: Method) {
    fun call(obj: T?, vararg args: Any?): Any? {
        return delegate.apply { isAccessible = true }.invoke(obj, *args)
    }

    fun hook(callback: XC_MethodHook): MethodOps<T> {
        XposedBridge.hookMethod(delegate, callback)
        return this
    }
}