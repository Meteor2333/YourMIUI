package cc.meteormc.yourmiui.xposed

import android.app.Application
import android.content.SharedPreferences
import android.os.Process
import android.util.Log
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.common.bridge.Bridge
import cc.meteormc.yourmiui.common.bridge.Host
import cc.meteormc.yourmiui.common.util.Unsafe.cast
import cc.meteormc.yourmiui.common.util.getClass
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

object XposedEntry {
    private val hostBridge = Host()

    class Rovo89 : IXposedHookLoadPackage {
        override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
            val bridgeClass = getBridgeClass(lpparam.classLoader)
            if (bridgeClass != null) {
                // 当存在桥接类时 则说明当前package为模块本身 可以进行初始化
                onModuleLoaded()
                operator(bridgeClass) {
                    val apiVersion = XposedBridge.getXposedVersion()
                    val frameworkName = ReflectOperator(XposedBridge::class.java).run {
                        field("TAG")?.get(null)
                    } ?: "Unknown"
                    field("apiVersion")?.set(null, apiVersion)
                    field("frameworkName")?.set(null, frameworkName)
                }
            } else {
                onPackageLoaded(
                    lpparam.packageName,
                    lpparam.classLoader,
                    XSharedPreferences("cc.meteormc.yourmiui", Feature.PREFERENCES_NAME).apply {
                        makeWorldReadable()
                        reload()
                    }
                )
            }
        }

        private fun getBridgeClass(classLoader: ClassLoader): Class<*>? {
            return getClass(classLoader, Bridge::class.java.name, true)
        }
    }

    class LSPosed : XposedModule {
        constructor() : super()

        constructor(base: XposedInterface, param: XposedModuleInterface.ModuleLoadedParam) : super(base, param) {
            onModuleLoaded(param)
        }

        override fun onModuleLoaded(param: XposedModuleInterface.ModuleLoadedParam) {
            XposedBridge.log("[YourMIUI] Module loaded: ${param.processName}")
            onModuleLoaded()
        }

        override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
            if (!param.isFirstPackage) return
            onPackageLoaded(
                param.packageName,
                param.defaultClassLoader,
                getRemotePreferences(Feature.PREFERENCES_NAME)
            )
        }
    }

    private fun onModuleLoaded() {
        hostBridge.register(Bridge.GET_SCOPES_CHANNEL) {
            scopes.toCollection(ArrayList())
        }
        this.onLoadFinished()
    }

    private fun onPackageLoaded(packageName: String, classLoader: ClassLoader, preferences: SharedPreferences) {
        hostBridge.register(Bridge.RESTART_SCOPE_CHANNEL) {
            Thread {
                Thread.sleep(300)
                Process.killProcess(Process.myPid())
            }.start()
        }

        val scope = this.scopes.firstOrNull {
            it.packages.contains(packageName)
        } ?: return
        scope.getFeatures()
            .filter { preferences.getBoolean(Feature.enabledKeyOf(it.key), false) }
            .forEach { feature ->
                feature.classLoader = classLoader

                runCatching {
                    feature.getOptions().forEach { option ->
                        val key = Feature.optionKeyOf(feature.key, option.key)
                        val value = preferences.getString(key, null)?.let { preference ->
                            option.type.deserializer(preference)
                        } ?: option.defaultValue
                        option.cast<Option<Any>>().onValueInit(value)
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
        this.onLoadFinished()
    }

    private fun onLoadFinished() {
        operator(Application::class.java) {
            method("attach")?.hookAfter {
                hostBridge.attach(it.instance())
            }
        }
    }
}