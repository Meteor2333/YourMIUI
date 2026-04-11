package cc.meteormc.yourmiui.ui.controller

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import cc.meteormc.yourmiui.service.SettingsPreferences

object LanguageController {
    fun apply(option: SettingsPreferences.LanguageOption) {
        val locales = if (option == SettingsPreferences.LanguageOption.FOLLOW_SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(option.value)
        }

        AppCompatDelegate.setApplicationLocales(locales)
    }
}
