package cc.meteormc.yourmiui.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.util.Unsafe

class FeaturePreferences(val prefKey: String) {
    companion object {
        private lateinit var prefs: SharedPreferences

        fun init(context: Context) {
            if (!::prefs.isInitialized) {
                // Xposed会阻止此方法SecurityException的发生
                // 但是如果还没启用模块或者因为各种神秘问题导致报错
                // 就先用MODE_PRIVATE
                this.prefs = runCatching {
                    context.getSharedPreferences(
                        Feature.PREFERENCES_NAME,
                        Unsafe.CONTEXT_MODE_WORLD_READABLE
                    )
                }.getOrElse {
                    context.getSharedPreferences(
                        Feature.PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                    )
                }
            }
        }

        fun getPreference(prefKey: String) = FeaturePreferences(prefKey)
    }

    var enabled: Boolean
        get() = prefs.getBoolean(Feature.enabledKeyOf(prefKey), false)
        set(value) = prefs.edit { putBoolean(Feature.enabledKeyOf(prefKey), value) }

    fun option(key: String): String? = prefs.getString(Feature.optionKeyOf(prefKey, key), null)

    fun option(key: String, value: String) = prefs.edit { putString(Feature.optionKeyOf(prefKey, key), value) }
}