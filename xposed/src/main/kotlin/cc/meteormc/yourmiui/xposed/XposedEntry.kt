package cc.meteormc.yourmiui.xposed

import android.app.Application
import android.os.Process
import android.util.Log
import cc.meteormc.yourmiui.FeatureRegistry
import cc.meteormc.yourmiui.api.FeatureHooker
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
        val packageName = lpparam.packageName
        val context = HookContext(
            lpparam.appInfo?.uid ?: 0,
            packageName,
            lpparam.processName,
            classLoader
        )

        val hookers: List<FeatureHooker>? = FeatureRegistry.features[packageName]
        val features = hookers?.map { FeatureInfo.fromHooker(it) } ?: emptyList()
        features.forEach { syncFeature(context, it) }

        Application::class.reflect.method("attach")?.hookAfter { param ->
            Host(param.instance()).register(Bridge.FORCE_STOP_CHANNEL) {
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

    private fun syncFeature(context: HookContext, feature: FeatureInfo) {
        val prefs = prefs.getFeature(feature)
        if (prefs.enabled) {
            updateOptions(feature, prefs)
            hookFeature(context, feature)
        } else {
            unhookFeature(context, feature)
        }
    }

    private fun hookFeature(context: HookContext, feature: FeatureInfo) {
        val hooker = feature.hooker
        if (context.isHooked(hooker)) return

        runCatching {
            hooker.hook(context)
            context.hooked.add(hooker)
        }.onFailure { exception ->
            XposedBridge.log(
                "[YourMIUI] Failed to " +
                        "hook feature '${feature.javaClass.simpleName}' " +
                        "in package '${feature.category}':\n" +
                        Log.getStackTraceString(exception)
            )
        }
    }

    private fun unhookFeature(context: HookContext, feature: FeatureInfo) {
        val hooker = feature.hooker
        if (!context.isHooked(hooker)) return

        runCatching {
            hooker.unhook()
            context.hooked.remove(hooker)
        }.onFailure {
            XposedBridge.log(
                "[YourMIUI] Failed to " +
                        "unhook feature '${feature.javaClass.simpleName}' " +
                        "in package '${feature.category}':\n" +
                        Log.getStackTraceString(it)
            )
        }
    }

    private fun updateOptions(feature: FeatureInfo, preferences: SharedPreferences.Feature) {
        feature.options.forEach { option ->
            val source = option.source
            val type = option.type
            val value = when (type) {
                is OptionType.App -> preferences.getOption(option.key, type) ?: type.defaultPackages
                is OptionType.List -> preferences.getOption(option.key, type) ?: type.defaultOptions
                is OptionType.Switch -> preferences.getOption(option.key, type) ?: type.defaultValue
                is OptionType.Text -> preferences.getOption(option.key, type) ?: type.defaultText
            }

            if (!PrimitiveUtil.canAssign(value.javaClass, source.type)) {
                throw IllegalStateException(
                    "Option '${option.key}' in feature '${feature.javaClass.simpleName}' " +
                            "expects a value of type ${source.type}, but got ${value.javaClass}"
                )
            }

            val instance = if (Modifier.isStatic(source.modifiers)) null
            else SingletonUtil.getInstance(feature.source)
            source[instance] = value
        }
    }
}