package cc.meteormc.yourmiui.xposed

import android.app.Application
import android.content.Context
import android.os.Process
import android.util.Log
import cc.meteormc.yourmiui.FeatureRegistry
import cc.meteormc.yourmiui.api.OptionType
import cc.meteormc.yourmiui.api.annotation.EntryClass
import cc.meteormc.yourmiui.api.data.FeatureInfo
import cc.meteormc.yourmiui.api.data.HookContext
import cc.meteormc.yourmiui.api.util.ClassUtil
import cc.meteormc.yourmiui.api.util.PrimitiveUtil
import cc.meteormc.yourmiui.api.util.SingletonUtil
import cc.meteormc.yourmiui.common.bridge.Bridge
import cc.meteormc.yourmiui.common.bridge.Host
import cc.meteormc.yourmiui.common.prefs.SharedPreferences
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Modifier

@EntryClass
class XposedEntry : IXposedHookLoadPackage {
    lateinit var hostBridge: Host
    private val prefs by lazy {
        SharedPreferences(
            XSharedPreferences(
                "cc.meteormc.yourmiui",
                SharedPreferences.SHARED_PREFERENCES_NAME
            ).apply {
                makeWorldReadable()
                reload()
            }
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val classLoader = lpparam.classLoader
        val context = HookContext(
            lpparam.appInfo.uid,
            lpparam.packageName,
            lpparam.processName,
            lpparam.classLoader
        )

        val packageName = lpparam.packageName
        FeatureRegistry.features[packageName]
            ?.map { FeatureInfo.fromHooker(it) }
            ?.forEach {
                val prefs = prefs.getFeature(it)
                if (!prefs.enabled) return@forEach

                runCatching {
                    it.options.forEach { option ->
                        val source = option.source
                        val type = option.type
                        val value = when (type) {
                            is OptionType.App -> prefs.getOption(option.key, type) ?: type.defaultPackages
                            is OptionType.List -> prefs.getOption(option.key, type) ?: type.defaultOptions
                            is OptionType.Switch -> prefs.getOption(option.key, type) ?: type.defaultValue
                            is OptionType.Text -> prefs.getOption(option.key, type) ?: type.defaultText
                        }

                        if (!PrimitiveUtil.canAssign(value.javaClass, source.type)) {
                            throw IllegalStateException(
                                "Option '${option.key}' in feature '${it.javaClass.simpleName}' " +
                                        "expects a value of type ${source.type}, but got ${value.javaClass}"
                            )
                        }

                        val instance = if (Modifier.isStatic(source.modifiers)) null
                        else SingletonUtil.getInstance(it.source)
                        source[instance] = value
                    }

                    it.hooker.hook(context)
                }.onFailure { exception ->
                    XposedBridge.log(
                        "[YourMIUI] Failed to " +
                                "initialize feature '${it.javaClass.simpleName}' " +
                                "in package '${packageName}':\n" +
                                Log.getStackTraceString(exception)
                    )
                }
            }

        Application::class.reflect.method("attach")?.hookAfter {
            initHostBridge(it.instance(), classLoader)
        }
    }

    private fun initHostBridge(context: Context, classLoader: ClassLoader) {
        hostBridge = Host(context)
        hostBridge.register(Bridge.FORCE_STOP_CHANNEL) {
            Thread {
                Thread.sleep(300)
                Process.killProcess(Process.myPid())
            }.start()
        }.attach()

        ClassUtil.getClass(classLoader, Bridge::class.java.name, true)?.reflect {
            val apiName = Reflect(XposedBridge::class.java).run {
                field("TAG")?.get(null)
            } ?: "Unknown"
            val apiVersion = XposedBridge.getXposedVersion()
            field("apiName")?.set(null, apiName)
            field("apiVersion")?.set(null, apiVersion)
        }
    }
}