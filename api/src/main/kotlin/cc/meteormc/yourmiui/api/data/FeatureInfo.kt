package cc.meteormc.yourmiui.api.data

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.api.util.NamingCaseUtil.toSnakeCase

data class FeatureInfo(
    val key: String,
    val category: Category,
    val name: Int,
    val description: Int,
    val warning: Int?,
    val originalAuthor: String?,
    val scopes: List<String>,
    val source: Class<*>,
    val hooker: FeatureHooker
) {
    companion object {
        fun fromHooker(hooker: FeatureHooker): FeatureInfo {
            val source = hooker.javaClass
            val registerAnnotation = source.getDeclaredAnnotation(FeatureRegister::class.java)
            val scopeAnnotations = source.getDeclaredAnnotationsByType(RequiredScope::class.java)
            return FeatureInfo(
                source.simpleName.toSnakeCase(),
                registerAnnotation.category,
                // todo: toInt只是为了过编译，后续需要改成解析真正的字符串资源id
                registerAnnotation.name.toInt(),
                registerAnnotation.description.toInt(),
                registerAnnotation.warning.toInt(),
                registerAnnotation.originalAuthor.takeIf { it.isNotEmpty() },
                scopeAnnotations.map { it.value },
                source,
                hooker
            )
        }
    }
}