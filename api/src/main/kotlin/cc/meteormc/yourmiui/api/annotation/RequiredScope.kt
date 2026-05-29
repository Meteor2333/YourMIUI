package cc.meteormc.yourmiui.api.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class RequiredScope(
    val value: String
)
