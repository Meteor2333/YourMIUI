@file:Suppress("unused")

package cc.meteormc.yourmiui.xposed

import android.content.res.XResources
import android.util.Log
import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.core.Option
import cc.meteormc.yourmiui.core.Scope
import cc.meteormc.yourmiui.core.bridge.Bridge
import cc.meteormc.yourmiui.core.util.compareParameterTypes
import cc.meteormc.yourmiui.core.util.getClass
import cc.meteormc.yourmiui.xposed.android.Android
import cc.meteormc.yourmiui.xposed.contentextension.ContentExtension
import cc.meteormc.yourmiui.xposed.market.Market
import cc.meteormc.yourmiui.xposed.mms.MMS
import cc.meteormc.yourmiui.xposed.packageinstaller.PackageInstaller
import cc.meteormc.yourmiui.xposed.securitycenter.SecurityCenter
import cc.meteormc.yourmiui.xposed.settings.Settings
import cc.meteormc.yourmiui.xposed.superwallpaper.SuperWallpaper
import cc.meteormc.yourmiui.xposed.systemadsolution.SystemAdSolution
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method

class XposedEntry : IXposedHookInitPackageResources, IXposedHookLoadPackage {
    private val scopes by lazy {
        listOf(
            Android,
            ContentExtension,
            Market,
            MMS,
            PackageInstaller,
            SecurityCenter,
            Settings,
            SuperWallpaper,
            SystemAdSolution
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

abstract class XposedScope : Scope {
    private val nameRes: Int?
    private val packages: Array<String>

    constructor(vararg packages: String, nameRes: Int? = null) {
        this.nameRes = nameRes
        this.packages = arrayOf(*packages)
    }

    final override fun getNameRes() = this.nameRes

    final override fun getPackages() = this.packages

    abstract override fun getFeatures(): Iterable<XposedFeature>
}

abstract class XposedFeature(
    private val key: String,
    private val nameRes: Int,
    private val descriptionRes: Int,
    private val warningRes: Int? = null,
    private val testEnvironmentRes: Int? = null,
    private val originalAuthor: String? = null
) : Feature {
    internal lateinit var classLoader: ClassLoader

    open fun onInitResources(resources: XResources) {

    }

    open fun onLoadPackage() {

    }

    final override fun getPreferenceKey() = this.key

    final override fun getNameRes() = this.nameRes

    final override fun getDescriptionRes() = this.descriptionRes

    final override fun getWarningRes() = this.warningRes

    final override fun getTestEnvironmentRes() = this.testEnvironmentRes

    final override fun getOriginalAuthor() = this.originalAuthor

    override fun getOptions(): Iterable<XposedOption<*>> = emptyList()

    protected fun <T : Any> helper(clazz: Class<T>): ReflectOperator<T> {
        return ReflectOperator(clazz)
    }

    protected fun helper(className: String): ReflectOperator<Any>? {
        val clazz = getClass(classLoader, className, false)
        return if (clazz != null) {
            @Suppress("UNCHECKED_CAST")
            ReflectOperator(clazz as Class<Any>)
        } else {
            XposedBridge.log("[YourMIUI] Class not found: $className!")
            null
        }
    }

    protected fun <T : Any, R> helper(clazz: Class<T>, operator: ReflectOperator<T>.() -> R): R {
        return this.helper(clazz).run(operator)
    }

    protected fun <R> helper(className: String, operator: ReflectOperator<Any>.() -> R): R? {
        return this.helper(className)?.run(operator)
    }
}

class XposedOption<T : Any>(
    private val key: String,
    private val nameRes: Int,
    private val summaryRes: Int,
    private val type: Option.Type<T>,
    private val defaultValue: T,
    val onValueInit: (value: T) -> Unit
) : Option {
    override fun getPreferenceKey() = this.key

    override fun getNameRes() = this.nameRes

    override fun getSummaryRes() = this.summaryRes

    override fun getType(): Option.Type<T> = this.type

    override fun getDefaultValue(): T = this.defaultValue
}

@Suppress("UNCHECKED_CAST")
class ReflectOperator<T : Any>(val delegate: Class<T>) {
    fun constructor(vararg paramTypes: Class<*>): ConstructorOps<T>? {
        return runCatching {
            ConstructorOps(delegate.getDeclaredConstructor(*paramTypes))
        }.onFailure {
            XposedBridge.log("[YourMIUI] Constructor not found: ${delegate.getName()}(${getParametersString(*paramTypes)})!")
        }.getOrNull()
    }

    fun constructors(): List<ConstructorOps<*>> {
        return delegate.constructors.map { ConstructorOps(it) }
    }

    fun declaredConstructors(): List<ConstructorOps<T>> {
        return (delegate.declaredConstructors as Array<Constructor<T>>).map { ConstructorOps(it) }
    }

    fun field(name: String): FieldOps<T>? {
        val field = findRecursive {
            runCatching { it.getDeclaredField(name) }.getOrNull()
        }

        return if (field != null) {
            FieldOps(field)
        } else {
            XposedBridge.log("[YourMIUI] Field not found: ${delegate.getName()}#$name!")
            null
        }
    }

    fun fields(type: Class<*>): List<FieldOps<T>> {
        val result = mutableListOf<FieldOps<T>>()
        var superClass: Class<*> = delegate
        do {
            for (field in superClass.declaredFields) {
                if (!type.isAssignableFrom(field.type)) continue
                result.add(FieldOps(field))
            }
        } while ((superClass.getSuperclass()?.also { superClass = it }) != null)
        return result
    }

    fun fields(): List<FieldOps<T>> {
        return delegate.fields.map { FieldOps(it) }
    }

    fun declaredFields(): List<FieldOps<T>> {
        return delegate.declaredFields.map { FieldOps(it) }
    }

    fun method(name: String, vararg paramTypes: Class<*>): MethodOps<T>? {
        var result: Method? = null
        findRecursive {
            runCatching { it.getDeclaredMethod(name, *paramTypes) }.getOrNull()?.let { dm -> return@findRecursive dm }
            for (method in it.getDeclaredMethods()) {
                // compare name and parameters
                if (method.name == name && (result == null || compareParameterTypes(
                        method.parameterTypes,
                        result!!.parameterTypes,
                        paramTypes
                    ) < 0)) {
                    result = method
                }
            }
            return@findRecursive null
        }?.let { result = it }

        return if (result != null) {
            MethodOps(result)
        } else {
            XposedBridge.log("[YourMIUI] Method not found: ${delegate.getName()}#$name(${getParametersString(*paramTypes)})!")
            null
        }
    }

    fun methods(): List<MethodOps<T>> {
        return delegate.methods.map { MethodOps(it) }
    }

    fun declaredMethods(): List<MethodOps<T>> {
        return delegate.declaredMethods.map { MethodOps(it) }
    }

    private fun getParametersString(vararg clazzes: Class<*>): String {
        return clazzes.joinToString(",") { it.getName() }
    }

    private fun <R> findRecursive(func: (Class<*>) -> R?): R? {
        var superClass: Class<*> = delegate
        do {
            func(superClass)?.let { return it }
        } while ((superClass.getSuperclass()?.also { superClass = it }) != null)
        return null
    }
}

abstract class HookableOps(private val member: Member) {
    fun hookResult(result: Any?): HookableOps {
        XposedBridge.hookMethod(member, XC_MethodReplacement.returnConstant(result))
        return this
    }

    fun hookDoNothing(): HookableOps {
        XposedBridge.hookMethod(member, XC_MethodReplacement.DO_NOTHING)
        return this
    }

    fun hookBefore(callback: (param: XC_MethodHook.MethodHookParam) -> Unit): HookableOps {
        XposedBridge.hookMethod(
            member,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    callback(param)
                }
            }
        )
        return this
    }

    fun hookAfter(callback: (param: XC_MethodHook.MethodHookParam) -> Unit): HookableOps {
        XposedBridge.hookMethod(
            member,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    callback(param)
                }
            }
        )
        return this
    }
}

