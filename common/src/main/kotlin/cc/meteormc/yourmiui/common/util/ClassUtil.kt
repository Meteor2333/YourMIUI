@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package cc.meteormc.yourmiui.common.util

import kotlin.math.abs

// From commons-lang

/**
 * The package separator character: `'&#x2e;' == {@value}`.
 */
const val PACKAGE_SEPARATOR_CHAR = '.'

/**
 * The inner class separator character: `'$' == {@value}`.
 */
private const val INNER_CLASS_SEPARATOR_CHAR = '$'

/**
 * Maps a primitive class name to its corresponding abbreviation used in array class names.
 */
private val abbreviationMap = mapOf(
    "int" to "I",
    "boolean" to "Z",
    "float" to "F",
    "long" to "J",
    "short" to "S",
    "byte" to "B",
    "double" to "D",
    "char" to "C"
)

/**
 * Maps primitive `Class`es to their corresponding wrapper `Class`.
 */
private val primitiveWrapperMap = mapOf(
    Boolean::class.javaPrimitiveType to Boolean::class.javaObjectType,
    Byte::class.javaPrimitiveType to Byte::class.javaObjectType,
    Character::class.javaPrimitiveType to Character::class.javaObjectType,
    Short::class.javaPrimitiveType to Short::class.javaObjectType,
    Integer::class.javaPrimitiveType to Integer::class.javaObjectType,
    Long::class.javaPrimitiveType to Long::class.javaObjectType,
    Double::class.javaPrimitiveType to Double::class.javaObjectType,
    Float::class.javaPrimitiveType to Float::class.javaObjectType,
    Void::class.javaPrimitiveType to Void::class.javaObjectType
)

/**
 * Maps wrapper `Class`es to their corresponding primitive types.
 */
private val wrapperPrimitiveMap = mapOf(
    Boolean::class.javaObjectType to Boolean::class.javaPrimitiveType,
    Byte::class.javaObjectType to Byte::class.javaPrimitiveType,
    Character::class.javaObjectType to Character::class.javaPrimitiveType,
    Short::class.javaObjectType to Short::class.javaPrimitiveType,
    Integer::class.javaObjectType to Integer::class.javaPrimitiveType,
    Long::class.javaObjectType to Long::class.javaPrimitiveType,
    Double::class.javaObjectType to Double::class.javaPrimitiveType,
    Float::class.javaObjectType to Float::class.javaPrimitiveType,
    Void::class.javaObjectType to Void::class.javaPrimitiveType
)

/**
 * Maps primitive `Class`es to the primitive `Class`es they can be widened to.
 */
private val primitiveWideningMap = mapOf(
    Byte::class.javaPrimitiveType!! to setOf(
        Short::class.javaPrimitiveType!!,
        Int::class.javaPrimitiveType!!,
        Long::class.javaPrimitiveType!!,
        Float::class.javaPrimitiveType!!,
        Double::class.javaPrimitiveType!!
    ),
    Short::class.javaPrimitiveType!! to setOf(
        Int::class.javaPrimitiveType!!,
        Long::class.javaPrimitiveType!!,
        Float::class.javaPrimitiveType!!,
        Double::class.javaPrimitiveType!!
    ),
    Char::class.javaPrimitiveType!! to setOf(
        Int::class.javaPrimitiveType!!,
        Long::class.javaPrimitiveType!!,
        Float::class.javaPrimitiveType!!,
        Double::class.javaPrimitiveType!!
    ),
    Int::class.javaPrimitiveType!! to setOf(
        Long::class.javaPrimitiveType!!,
        Float::class.javaPrimitiveType!!,
        Double::class.javaPrimitiveType!!
    ),
    Long::class.javaPrimitiveType!! to setOf(
        Float::class.javaPrimitiveType!!,
        Double::class.javaPrimitiveType!!
    ),
    Float::class.javaPrimitiveType!! to setOf(
        Double::class.javaPrimitiveType!!
    )
)

/**
 * Array of primitive number types ordered by "promotability".
 */
private val orderedPrimitiveTypes = listOf(
    Byte::class.javaPrimitiveType,
    Short::class.javaPrimitiveType,
    Char::class.javaPrimitiveType,
    Int::class.javaPrimitiveType,
    Long::class.javaPrimitiveType,
    Float::class.javaPrimitiveType,
    Double::class.javaPrimitiveType
)

