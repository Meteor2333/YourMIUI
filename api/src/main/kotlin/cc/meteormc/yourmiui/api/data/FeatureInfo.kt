package cc.meteormc.yourmiui.api.data

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope

data class FeatureInfo(
    val id: String,
    val category: Category,
    val name: Int,
    val description: Int,
    val warning: Int?,
    val originalAuthor: String?,
    val scopes: List<String>,
    val source: Class<*>
) {
    companion object {
        fun fromSource(source: Class<*>): FeatureInfo {
            val registerAnnotation = source.getDeclaredAnnotation(FeatureRegister::class.java)
            val scopeAnnotations = source.getDeclaredAnnotationsByType(RequiredScope::class.java)
            return FeatureInfo(
                source.simpleName
                    .replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
                    .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1_$2")
                    .lowercase(),
                registerAnnotation.category,
                // todo: toInt只是为了过编译，后续需要改成解析真正的字符串资源id
                registerAnnotation.name.toInt(),
                registerAnnotation.description.toInt(),
                registerAnnotation.warning.toInt(),
                registerAnnotation.originalAuthor.takeIf { it.isNotEmpty() },
                scopeAnnotations.map { it.value },
                source
            )
        }
    }
}