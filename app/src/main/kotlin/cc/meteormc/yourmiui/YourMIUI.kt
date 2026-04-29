package cc.meteormc.yourmiui

import android.app.Application
import android.content.pm.PackageManager
import cc.meteormc.yourmiui.core.Scope
import cc.meteormc.yourmiui.core.bridge.Bridge
import cc.meteormc.yourmiui.core.bridge.Module
import cc.meteormc.yourmiui.core.bridge.ResponseCallback
import cc.meteormc.yourmiui.helper.SysVersion
import cc.meteormc.yourmiui.service.FeaturePreference
import cc.meteormc.yourmiui.service.SettingsPreferences
import cc.meteormc.yourmiui.ui.controller.LanguageController
import cc.meteormc.yourmiui.ui.controller.ThemeController
import cc.meteormc.yourmiui.ui.data.AppInfo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.system.exitProcess

class YourMIUI : Application() {
	companion object {
		@JvmStatic
		lateinit var INSTANCE: YourMIUI
			private set
	}

	init {
	    INSTANCE = this
	}

	val moduleBridge = Module(this)
	var scopes: Map<Scope, List<AppInfo>> = emptyMap()

	override fun onCreate() {
		super.onCreate()
		checkSystem()
		FeaturePreference.init(this)
		SettingsPreferences.init(this)
		LanguageController.apply(SettingsPreferences.language)
		ThemeController.apply(SettingsPreferences.colorMode)
		initModuleBridge()
		loadScopes()
	}

	private fun initModuleBridge() {
		moduleBridge.attach()
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

	private fun loadScopes() {
		moduleBridge.request(
			Bridge.SCOPES_CHANNEL,
			BuildConfig.APPLICATION_ID,
			object : ResponseCallback<ArrayList<Scope>> {
				override fun onSuccess(data: ArrayList<Scope>) {
					scopes = data.associateWith {
						// 获取目标应用的名称和图标
						it.getPackages().mapNotNull { pkg ->
							val info = runCatching {
								packageManager.getApplicationInfo(pkg, PackageManager.GET_META_DATA)
							}.getOrNull() ?: return@mapNotNull null
							AppInfo(
								pkg,
								packageManager.getApplicationLabel(info).toString(),
								packageManager.getApplicationIcon(info)
							)
						}
					}.filterValues {
						// 过滤掉未安装的应用
						it.isNotEmpty()
					}
				}

				override fun onFailure() {
				}
			}
		)
	}
}