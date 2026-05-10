package cc.meteormc.yourmiui

import android.app.Application
import cc.meteormc.yourmiui.common.bridge.Module
import cc.meteormc.yourmiui.helper.SysVersion
import cc.meteormc.yourmiui.preferences.FeaturePreferences
import cc.meteormc.yourmiui.preferences.SettingsPreferences
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

    override fun onCreate() {
        super.onCreate()
        checkSystem()
        FeaturePreferences.init(this)
        SettingsPreferences.init(this)
        initModuleBridge()
    }

    private fun checkSystem() {
        val check = when (val current = SysVersion.getCurrent()) {
            SysVersion.HYPEROS -> getString(R.string.syscheck_hyperos)
            SysVersion.OTHER -> getString(R.string.syscheck_unknown_system)
            SysVersion.MIUI_UNSUPPORTED -> getString(R.string.syscheck_unsupported_version, current.code)
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
    }
}