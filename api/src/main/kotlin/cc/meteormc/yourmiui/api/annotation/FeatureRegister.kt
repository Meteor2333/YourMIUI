package cc.meteormc.yourmiui.api.annotation

import cc.meteormc.yourmiui.api.Category

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class FeatureRegister(
    val category: Category,
    val name: String,
    val description: String,
    val warning: String = "",
    val originalAuthor: String = ""
)