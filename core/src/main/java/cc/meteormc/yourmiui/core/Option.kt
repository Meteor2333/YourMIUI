package cc.meteormc.yourmiui.core

interface Option {
    fun getPreferenceKey(): String

    fun getNameRes(): Int

    fun getSummaryRes(): Int

    fun getType(): Type<*>

    fun getDefaultValue(): Any

    @Suppress("ClassName", "unused")
    sealed class Type<T>(
        val serializer: (T) -> String,
        val deserializer: (String) -> T?
    ) {

        object BOOLEAN : Type<Boolean>(
            { it.toString() },
            { it.toBooleanStrictOrNull() }
        )

        object BOOLEAN_LIST : Type<List<Boolean>>(
            { it.serializeToString(BOOLEAN) },
            { it.deserializeToList(BOOLEAN) }
        )

        object DOUBLE : Type<Double>(
            { it.toString() },
            { it.toDoubleOrNull() }
        )

        object DOUBLE_LIST : Type<List<Double>>(
            { it.serializeToString(DOUBLE) },
            { it.deserializeToList(DOUBLE) }
        )

        object FLOAT : Type<Float>(
            { it.toString() },
            { it.toFloatOrNull() }
        )

        object FLOAT_LIST : Type<List<Float>>(
            { it.serializeToString(FLOAT) },
            { it.deserializeToList(FLOAT) }
        )

        object INT : Type<Int>(
            { it.toString() },
            { it.toIntOrNull() }
        )

        object INT_LIST : Type<List<Int>>(
            { it.serializeToString(INT) },
            { it.deserializeToList(INT) }
        )

        object STRING : Type<String>(
            { it },
            { it }
        )

        object STRING_LIST : Type<List<String>>(
            { it.serializeToString(STRING) },
            { it.deserializeToList(STRING) }
        )

        companion object {
            fun <T> List<T>.serializeToString(parentType: Type<T>): String {
                return this.joinToString(";") {
                    parentType.serializer(it)
                        .replace("\\", "\\\\")
                        .replace(";", "\\;")
                }
            }

            fun <T> String.deserializeToList(parentType: Type<T>): List<T> {
                if (this.isBlank()) return emptyList()
                return this.split(";").map {
                    buildString {
                        var i = 0
                        while (i < it.length) {
                            if (it[i] == '\\' && i + 1 < it.length) {
                                append(it[i + 1])
                                i += 2
                            } else {
                                append(it[i])
                                i++
                            }
                        }
                    }
                }.mapNotNull { parentType.deserializer(it) }
            }
        }
    }
}