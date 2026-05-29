package cc.meteormc.yourmiui.api.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class FeatureRegister(
    val category: String,
    val name: String,
    val description: String,
    val warning: String = "",
    val originalAuthor: String = ""
)