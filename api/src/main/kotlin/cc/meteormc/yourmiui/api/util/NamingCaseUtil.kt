package cc.meteormc.yourmiui.api.util

object NamingCaseUtil {
    private val formatRegex = Regex("""[A-Z]+(?=[A-Z][a-z])|[A-Z]?[a-z]+|[A-Z]+|\d+""")

    fun String.toCamelCase() = tokenize().let {
        buildString {
            append(it.first())
            it.drop(1).forEach { part ->
                append(part.replaceFirstChar { ch -> ch.uppercase() })
            }
        }
    }

    fun String.toPascalCase() = tokenize().joinToString("") {
        it.replaceFirstChar { c ->
            c.uppercase()
        }
    }

    fun String.toSnakeCase() = tokenize().joinToString("_")

    private fun String.tokenize() =
        replace('_', ' ')
            .split(' ')
            .filter { it.isNotBlank() }
            .flatMap { part ->
                formatRegex.findAll(part)
                    .map { it.value.lowercase() }
                    .toList()
            }
}