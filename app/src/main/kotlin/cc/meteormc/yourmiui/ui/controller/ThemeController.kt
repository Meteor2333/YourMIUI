package cc.meteormc.yourmiui.ui.controller

import androidx.appcompat.app.AppCompatDelegate
import cc.meteormc.yourmiui.service.SettingsPreferences

object ThemeController {
    fun apply(option: SettingsPreferences.ColorModeOption) {
        val mode = when (option) {
            SettingsPreferences.ColorModeOption.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            SettingsPreferences.ColorModeOption.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            SettingsPreferences.ColorModeOption.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }

        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
