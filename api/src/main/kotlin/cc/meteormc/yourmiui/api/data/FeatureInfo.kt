package cc.meteormc.yourmiui.api.data

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.api.util.NamingCaseUtil.toSnakeCase

data class FeatureInfo(
    val key: String,
    val category: Category,
    val name: String,
    val description: String,
    val warning: String?,
    val originalAuthor: String?,
    val options: List<OptionInfo>,
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
                registerAnnotation.name,
                registerAnnotation.description,
                registerAnnotation.warning.takeIf { it.isNotEmpty() },
                registerAnnotation.originalAuthor.takeIf { it.isNotEmpty() },
                source.declaredFields.mapNotNull { OptionInfo.fromSource(it) },
                scopeAnnotations.map { it.value },
                source,
                hooker
            )
        }
    }
}