package cc.meteormc.yourmiui.common.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.OptionType
import cc.meteormc.yourmiui.api.data.FeatureInfo

class SharedPreferences(val prefs: SharedPreferences) {
    companion object {
        const val SHARED_PREFERENCES_NAME = "shared"
    }

    fun getFeature(feature: FeatureInfo) = Feature(feature.category, feature.key)

    inner class Feature internal constructor(category: Category, featureKey: String) {
        val featureKey = "feature_${category.name.lowercase()}_$featureKey"

        var enabled: Boolean
            get() = prefs.getBoolean("${featureKey}_enabled", false)
            set(value) = prefs.edit { putBoolean("${featureKey}_enabled", value) }

        var options: Map<String, Any>
            get() = prefs.all
                .filterKeys { it.startsWith("${featureKey}_options") }
                .mapKeys { it.key.removePrefix("${featureKey}_options_") }
                .mapValues { it }
            set(value) = prefs.edit {
                prefs.all.keys
                    .filter { it.startsWith("${featureKey}_options") }
                    .forEach { remove(it) }
                value.forEach { (k, v) -> setOption(k, v, null) }
            }

        inline fun <reified T> getOption(key: String, type: OptionType<T>): T? {
            val format = "${featureKey}_options_$key"
            @Suppress("UNCHECKED_CAST")
            return when (val clazz = T::class) {
                String::class -> prefs.getString(format, null)
                Boolean::class -> prefs.getBoolean(format, false)
                Int::class -> prefs.getInt(format, -1)
                Long::class -> prefs.getLong(format, -1L)
                Float::class -> prefs.getFloat(format, -1F)
                Set::class -> prefs.getStringSet(format, null)
                else -> throw IllegalArgumentException("Unsupported type: $clazz")
            } as T?
        }

        fun <T> setOption(key: String, value: T, type: OptionType<T>?) {
            prefs.edit {
                val format = "${featureKey}_options_$key"
                when (value) {
                    is String -> putString(format, value)
                    is Boolean -> putBoolean(format, value)
                    is Int -> putInt(format, value)
                    is Long -> putLong(format, value)
                    is Float -> putFloat(format, value)
                    is Set<*> -> putStringSet(format, value.filterIsInstance<String>().toSet())
                }
            }
        }
    }
}