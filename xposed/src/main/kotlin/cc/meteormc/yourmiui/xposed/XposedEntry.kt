package cc.meteormc.yourmiui.xposed

import android.util.Log
import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.core.bridge.Bridge
import cc.meteormc.yourmiui.core.util.getClass
import cc.meteormc.yourmiui.xposed.android.Android
import cc.meteormc.yourmiui.xposed.contentextension.ContentExtension
import cc.meteormc.yourmiui.xposed.market.Market
import cc.meteormc.yourmiui.xposed.mms.MMS
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
            MMS,
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
            this.initBridge(lpparam.classLoader, Bridge::class.java.name)
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
    }

    private fun findScope(packageName: String): XposedScope? {
        return this.scopes.firstOrNull { it.getPackages().contains(packageName) }
    }

    private fun initBridge(classLoader: ClassLoader, className: String) {
        val bridgeClass = getClass(classLoader, className, true) ?: return
        ReflectOperator(bridgeClass).run {
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