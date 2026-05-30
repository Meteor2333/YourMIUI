package cc.meteormc.yourmiui.api.util

object SingletonUtil {
    fun getInstance(clazz: Class<*>) = runCatching {
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        field.get(null)
    }.getOrNull()
}