package cc.meteormc.yourmiui.api.data

data class HookContext(
    val uid: Int,
    val packageName: String,
    val processName: String,
    val classLoader: ClassLoader
)