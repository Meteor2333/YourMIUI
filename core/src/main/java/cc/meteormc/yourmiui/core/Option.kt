package cc.meteormc.yourmiui.core

import java.io.*

interface Option {
    fun getPreferenceKey(): String

    fun getNameRes(): Int

    fun getSummaryRes(): Int

    fun getType(): Any

    fun getDefaultValue(): Any

    // 一种仿enum类的写法
    @Suppress("ClassName")
    sealed class Type<T>(
        serializer: (T) -> String,
        deserializer: (String) -> T?
    ) : Serializable {
        @Transient
        var serializer: (T) -> String = serializer
            private set
        @Transient
        var deserializer: (String) -> T? = deserializer
            private set

        class APP : Type<String>(
            { it },
            { it }
        )

        class APP_LIST : Type<List<String>>(
            { it.serializeToString() },
            { it.deserializeToList() }
        )

        class SINGLE_LIST(vararg options: Pair<String, Int>) : Type<String>(
            { it },
            { it }
        ) {
            val options: List<Pair<String, Int>> = options.toList()
        }

        class MULTI_LIST(vararg options: Pair<String, Int>) : Type<List<String>>(
            { it.serializeToString() },
            { it.deserializeToList() }
        ) {
            val options: List<Pair<String, Int>> = options.toList()
        }

        class SWITCH : Type<Boolean>(
            { it.toString() },
            { it.toBooleanStrictOrNull() }
        )

        class TEXT : Type<String>(
            { it },
            { it }
        )

        companion object {
            private val lookupTypes = mapOf(
                "APP" to APP(),
                "APP_LIST" to APP_LIST(),
                "SINGLE_LIST" to SINGLE_LIST(),
                "MULTI_LIST" to MULTI_LIST(),
                "SWITCH" to SWITCH(),
                "TEXT" to TEXT()
            )

            @Suppress("UNCHECKED_CAST")
            fun <T> getTypeByObject(obj: Any): Type<T>? {
                if (obj is Type<*>) return obj as Type<T>
                if (obj !is Serializable) return null
                val bytes = ByteArrayOutputStream().use { output ->
                    ObjectOutputStream(output).use { it.writeObject(obj) }
                    output.toByteArray()
                }

                val type = ByteArrayInputStream(bytes).use { input ->
                    ObjectInputStream(input).use { it.readObject() }
                } as? Type<T> ?: return null
                val lookupType = lookupTypes[type.javaClass.simpleName] ?: return null
                type.serializer = lookupType.serializer as (T) -> String
                type.deserializer = lookupType.deserializer as (String) -> T?
                return type
            }

            private fun List<String>.serializeToString(): String {
                return this.joinToString(";") { it.escape() }
            }

            private fun String.deserializeToList(): List<String> {
                if (this.isBlank()) return emptyList()
                return this.split(';').map { it.unescape() }
            }

            private fun String.escape() = this.replace("\\", "\\\\").replace(";", "\\;")

            private fun String.unescape() = buildString {
                var i = 0
                val input = this@unescape

                while (i < input.length) {
                    if (input[i] == '\\' && i + 1 < input.length) {
                        append(input[i + 1])
                        i += 2
                    } else {
                        append(input[i])
                        i++
                    }
                }
            }
        }
    }
}