class ConstructorOps<T : Any>(private val delegate: Constructor<T>) : HookableOps(delegate) {
    fun parameterTypes(): Array<Class<*>> = delegate.parameterTypes

    fun new(vararg args: Any?): T {
        return delegate.apply { isAccessible = true }.newInstance(*args)
    }
}

@Suppress("UNCHECKED_CAST")
class FieldOps<T : Any>(private val delegate: Field) {
    fun type(): Class<*> = delegate.type

    operator fun <R> get(obj: T?, returnType: Class<R>? = null): R? {
        return delegate.apply { isAccessible = true }[obj] as? R?
    }

    operator fun set(obj: T?, value: Any?): FieldOps<T> {
        delegate.apply { isAccessible = true }[obj] = value
        return this
    }
}

class MethodOps<T : Any>(private val delegate: Method) : HookableOps(delegate) {
    fun parameterTypes(): Array<Class<*>> = delegate.parameterTypes

    fun returnType(): Class<*> = delegate.returnType

    fun call(obj: T?, vararg args: Any?): Any? {
        return delegate.apply { isAccessible = true }.invoke(obj, *args)
    }

    fun callSuper(obj: T?, vararg args: Any?): Any? {
        return XposedBridge.invokeOriginalMethod(delegate, obj, args)
    }
}