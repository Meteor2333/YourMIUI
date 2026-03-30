package cc.meteormc.yourmiui.xposed

import android.util.Log
import cc.meteormc.yourmiui.core.Bridge
import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.core.Scope
import cc.meteormc.yourmiui.xposed.android.Android
import cc.meteormc.yourmiui.xposed.market.Market
import cc.meteormc.yourmiui.xposed.packageinstaller.PackageInstaller
import cc.meteormc.yourmiui.xposed.securitycenter.SecurityCenter
import cc.meteormc.yourmiui.xposed.settings.Settings
import cc.meteormc.yourmiui.xposed.superwallpaper.SuperWallpaper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Proxy

class XposedEntry : IXposedHookLoadPackage {
    companion object {
        private val scopes = listOf(
            Android,
            Market,
            PackageInstaller,
            SecurityCenter,
            Settings,
            SuperWallpaper
        )
        private val packageToScope = scopes.flatMap { scope ->
            scope.getPackages().map { it to scope }
        }.toMap()

        fun log(message: String, debug: Boolean = false) {
            if (!debug || BuildConfig.DEBUG) {
                XposedBridge.log("[YourMIUI] $message")
            }
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        if (packageName == BuildConfig.PACKAGE_NAME) {
            ReflectHelper.of(Bridge::class.java.name, lpparam.classLoader)?.operate {
                method("getApiName")?.hook(XC_MethodReplacement.returnConstant(
                    ReflectHelper.fromJava(XposedBridge::class.java).operate {
                        field("TAG")?.get(null) as String?
                    }
                ))
                method("getApiVersion")?.hook(XC_MethodReplacement.returnConstant(XposedBridge.getXposedVersion()))
                method("isModuleActivated")?.hook(XC_MethodReplacement.returnConstant(true))
                method("getScopes")?.hook(object : XC_MethodReplacement() {
                    // 由于这里的ClassLoader与调用方的ClassLoader不同
                    // 直接返回对象会导致ClassCastException
                    // 所以使用Proxy来优雅地解决这个问题
                    override fun replaceHookedMethod(param: MethodHookParam): Any {
                        // 获取调用方期望的接口类型（此类被调用方的ClassLoader加载）
                        val interfaceClass = param.args[0] as Class<*>
                        // 为真实对象创建Proxy（这些类都被这里的ClassLoader加载）
                        val proxies = scopes.map { target ->
                            // 使用调用方的ClassLoader创建代理
                            // 让Proxy"看起来"好像实现了这个接口
                            Proxy.newProxyInstance(
                                interfaceClass.classLoader,
                                arrayOf(interfaceClass)
                            ) { _, method, args ->
                                // 注意:
                                // `method`是被调用方ClassLoader加载的方法
                                // `target`是被这里ClassLoader加载的对象
                                // 二者互相不认识 所以不能直接 method.invoke(target)
                                // 必须从method找到target对应的真实方法并调用
                                target.javaClass.getMethod(
                                    method.name,
                                    *method.parameterTypes
                                ).invoke(
                                    target,
                                    *(args ?: emptyArray())
                                )
                            }
                        }

                        return proxies.toTypedArray()
                    }
                })
            }
        }

        packageToScope[packageName]?.init(lpparam)
    }
}

abstract class XposedScope : Scope {
    private val packages: Array<String>

    constructor(vararg packages: String) {
        this.packages = arrayOf(*packages)
    }

    final override fun getPackages(): Array<String> {
        return this.packages
    }

    open fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!packages.contains(lpparam.packageName)) {
            return
        }

        val scopeName = this.javaClass.simpleName
        XposedEntry.log("Initializing scope '$scopeName'", true)
        this.getFeatures()
            .filterIsInstance<HookFeature>()
            .forEach {
                val featureName = it.javaClass.simpleName
                try {
                    it.init(lpparam)
                } catch (t: Throwable) {
                    val stackTrace = Log.getStackTraceString(t)
                    XposedEntry.log("Failed to initialize hook feature '$featureName' in scope '$scopeName':\n$stackTrace", true)
                }
            }
        XposedEntry.log("Initialized scope '$scopeName'", true)
    }
}

abstract class TriggerFeature(
    name: String,
    description: String,
    warning: String? = null,
    testEnvironment: String? = null,
    originalAuthor: String? = null
) : Feature(
    name,
    description,
    warning,
    testEnvironment,
    originalAuthor
)

abstract class HookFeature(
    name: String,
    description: String,
    warning: String? = null,
    testEnvironment: String? = null,
    originalAuthor: String? = null
) : Feature(
    name,
    description,
    warning,
    testEnvironment,
    originalAuthor
) {
    abstract fun init(lpparam: XC_LoadPackage.LoadPackageParam)
}

abstract class ConfigurableHookFeature(
    name: String,
    description: String,
    warning: String? = null,
    testEnvironment: String? = null,
    originalAuthor: String? = null
) : HookFeature(
    name,
    description,
    warning,
    testEnvironment,
    originalAuthor
)