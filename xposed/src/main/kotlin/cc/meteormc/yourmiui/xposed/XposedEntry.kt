package cc.meteormc.yourmiui.xposed

import android.app.Application
import android.content.Context
import android.os.Process
import android.util.Log
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.common.bridge.Bridge
import cc.meteormc.yourmiui.common.bridge.Host
import cc.meteormc.yourmiui.common.util.getClass
import cc.meteormc.yourmiui.xposed.android.Android
import cc.meteormc.yourmiui.xposed.contentextension.ContentExtension
import cc.meteormc.yourmiui.xposed.home.Home
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
            Home,
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
        XSharedPreferences("cc.meteormc.yourmiui", Feature.PREFERENCES_NAME).apply {
            makeWorldReadable()
            reload()
        }
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        initFeatures(resparam.packageName) {
            it.resources = resparam.res

            runCatching {
                it.onInitResources()
            }.onFailure { exception ->
                XposedBridge.log(
                    "[YourMIUI] Failed to " +
                            "initialize resources for feature '${it.javaClass.simpleName}' " +
                            "in scope '${this.javaClass.simpleName}':\n" +
                            Log.getStackTraceString(exception)
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        initFeatures(lpparam.packageName) {
            it.classLoader = lpparam.classLoader

            runCatching {
                it.getOptions().forEach { option ->
                    val key = Feature.optionKeyOf(it.key, option.key)
                    val value = prefs.getString(key, null)?.let { preference ->
                        option.type.deserializer(preference)
                    } ?: option.defaultValue
                    (option as Option<Any>).onValueInit(value)
                }

                it.onLoadPackage()
            }.onFailure { exception ->
                XposedBridge.log(
                    "[YourMIUI] Failed to " +
                            "initialize feature '${it.javaClass.simpleName}' " +
                            "in scope '${this.javaClass.simpleName}':\n" +
                            Log.getStackTraceString(exception)
                )
            }

        }

        operator(Application::class.java) {
            method("attach")?.hookAfter {
                initHostBridge(lpparam.classLoader, it.instance())
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

        val bridgeClass = getClass(classLoader, Bridge::class.java.name, true)
        if (bridgeClass != null) {
            operator(bridgeClass) {
                val apiName = ReflectOperator(XposedBridge::class.java).run {
                    field("TAG")?.get(null)
                } ?: "Unknown"
                val apiVersion = XposedBridge.getXposedVersion()
                field("apiName")?.set(null, apiName)
                field("apiVersion")?.set(null, apiVersion)
            }
        }
    }

    private fun initFeatures(packageName: String, initializer: (feature: Feature) -> Unit) {
        this.scopes.firstOrNull {
            it.packages.contains(packageName)
        }?.getFeatures()?.forEach {
            if (!prefs.getBoolean(Feature.enabledKeyOf(it.key), false)) return@forEach
            initializer(it)
        }
    }
}