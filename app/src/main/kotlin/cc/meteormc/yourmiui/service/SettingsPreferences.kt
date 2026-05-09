package cc.meteormc.yourmiui.service

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import cc.meteormc.yourmiui.R

object SettingsPreferences {
    private const val KEY_LANGUAGE = "language"
    private const val KEY_COLOR_MODE = "color_mode"
    private const val KEY_HIDE_ICON = "hide_icon"
    private const val KEY_ENABLE_UPDATE_CHECK = "enable_update_check"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            this.prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        }
    }

    var language: LanguageOption
        get() = LanguageOption.entries.firstOrNull {
            it.value == prefs.getString(
                KEY_LANGUAGE,
                null
            )
        } ?: LanguageOption.FOLLOW_SYSTEM
        set(value) = prefs.edit { putString(KEY_LANGUAGE, value.value) }

    var colorMode: ColorModeOption
        get() = ColorModeOption.entries.firstOrNull {
            it.value == prefs.getString(
                KEY_COLOR_MODE,
                null
            )
        } ?: ColorModeOption.FOLLOW_SYSTEM
        set(value) = prefs.edit { putString(KEY_COLOR_MODE, value.value) }

    var iconHidden: Boolean
        get() = prefs.getBoolean(KEY_HIDE_ICON, false)
        set(value) = prefs.edit { putBoolean(KEY_HIDE_ICON, value) }

    var updateCheckEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_UPDATE_CHECK, true)
        set(value) = prefs.edit { putBoolean(KEY_ENABLE_UPDATE_CHECK, value) }

    enum class LanguageOption(val value: String) {
        FOLLOW_SYSTEM("system"),
        ENGLISH("en"),
        SIMPLIFIED_CHINESE("zh-Hans"),
        TRADITIONAL_CHINESE("zh-Hant")
    }

    enum class ColorModeOption(val value: String, val res: Int) {
        FOLLOW_SYSTEM("system", R.string.settings_theme_colormode_followsystem),
        LIGHT("light", R.string.settings_theme_colormode_light),
        DARK("dark", R.string.settings_theme_colormode_dark)
    }
}