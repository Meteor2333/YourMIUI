@file:Suppress("unused")

package cc.meteormc.yourmiui.xposed

import android.content.SharedPreferences
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
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.jetbrains.annotations.ApiStatus
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method

class XposedEntry : IXposedHookLoadPackage {
    private val scopes = listOf(
        Android,
        ContentExtension,
        Market,
        MMS,
        PackageInstaller,
        SecurityCenter,
        Settings,
        SuperWallpaper
    )

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        if (packageName == BuildConfig.APPLICATION_ID) {
            this.initBridge(lpparam.classLoader, Bridge::class.java.name)
        } else {
            this.scopes.firstOrNull { it.getPackages().contains(packageName) }?.init(lpparam)
        }
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

    abstract override fun getFeatures(): Iterable<Feature>

    open fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!packages.contains(lpparam.packageName)) {
            return
        }

        val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID, Feature.PREFERENCE_TAG)
        prefs.makeWorldReadable()
        prefs.reload()

        this.getFeatures()
            .filterIsInstance<XposedFeature>()
            .forEach {
                runCatching { it.initInternal(lpparam, prefs) }.onFailure { ex ->
                    val scopeName = this.javaClass.simpleName
                    val featureName = it.javaClass.simpleName
                    val stackTrace = Log.getStackTraceString(ex)
                    XposedBridge.log("[YourMIUI] Failed to initialize hook feature '$featureName' in scope '$scopeName':\n$stackTrace")
                }
            }
    }
}

abstract class XposedFeature(
    private val key: String,
    private val nameRes: Int,
    private val descriptionRes: Int,
    private val warningRes: Int? = null,
    private val testEnvironmentRes: Int? = null,
    private val originalAuthor: String? = null
) : Feature {
    private lateinit var classLoader: ClassLoader

    protected abstract fun init()

    @ApiStatus.Internal
    internal fun initInternal(lpparam: XC_LoadPackage.LoadPackageParam, prefs: SharedPreferences) {
        this.classLoader = lpparam.classLoader
        if (!prefs.getBoolean(Feature.enabledKeyOf(key), false)) return

        getOptions().filterIsInstance<XposedOption<Any>>().forEach {
            val key = Feature.optionKeyOf(this.getPreferenceKey(), it.getPreferenceKey())
            val value = prefs.getString(key, null)?.let { preference ->
                it.getType().deserializer(preference)
            } ?: it.getDefaultValue()
            it.onValueInit(value)
        }

        this.init()
    }

    final override fun getPreferenceKey() = this.key

    final override fun getNameRes() = this.nameRes

    final override fun getDescriptionRes() = this.descriptionRes

    final override fun getWarningRes() = this.warningRes

    final override fun getTestEnvironmentRes() = this.testEnvironmentRes

    final override fun getOriginalAuthor() = this.originalAuthor

    override fun getOptions(): Iterable<Option> = emptyList()

    protected fun <T : Any, R> helper(clazz: Class<T>, operate: ReflectOperator<T>.() -> R): R {
        return ReflectOperator(clazz).run(operate)
    }

    protected fun <R> helper(className: String, operate: ReflectOperator<Any>.() -> R): R? {
        val clazz = getClass(classLoader, className, false)
        return if (clazz != null) {
            @Suppress("UNCHECKED_CAST")
            ReflectOperator(clazz as Class<Any>).run(operate)
        } else {
            XposedBridge.log("[YourMIUI] Class not found: $className!")
            null
        }
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
    fun hookResult(result: Any?) {
        XposedBridge.hookMethod(member, XC_MethodReplacement.returnConstant(result))
    }

    fun hookDoNothing() {
        XposedBridge.hookMethod(member, XC_MethodReplacement.DO_NOTHING)
    }

    fun hookBefore(callback: (param: XC_MethodHook.MethodHookParam) -> Unit) {
        XposedBridge.hookMethod(
            member,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    callback(param)
                }
            }
        )
    }

    fun hookAfter(callback: (param: XC_MethodHook.MethodHookParam) -> Unit) {
        XposedBridge.hookMethod(
            member,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    callback(param)
                }
            }
        )
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