private val cache = mutableMapOf<String, Class<*>>()

/**
 * Returns the class represented by `className` using the
 * `classLoader`.  This implementation supports the syntaxes
 * "`java.util.Map.Entry[]`", "`java.util.Map$Entry[]`",
 * "`[Ljava.util.Map.Entry;`", and "`[Ljava.util.Map$Entry;`".
 *
 * @param classLoader  the class loader to use to load the class
 * @param className  the class name
 * @param initialize  whether the class must be initialized
 * @return the class represented by `className` using the `classLoader`
 */
fun getClass(classLoader: ClassLoader? = null, className: String, initialize: Boolean): Class<*>? {
    fun forName(name: String): Class<*> {
        return if (classLoader == null) Class.forName(name)
        else Class.forName(name, initialize, classLoader)
    }

    fun tryLoad(name: String): Class<*> {
        return abbreviationMap[name]?.let { abbreviation ->
            forName("[$abbreviation").componentType
        } ?: forName(toCanonicalName(name))
    }

    return cache[className] ?: runCatching {
        tryLoad(className)
    }.recoverCatching { ex ->
        if (ex !is ClassNotFoundException) throw ex

        val lastDot = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR)
        if (lastDot == -1) throw ex

        val innerName = className.replaceRange(
            lastDot,
            lastDot + 1,
            "$INNER_CLASS_SEPARATOR_CHAR"
        )

        tryLoad(innerName)
    }.onSuccess {
        cache[className] = it
    }.getOrNull()
}

/**
 * Converts a class name to a JLS style class name.
 *
 * @param className  the class name
 * @return the converted name
 */
private fun toCanonicalName(className: String): String {
    var name = className.filterNot { it.isWhitespace() }
    require(name.isNotEmpty()) { "className must not be null." }

    if (!name.endsWith("[]")) return name
    val prefix = buildString {
        while (name.endsWith("[]")) {
            name = name.dropLast(2)
            append('[')
        }
    }

    return prefix + (abbreviationMap[name] ?: "L$name;")
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
fun compareParameterTypes(
    left: Array<Class<*>>,
    right: Array<Class<*>>,
    actual: Array<out Class<*>>
): Int {
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
private fun getTotalTransformationCost(
    srcArgs: Array<out Class<*>>,
    destArgs: Array<out Class<*>>
): Float {
    val size = minOf(srcArgs.size, destArgs.size)
    val extra = abs(srcArgs.size - destArgs.size)

    var totalCost = extra * 5.0f
    for (i in 0 until size) {
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

    var cost = 0f
    var current: Class<*>? = srcClass
    while (current != null && current != destClass) {
        if (destClass.isInterface && isAssignable(current, destClass)) {
            return cost + 0.25f
        }

        cost++
        current = current.superclass
    }

    return if (current == null) cost + 1.5f else cost
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
private fun isAssignable(clazz: Class<*>?, toClass: Class<*>?): Boolean {
    if (toClass == null) return false
    if (clazz == null) return !toClass.isPrimitive

    var from = clazz
    if (from.isPrimitive && !toClass.isPrimitive) {
        from = primitiveWrapperMap[from] ?: return false
    }
    if (toClass.isPrimitive && !from.isPrimitive) {
        from = wrapperPrimitiveMap[from] ?: return false
    }

    if (from == toClass) return true
    if (from.isPrimitive) {
        if (!toClass.isPrimitive) return false
        return primitiveWideningMap[from]?.contains(toClass) == true
    }

    return toClass.isAssignableFrom(from)
}

/**
 * Gets the number of steps required to promote a primitive number to another
 * type.
 * @param srcClass the (primitive) source class
 * @param destClass the (primitive) destination class
 * @return The cost of promoting the primitive
 */
private fun getPrimitivePromotionCost(srcClass: Class<*>, destClass: Class<*>): Float {
    var cost = 0f
    val current = if (!srcClass.isPrimitive) {
        cost += 0.1f
        wrapperPrimitiveMap[srcClass] ?: return 0f
    } else {
        srcClass
    }

    val srcIndex = orderedPrimitiveTypes.indexOf(current)
    val destIndex = orderedPrimitiveTypes.indexOf(destClass)
    if (srcIndex == -1 || destIndex == -1 || srcIndex > destIndex) {
        return cost
    }

    cost += (destIndex - srcIndex) * 0.1f
    return cost
}