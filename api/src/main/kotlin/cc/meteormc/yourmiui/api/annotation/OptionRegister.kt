package cc.meteormc.yourmiui.api.annotation

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class AppOptionRegister(
    val name: String,
    val description: String,
    val defaultPackages: Array<String> = [],
    val multiSelect: Boolean = true
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ListOptionRegister(
    val name: String,
    val description: String,
    val options: Array<String>,
    val displayOptions: Array<String> = [],
    val defaultOptions: Array<String> = [],
    val multiSelect: Boolean = true
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SwitchOptionRegister(
    val name: String,
    val description: String,
    val defaultValue: Boolean = false
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class TextOptionRegister(
    val name: String,
    val description: String,
    val defaultText: String = ""
)