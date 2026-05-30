package cc.meteormc.yourmiui.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import cc.meteormc.yourmiui.api.util.PrefsUtil

class FeaturePreferences(val prefKey: String) {
    companion object {
        private lateinit var prefs: SharedPreferences

        fun init(context: Context) {
            if (!::prefs.isInitialized) {
                // Xposed会阻止此方法SecurityException的发生
                // 但是如果还没启用模块或者因为各种神秘问题导致报错
                // 就先用MODE_PRIVATE
                this.prefs = runCatching {
                    @Suppress("DEPRECATION")
                    @SuppressLint("WorldReadableFiles")
                    context.getSharedPreferences(
                        PrefsUtil.SHARED_PREFERENCES_NAME,
                        Context.MODE_WORLD_READABLE
                    )
                }.getOrElse {
                    context.getSharedPreferences(
                        PrefsUtil.SHARED_PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                    )
                }
            }
        }

        fun getPreference(prefKey: String) = FeaturePreferences(prefKey)
    }
}