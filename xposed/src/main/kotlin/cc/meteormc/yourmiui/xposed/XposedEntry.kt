package cc.meteormc.yourmiui.xposed

import android.app.Application
import android.content.Context
import android.os.Process
import android.util.Log
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.common.bridge.Bridge
import cc.meteormc.yourmiui.common.bridge.Host
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
    private lateinit var hostBridge: Host
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
        XSharedPreferences("cc.meteormc.yourmiui", Feature.PREFERENCE_TAG).apply {
            makeWorldReadable()
            reload()
        }
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        initFeatures(resparam.packageName) {
            it.onInitResources(resparam.res)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        initFeatures(lpparam.packageName) {
            runCatching {
                it.classLoader = lpparam.classLoader
                it.getOptions().forEach { option ->
                    val key = Feature.optionKeyOf(it.key, option.key)
                    val value = prefs.getString(key, null)?.let { preference ->
                        option.type.deserializer(preference)
                    } ?: option.defaultValue
                    (option as Option<Any>).onValueInit(value)
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
                initHostBridge(lpparam.classLoader, it.getThisObject(Context::class.java))
            }
        }
    }

    private fun initHostBridge(classLoader: ClassLoader, context: Context) {
        hostBridge = Host(context)
        hostBridge.register(Bridge.GET_SCOPES_CHANNEL) {
            scopes.toCollection(ArrayList())
        }.register(Bridge.RESTART_SCOPE_CHANNEL) {
            Thread {
                Thread.sleep(300)
                Process.killProcess(Process.myPid())
            }.start()
        }.attach()

        operator(classLoader, Bridge::class.java.name) {
            val apiName = ReflectOperator(XposedBridge::class.java).run {
                field("TAG")?.get(null)
            } ?: "Unknown"
            val apiVersion = XposedBridge.getXposedVersion()
            field("apiName")?.set(null, apiName)
            field("apiVersion")?.set(null, apiVersion)
        }
    }

    private fun initFeatures(packageName: String, initializer: (feature: Feature) -> Unit) {
        this.scopes.firstOrNull {
            it.packages.map { pkg -> pkg.first }.contains(packageName)
        }?.getFeatures()?.forEach {
            if (!prefs.getBoolean(Feature.enabledKeyOf(it.key), false)) return@forEach
            initializer(it)
        }
    }
}