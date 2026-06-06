package cc.meteormc.yourmiui

import android.app.Activity
import android.app.Application
import android.os.Bundle
import cc.meteormc.yourmiui.common.bridge.Module
import cc.meteormc.yourmiui.common.prefs.SharedPreferences
import cc.meteormc.yourmiui.helper.SysVersion
import cc.meteormc.yourmiui.preferences.SettingsPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class YourMIUI : Application() {
    companion object {
        private lateinit var instance: YourMIUI

        fun get() = instance
    }

    init {
        instance = this
    }

    val moduleBridge = Module(this)
    val prefs by lazy {
        SharedPreferences(
            // Xposed会阻止此方法SecurityException的发生
            // 但是如果还没启用模块或者因为各种神秘问题导致报错
            // 就先用MODE_PRIVATE
            runCatching {
                @Suppress("DEPRECATION")
                this.getSharedPreferences(
                    SharedPreferences.SHARED_PREFERENCES_NAME,
                    MODE_WORLD_READABLE
                )
            }.getOrElse {
                this.getSharedPreferences(
                    SharedPreferences.SHARED_PREFERENCES_NAME,
                    MODE_PRIVATE
                )
            }
        )
    }

    override fun onCreate() {
        super.onCreate()
        checkSystem()
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

        registerActivityLifecycleCallbacks(
            object : ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    unregisterActivityLifecycleCallbacks(this)
                    MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.syscheck_title)
                        .setMessage(check)
                        .setCancelable(false)
                        .setPositiveButton(R.string.syscheck_exit) { _, _ -> activity.finishAffinity() }
                        .setNegativeButton(R.string.syscheck_ignore, null)
                        .show()
                }

                override fun onActivityDestroyed(activity: Activity) { }
                override fun onActivityPaused(activity: Activity) { }
                override fun onActivityResumed(activity: Activity) { }
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { }
                override fun onActivityStarted(activity: Activity) { }
                override fun onActivityStopped(activity: Activity) { }
            }
        )
    }

    private fun initModuleBridge() {
        moduleBridge.attach()
    }
}