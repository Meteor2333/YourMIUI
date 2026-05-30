package cc.meteormc.yourmiui.xposed

import android.app.Application
import android.content.Context
import android.os.Process
import android.util.Log
import cc.meteormc.yourmiui.FeatureRegistry
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.EntryClass
import cc.meteormc.yourmiui.api.util.ClassUtil
import cc.meteormc.yourmiui.api.util.PrefsUtil
import cc.meteormc.yourmiui.api.util.SingletonUtil
import cc.meteormc.yourmiui.common.bridge.Bridge
import cc.meteormc.yourmiui.common.bridge.Host
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Modifier

@EntryClass
class XposedEntry : IXposedHookLoadPackage {
    companion object {
        lateinit var INSTANCE: XposedEntry
            private set
    }

    init {
        INSTANCE = this
    }

    lateinit var hostBridge: Host
    lateinit var classLoader: ClassLoader

    private val prefs by lazy {
        XSharedPreferences("cc.meteormc.yourmiui", PrefsUtil.SHARED_PREFERENCES_NAME).apply {
            makeWorldReadable()
            reload()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        classLoader = lpparam.classLoader

        val packageName = lpparam.packageName
        initFeatures(lpparam.packageName) {
            runCatching {
                operator(it.javaClass) {
                    for (field in declaredFields()) {
                        // todo
                        val value = null

                        val instance = if (Modifier.isStatic(field.modifiers)) {
                            null
                        } else {
                            SingletonUtil.getInstance(delegate) as? FeatureHooker?
                        }
                        field[instance] = value
                    }
                }

//                feature.getOptions().forEach { option ->
//                    val key = Feature.optionKeyOf(feature.key, option.key)
//                    val value = prefs.getString(key, null)?.let { preference ->
//                        option.type.deserializer(preference)
//                    } ?: option.defaultValue
//                    (option as Option<Any>).onValueInit(value)
//                }

                it.hook(packageName)
            }.onFailure { exception ->
                XposedBridge.log(
                    "[YourMIUI] Failed to " +
                            "initialize feature '${it.javaClass.simpleName}' " +
                            "in package '${packageName}':\n" +
                            Log.getStackTraceString(exception)
                )
            }

        }

        operator(Application::class.java) {
            method("attach")?.hookAfter {
                initHostBridge(it.instance())
            }
        }
    }

    private fun initHostBridge(context: Context) {
        hostBridge = Host(context)
        hostBridge.register(Bridge.RESTART_SCOPE_CHANNEL) {
            Thread {
                Thread.sleep(300)
                Process.killProcess(Process.myPid())
            }.start()
        }.attach()

        val bridgeClass = ClassUtil.getClass(classLoader, Bridge::class.java.name, true)
        if (bridgeClass != null) {
            operator(bridgeClass) {
                val apiName = Reflect(XposedBridge::class.java).run {
                    field("TAG")?.get(null)
                } ?: "Unknown"
                val apiVersion = XposedBridge.getXposedVersion()
                field("apiName")?.set(null, apiName)
                field("apiVersion")?.set(null, apiVersion)
            }
        }
    }

    private fun initFeatures(packageName: String, initializer: (hooker: FeatureHooker) -> Unit) {
        FeatureRegistry.features[packageName]
            ?.filter { prefs.getBoolean(PrefsUtil.getFeatureEnabledKey(it.javaClass.simpleName), false) }
            ?.forEach { initializer(it) }
    }
}