package cc.meteormc.yourmiui

import android.app.Application
import cc.meteormc.yourmiui.core.bridge.Module
import cc.meteormc.yourmiui.helper.SysVersion
import cc.meteormc.yourmiui.service.FeaturePreference
import cc.meteormc.yourmiui.service.HostDataStore
import cc.meteormc.yourmiui.service.SettingsPreferences
import cc.meteormc.yourmiui.ui.controller.LanguageController
import cc.meteormc.yourmiui.ui.controller.ThemeController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.system.exitProcess

class YourMIUI : Application() {
	companion object {
		private lateinit var instance: YourMIUI

		fun get() = instance
	}

	init {
		instance = this
	}

	val moduleBridge = Module(this)
	val hostDataStore = HostDataStore()

	override fun onCreate() {
		super.onCreate()
		checkSystem()
		FeaturePreference.init(this)
		SettingsPreferences.init(this)
		LanguageController.apply(SettingsPreferences.language)
		ThemeController.apply(SettingsPreferences.colorMode)
		initModuleBridge()
	}

	private fun checkSystem() {
		val check = when (val current = SysVersion.getCurrent()) {
			SysVersion.HYPEROS -> getString(R.string.syscheck_hyperos)
			SysVersion.OTHER -> getString(R.string.syscheck_unknown_system)
			SysVersion.UNSUPPORTED -> getString(R.string.syscheck_unsupported_version, current.code)
			else -> return
		}

		MaterialAlertDialogBuilder(this)
			.setTitle(R.string.syscheck_title)
			.setMessage(check)
			.setCancelable(false)
			.setPositiveButton(R.string.syscheck_exit) { _, _ -> exitProcess(0) }
			.setNegativeButton(R.string.syscheck_ignore, null)
			.show()
	}

	private fun initModuleBridge() {
		moduleBridge.attach()
		hostDataStore.fetch(moduleBridge)
	}
}