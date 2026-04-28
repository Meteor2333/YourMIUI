package cc.meteormc.yourmiui.xposed

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Process
import android.util.Log
import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.core.bridge.Bridge
import cc.meteormc.yourmiui.xposed.android.Android
import cc.meteormc.yourmiui.xposed.contentextension.ContentExtension
import cc.meteormc.yourmiui.xposed.market.Market
import cc.meteormc.yourmiui.xposed.mirror.Mirror
import cc.meteormc.yourmiui.xposed.mms.MMS
import cc.meteormc.yourmiui.xposed.nfc.NFC
import cc.meteormc.yourmiui.xposed.notification.Notification
import cc.meteormc.yourmiui.xposed.packageinstaller.PackageInstaller
import cc.meteormc.yourmiui.xposed.securitycenter.SecurityCenter
import cc.meteormc.yourmiui.xposed.settings.Settings
import cc.meteormc.yourmiui.xposed.superwallpaper.SuperWallpaper
import cc.meteormc.yourmiui.xposed.systemadsolution.SystemAdSolution
import cc.meteormc.yourmiui.xposed.systemui.SystemUI
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedEntry : IXposedHookInitPackageResources, IXposedHookLoadPackage {
    private val scopes by lazy {
        listOf(
            Android,
            ContentExtension,
            Market,
            Mirror,
            MMS,
            NFC,
            Notification,
            PackageInstaller,
            SecurityCenter,
            Settings,
            SuperWallpaper,
            SystemAdSolution,
            SystemUI
        )
    }
    private val prefs by lazy {
        XSharedPreferences(BuildConfig.APPLICATION_ID, Feature.PREFERENCE_TAG).apply {
            makeWorldReadable()
            reload()
        }
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        val scope = this.findScope(resparam.packageName) ?: return
        scope.getFeatures().forEach {
            if (!prefs.getBoolean(Feature.enabledKeyOf(it.getPreferenceKey()), false)) return@forEach
            it.onInitResources(resparam.res)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        if (packageName == BuildConfig.APPLICATION_ID) {
            this.initBridge(lpparam.classLoader)
            return
        }

        val scope = this.findScope(packageName) ?: return
        scope.getFeatures().forEach {
            if (!prefs.getBoolean(Feature.enabledKeyOf(it.getPreferenceKey()), false)) return@forEach
            runCatching {
                it.classLoader = lpparam.classLoader
                it.getOptions().forEach { option ->
                    val key = Feature.optionKeyOf(it.getPreferenceKey(), option.getPreferenceKey())
                    val value = prefs.getString(key, null)?.let { preference ->
                        option.getType().deserializer(preference)
                    } ?: option.getDefaultValue()
                    (option as XposedOption<Any>).onValueInit(value)
                }
            }.onFailure { exception ->
                val scopeName = this.javaClass.simpleName
                val featureName = it.javaClass.simpleName
                val stackTrace = Log.getStackTraceString(exception)
                XposedBridge.log("[YourMIUI] Failed to initialize feature '$featureName' in scope '$scopeName':\n$stackTrace")
            }

            it.onLoadPackage()
        }

        operator(Application::class.java) {
            method("attach")?.hookAfter {
                val application = it.getThisObject(Application::class.java)
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        if (context.packageName != intent.getStringExtra("validation")) {
                            return
                        }

                        Process.killProcess(Process.myPid())
                    }
                }
                val filter = IntentFilter().apply { addAction("cc.meteormc.yourmiui.ACTION_RESTART_SCOPE") }
                val permission = "cc.meteormc.yourmiui.permission.RESTART_SCOPE"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    application.registerReceiver(receiver, filter, permission, null, Context.RECEIVER_EXPORTED)
                } else {
                    @SuppressLint("UnspecifiedRegisterReceiverFlag")
                    application.registerReceiver(receiver, filter, permission, null)
                }
            }
        }
    }

    private fun findScope(packageName: String): XposedScope? {
        return this.scopes.firstOrNull { it.getPackages().contains(packageName) }
    }

    private fun initBridge(classLoader: ClassLoader) {
        operator(classLoader, Bridge::class.java.name) {
            method("getApiName")?.hookResult(
                ReflectOperator(XposedBridge::class.java).run {
                    field("TAG")?.get(null, String::class.java) ?: "Unknown"
                }
            )
            method("getApiVersion")?.hookResult(XposedBridge.getXposedVersion())
            method("isModuleActivated")?.hookResult(true)
            method("getScopes")?.hookResult(scopes)
        }
    }
}