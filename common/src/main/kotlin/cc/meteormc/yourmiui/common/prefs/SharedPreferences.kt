package cc.meteormc.yourmiui.common.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import cc.meteormc.yourmiui.api.data.FeatureInfo

class SharedPreferences(private val prefs: SharedPreferences) {
    companion object {
        const val SHARED_PREFERENCES_NAME = "shared"

        private val formatRegex = Regex("([a-z0-9])([A-Z])")
    }

    fun getFeature(feature: FeatureInfo) = Feature(
        feature.id
            .replace(formatRegex, "$1_$2")
            .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1_$2")
            .lowercase()
    )

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
            val format = key.replace(formatRegex, "$1_$2").lowercase()
            val key = "${featureKey}_option_$format"
            @Suppress("UNCHECKED_CAST")
            return when (type) {
                String::class.java -> prefs.getString(key, null)
                Boolean::class.java -> prefs.getBoolean(key, false)
                Int::class.java -> prefs.getInt(key, -1)
                Long::class.java -> prefs.getLong(key, -1L)
                Float::class.java -> prefs.getFloat(key, -1F)
                Set::class.java -> prefs.getStringSet(key, null)
                else -> throw IllegalArgumentException("Unsupported type: $type")
            } as T?
        }

        fun <T> setOption(key: String, value: T) {
            val format = key.replace(formatRegex, "$1_$2").lowercase()
            prefs.edit {
                val key = "${featureKey}_options_$format"
                when (value) {
                    is String -> putString(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Set<*> -> putStringSet(key, value.mapNotNull { it as? String }.toSet())
                }
            }
            prefs.getString("${featureKey}_${key.replace(formatRegex, "$1_$2").lowercase()}", null)
        }
    }
}