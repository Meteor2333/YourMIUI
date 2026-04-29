package cc.meteormc.yourmiui.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.databinding.FragmentSettingsBinding
import cc.meteormc.yourmiui.service.SettingsPreferences
import cc.meteormc.yourmiui.ui.adapter.SettingGroupAdapter
import cc.meteormc.yourmiui.ui.controller.LanguageController
import cc.meteormc.yourmiui.ui.controller.ThemeController
import cc.meteormc.yourmiui.ui.data.SettingGroup
import cc.meteormc.yourmiui.ui.data.SettingItem
import cc.meteormc.yourmiui.ui.data.SwitchableSettingItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class SettingsFragment : BaseFragment<FragmentSettingsBinding>({ inflater, container ->
    FragmentSettingsBinding.inflate(inflater, container, false)
}) {
    private val settingGroups = arrayOf(
        SettingGroup(
            R.string.settings_language,
            SettingItem(
                R.drawable.ic_language_24dp,
                R.string.settings_language_language_title,
                if (SettingsPreferences.language != SettingsPreferences.LanguageOption.FOLLOW_SYSTEM) R.string.langauge
                else R.string.settings_language_language_followsystem
            ) {
                openOptionDialog(
                    R.string.settings_language_language_title,
                    SettingsPreferences.LanguageOption.entries.map { option ->
                        if (option == SettingsPreferences.LanguageOption.FOLLOW_SYSTEM) {
                            getString(R.string.settings_language_language_followsystem)
                        } else {
                            requireContext().createConfigurationContext(
                                Configuration(resources.configuration).apply {
                                    setLocale(Locale.forLanguageTag(option.value))
                                }
                            ).getString(R.string.langauge)
                        }
                    },
                    SettingsPreferences.language.ordinal
                ) { which ->
                    val selected = SettingsPreferences.LanguageOption.entries[which]
                    if (selected != SettingsPreferences.language) {
                        SettingsPreferences.language = selected
                        LanguageController.apply(selected)
                        requireActivity().recreate()
                    }
                }
            },
            SettingItem(
                R.drawable.ic_translate_24dp,
                R.string.settings_language_translate_title,
                R.string.settings_language_translate_summary
            ) { startActivity(Intent(
                Intent.ACTION_VIEW,
                "https://crowdin.com/project/yourmiui".toUri()
            )) }
        ),
        SettingGroup(
            R.string.settings_theme,
            SettingItem(
                R.drawable.ic_colors_24dp,
                R.string.settings_theme_colormode_title,
                SettingsPreferences.colorMode.res
            ) {
                openOptionDialog(
                    R.string.settings_theme_colormode_title,
                    SettingsPreferences.ColorModeOption.entries.map { option -> getString(option.res) },
                    SettingsPreferences.colorMode.ordinal
                ) { which ->
                    val selected = SettingsPreferences.ColorModeOption.entries[which]
                    if (selected != SettingsPreferences.colorMode) {
                        SettingsPreferences.colorMode = selected
                        ThemeController.apply(selected)
                        it.settingSummary.setText(selected.res)
                        requireActivity().recreate()
                    }
                }
            }
        ),
        SettingGroup(
            R.string.settings_basic,
            SwitchableSettingItem(
                R.drawable.ic_hide_24dp,
                R.string.settings_basic_hideicon_title,
                R.string.settings_basic_hideicon_summary,
                SettingsPreferences.iconHidden
            ) {
                SettingsPreferences.iconHidden = it
                val context = requireContext()
                context.packageManager.setComponentEnabledSetting(
                    ComponentName(context, "${BuildConfig.APPLICATION_ID}.LauncherAlias"),
                    if (!it) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    else {
                        Toast.makeText(
                            context,
                            getString(R.string.settings_basic_hideicon_notice),
                            Toast.LENGTH_LONG
                        ).show()
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    },
                    PackageManager.DONT_KILL_APP
                )
            },
            SwitchableSettingItem(
                R.drawable.ic_update_check_24dp,
                R.string.settings_basic_checkupdate_title,
                R.string.settings_basic_checkupdate_summary,
                SettingsPreferences.updateCheckEnabled
            ) { SettingsPreferences.updateCheckEnabled = it }
        )
    )

    override fun onCreate(): View {
        val settingGroupList = binding.settingGroupList
        settingGroupList.adapter = SettingGroupAdapter(settingGroups)
        settingGroupList.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    private fun openOptionDialog(
        titleRes: Int,
        options: List<String>,
        current: Int,
        listener: (which: Int) -> Unit
    ) {
        val context = requireContext()
        MaterialAlertDialogBuilder(context)
            .setTitle(titleRes)
            .setSingleChoiceItems(options.toTypedArray(), current) { dialog, which ->
                listener(which)
                dialog.dismiss()
            }.show()
    }
}