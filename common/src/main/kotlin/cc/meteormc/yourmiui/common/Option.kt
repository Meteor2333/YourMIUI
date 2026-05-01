package cc.meteormc.yourmiui.common

import java.io.*

class Option<T : Any>(
    val key: String,
    val nameRes: Int,
    val summaryRes: Int,
    val type: Type<T>,
    val defaultValue: T,
    @Transient
    val onValueInit: (value: T) -> Unit
) : Serializable {
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

        class App : Type<String>(
            { it },
            { it }
        )

        class AppList : Type<Set<String>>(
            { it.serializeToString() },
            { it.deserializeToCollection().toSet() }
        )

        class SingleChoiceList(vararg options: Pair<String, Int>) : Type<String>(
            { it },
            { it }
        ) {
            val options: List<Pair<String, Int>> = options.toList()
        }

        class MultiChoiceList(vararg options: Pair<String, Int>) : Type<Set<String>>(
            { it.serializeToString() },
            { it.deserializeToCollection().toSet() }
        ) {
            val options: List<Pair<String, Int>> = options.toList()
        }

        class Switch : Type<Boolean>(
            { it.toString() },
            { it.toBooleanStrictOrNull() }
        )

        class Text : Type<String>(
            { it },
            { it }
        )

        companion object {
            private val lookupTypes = mapOf(
                "App" to App(),
                "AppList" to AppList(),
                "SingleChoiceList" to SingleChoiceList(),
                "MultiChoiceList" to MultiChoiceList(),
                "Switch" to Switch(),
                "Text" to Text()
            )

            @Suppress("UNCHECKED_CAST")
            fun <T> getTypeByObject(obj: Any?): Type<T>? {
                val type = when (obj) {
                    is Type<*> -> obj
                    is Serializable -> {
                        val bytes = ByteArrayOutputStream().use { output ->
                            ObjectOutputStream(output).use { it.writeObject(obj) }
                            output.toByteArray()
                        }

                        ByteArrayInputStream(bytes).use { input ->
                            ObjectInputStream(input).use { it.readObject() }
                        }
                    }
                    else -> null
                } as? Type<T>? ?: return null
                val lookupType = lookupTypes[type.javaClass.simpleName] ?: return null
                type.serializer = lookupType.serializer as (T) -> String
                type.deserializer = lookupType.deserializer as (String) -> T?
                return type
            }

            private fun Collection<String>.serializeToString(): String {
                return this.joinToString(";") { it.escape() }
            }

            private fun String.deserializeToCollection(): Collection<String> {
                if (this.isBlank()) return emptySet()
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