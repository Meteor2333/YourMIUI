package cc.meteormc.yourmiui.api.util

object PrimitiveUtil {
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
        Char::class.javaPrimitiveType to Char::class.javaObjectType,
        Short::class.javaPrimitiveType to Short::class.javaObjectType,
        Int::class.javaPrimitiveType to Int::class.javaObjectType,
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
        Char::class.javaObjectType to Char::class.javaPrimitiveType,
        Short::class.javaObjectType to Short::class.javaPrimitiveType,
        Int::class.javaObjectType to Int::class.javaPrimitiveType,
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

    fun getAbbreviation(primitiveName: String): String? {
        return abbreviationMap[primitiveName]
    }

    fun toWrapperClass(primitiveClass: Class<*>): Class<*>? {
        if (!primitiveClass.isPrimitive) return primitiveClass
        return primitiveWrapperMap[primitiveClass]
    }

    fun toPrimitiveClass(wrapperClass: Class<*>): Class<*>? {
        if (wrapperClass.isPrimitive) return wrapperClass
        return wrapperPrimitiveMap[wrapperClass]
    }

    fun canWiden(from: Class<*>, to: Class<*>): Boolean {
        return primitiveWideningMap[from]?.contains(to) == true
    }

    fun canAssign(from: Class<*>, to: Class<*>): Boolean {
        if (to.isAssignableFrom(from)) return true

        val fromPrimitive = toPrimitiveClass(from)
        val toPrimitive = toPrimitiveClass(to)
        return fromPrimitive == toPrimitive
    }
}