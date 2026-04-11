package cc.meteormc.yourmiui

import android.app.Application
import android.content.pm.PackageManager
import cc.meteormc.yourmiui.core.Scope
import cc.meteormc.yourmiui.core.bridge.Bridge
import cc.meteormc.yourmiui.core.util.proxyClass
import cc.meteormc.yourmiui.service.FeaturePreference
import cc.meteormc.yourmiui.service.SettingsPreferences
import cc.meteormc.yourmiui.ui.controller.LanguageController
import cc.meteormc.yourmiui.ui.controller.ThemeController
import cc.meteormc.yourmiui.ui.data.AppInfo

class YourMIUI : Application() {
	lateinit var scopes: Map<Scope, List<AppInfo>>

	override fun onCreate() {
		super.onCreate()
		FeaturePreference.init(this)
		SettingsPreferences.init(this)
		LanguageController.apply(SettingsPreferences.language)
		ThemeController.apply(SettingsPreferences.colorMode)
		this.loadScopes()
	}

	private fun loadScopes() {
		this.scopes = Bridge.getScopes<Any>()
			.map { proxyClass(Scope::class.java, it!!) }
			.filterIsInstance<Scope>()
			// 获取目标应用的名称和图标
			.associateWith {
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
			}
			// 过滤掉未安装的应用
			.filterValues { it.isNotEmpty() }
	}
}