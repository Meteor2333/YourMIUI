package cc.meteormc.yourmiui.common.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import cc.meteormc.yourmiui.api.data.FeatureInfo

class SharedPreferences(private val prefs: SharedPreferences) {
    companion object {
        const val SHARED_PREFERENCES_NAME = "shared"
    }

    fun getFeature(feature: FeatureInfo) = Feature(feature.key)

    inner class Feature internal constructor(featureKey: String) {
        private val featureKey = "feature_$featureKey"

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
                value.forEach { (k, v) -> setOption(k, v) }
            }

        fun <T> getOption(key: String, type: Class<T>): T? {
            val format = "${featureKey}_option_$key"
            @Suppress("UNCHECKED_CAST")
            return when (type) {
                String::class.java -> prefs.getString(format, null)
                Boolean::class.java -> prefs.getBoolean(format, false)
                Int::class.java -> prefs.getInt(format, -1)
                Long::class.java -> prefs.getLong(format, -1L)
                Float::class.java -> prefs.getFloat(format, -1F)
                Set::class.java -> prefs.getStringSet(format, null)
                else -> throw IllegalArgumentException("Unsupported type: $type")
            } as T?
        }

        fun <T> setOption(key: String, value: T) {
            prefs.edit {
                val format = "${featureKey}_options_$key"
                when (value) {
                    is String -> putString(format, value)
                    is Boolean -> putBoolean(format, value)
                    is Int -> putInt(format, value)
                    is Long -> putLong(format, value)
                    is Float -> putFloat(format, value)
                    is Set<*> -> putStringSet(format, value.mapNotNull { it as? String }.toSet())
                }
            }
        }
    }
}