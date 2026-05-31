package cc.meteormc.yourmiui.api.util

@Suppress("PrivateApi")
object PropertiesUtil {
    private val propClass = Class.forName("android.os.SystemProperties")

    fun get(key: String, defaultValue: String = "unknown"): String {
        return runCatching {
            propClass.getDeclaredMethod(
                "get",
                String::class.java,
                String::class.java
            ).apply {
                isAccessible = true
            }.invoke(
                null,
                key,
                defaultValue
            ) as String
        }.getOrDefault(defaultValue)
    }

    fun getInt(key: String, defaultValue: Int = -1): Int {
        return runCatching {
            propClass.getDeclaredMethod(
                "getInt",
                String::class.java,
                Int::class.javaPrimitiveType
            ).apply {
                isAccessible = true
            }.invoke(
                null,
                key,
                defaultValue
            ) as Int
        }.getOrDefault(defaultValue)
    }

    fun getLong(key: String, defaultValue: Long = -1L): Long {
        return runCatching {
            propClass.getDeclaredMethod(
                "getLong",
                String::class.java,
                Long::class.javaPrimitiveType
            ).apply {
                isAccessible = true
            }.invoke(
                null,
                key,
                defaultValue
            ) as Long
        }.getOrDefault(defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return runCatching {
            propClass.getDeclaredMethod(
                "getBoolean",
                String::class.java,
                Boolean::class.javaPrimitiveType
            ).apply {
                isAccessible = true
            }.invoke(
                null,
                key,
                defaultValue
            ) as Boolean
        }.getOrDefault(defaultValue)
    }
}