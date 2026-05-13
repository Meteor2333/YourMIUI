package cc.meteormc.yourmiui.xposed

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.annotation.RequiresApi
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.common.Scope
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
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

object XposedEntry {
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

    class Rovo89 : IXposedHookLoadPackage {
        override fun handleLoadPackage(resparam: XC_LoadPackage.LoadPackageParam) {
            onLoadPackage(
                resparam.packageName,
                resparam.classLoader,
                XSharedPreferences("cc.meteormc.yourmiui", Feature.PREFERENCES_NAME).apply {
                    makeWorldReadable()
                    reload()
                }
            )
        }
    }

    @Suppress("unused")
    class LSPosed : XposedModule {
        constructor() : super()

        constructor(
            base: XposedInterface,
            param: XposedModuleInterface.ModuleLoadedParam
        ) : super(base, param)

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
            onLoadPackage(
                param.packageName,
                param.defaultClassLoader,
                runCatching {
                    getRemotePreferences(Feature.PREFERENCES_NAME)
                }.recoverCatching {
                    @Suppress("DEPRECATION")
                    @SuppressLint("WorldReadableFiles")
                    getSharedPreferences(Feature.PREFERENCES_NAME, Context.MODE_WORLD_READABLE)
                }.getOrNull() ?: return
            )
        }
    }

    private fun onLoadPackage(packageName: String, classLoader: ClassLoader, preferences: SharedPreferences) {
        initFeatures(packageName, preferences) { scope, feature ->
            feature.classLoader = classLoader

            runCatching {
                feature.getOptions().forEach { option ->
                    val key = Feature.optionKeyOf(feature.key, option.key)
                    val value = preferences.getString(key, null)?.let { preference ->
                        option.type.deserializer(preference)
                    } ?: option.defaultValue
                    @Suppress("UNCHECKED_CAST")
                    (option as Option<Any>).onValueInit(value)
                }

                XposedBridge.log("[YourMIUI] Initializing feature '${feature.id}' in scope '${scope.id}'")
                feature.onLoadPackage()
            }.onFailure { exception ->
                XposedBridge.log(
                    "[YourMIUI] Failed to " +
                            "initialize feature '${feature.id}' " +
                            "in scope '${scope.id}':\n" +
                            Log.getStackTraceString(exception)
                )
            }

        }

        operator(Application::class.java) {
            method("attach")?.hookAfter {
                initHostBridge(classLoader, it.instance())
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
                val apiVersion = XposedBridge.getXposedVersion()
                val frameworkName = ReflectOperator(XposedBridge::class.java).run {
                    field("TAG")?.get(null)
                } ?: "Unknown"
                field("apiVersion")?.set(null, apiVersion)
                field("frameworkName")?.set(null, frameworkName)
            }
        }
    }

    private fun initFeatures(
        packageName: String,
        prefs: SharedPreferences,
        initializer: (scope: Scope, feature: Feature) -> Unit
    ) {
        val scope = this.scopes.firstOrNull {
            it.packages.contains(packageName)
        } ?: return
        scope.getFeatures()
            .filter { prefs.getBoolean(Feature.enabledKeyOf(it.key), false) }
            .forEach { initializer(scope, it) }
    }
}