package cc.meteormc.yourmiui.xposed

import android.util.Log
import cc.meteormc.yourmiui.core.Bridge
import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.core.Scope
import cc.meteormc.yourmiui.core.util.compareParameterTypes
import cc.meteormc.yourmiui.core.util.getClass
import cc.meteormc.yourmiui.xposed.android.Android
import cc.meteormc.yourmiui.xposed.market.Market
import cc.meteormc.yourmiui.xposed.packageinstaller.PackageInstaller
import cc.meteormc.yourmiui.xposed.securitycenter.SecurityCenter
import cc.meteormc.yourmiui.xposed.settings.Settings
import cc.meteormc.yourmiui.xposed.superwallpaper.SuperWallpaper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

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

        fun log(message: String) {
            XposedBridge.log("[YourMIUI] $message")
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        if (this.javaClass.name.startsWith(packageName)) {
            val bridgeClass = getClass(lpparam.classLoader, Bridge::class.java.name, true) ?: return
            XposedFeature.ReflectHelper(bridgeClass).operate {
                method("getApiName")?.hook(XC_MethodReplacement.returnConstant(
                    XposedFeature.ReflectHelper(XposedBridge::class.java).operate {
                        field("TAG")?.get(null, String::class.java)
                    }
                ))
                method("getApiVersion")?.hook(XC_MethodReplacement.returnConstant(XposedBridge.getXposedVersion()))
                method("isModuleActivated")?.hook(XC_MethodReplacement.returnConstant(true))
                method("getScopes")?.hook(XC_MethodReplacement.returnConstant(scopes))
            }
        } else {
            packageToScope[packageName]?.init(lpparam)
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

    final override fun getNameRes(): Int? {
        return this.nameRes
    }

    final override fun getPackages(): Array<String> {
        return this.packages
    }

    abstract override fun getFeatures(): Iterable<Feature>

    open fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!packages.contains(lpparam.packageName)) {
            return
        }

        val scopeName = this.javaClass.simpleName
        this.getFeatures()
            .filterIsInstance<XposedFeature>()
            .forEach {
                runCatching { it.initInternal(lpparam) }.onFailure { ex ->
                    val featureName = it.javaClass.simpleName
                    val stackTrace = Log.getStackTraceString(ex)
                    XposedEntry.log("Failed to initialize hook feature '$featureName' in scope '$scopeName':\n$stackTrace")
                }
            }
    }
}

@Suppress("unused")
abstract class XposedFeature(
    private val key: String,
    private val nameRes: Int,
    private val descriptionRes: Int,
    private val warningRes: Int? = null,
    private val testEnvironmentRes: Int? = null,
    private val originalAuthor: String? = null
) : Feature {
    private lateinit var classLoader: ClassLoader

    fun initInternal(lpparam: XC_LoadPackage.LoadPackageParam) {
        this.classLoader = lpparam.classLoader
        init()
    }

    protected abstract fun init()

    override fun getPreferenceKey(): String {
        return this.key
    }

    override fun getNameRes(): Int {
        return this.nameRes
    }

    override fun getDescriptionRes(): Int {
        return this.descriptionRes
    }

    override fun getWarningRes(): Int? {
        return this.warningRes
    }

    override fun getTestEnvironmentRes(): Int? {
        return this.testEnvironmentRes
    }

    override fun getOriginalAuthor(): String? {
        return this.originalAuthor
    }

    protected fun <T : Any> helper(clazz: Class<T>) = ReflectHelper(clazz)

    protected fun helper(className: String): ReflectHelper<Any>? {
        val clazz = getClass(classLoader, className, false)
        return if (clazz != null) {
            @Suppress("UNCHECKED_CAST")
            ReflectHelper(clazz as Class<Any>)
        } else {
            null
        }
    }

    class ReflectHelper<T : Any>(val delegate: Class<T>) {
        fun <R> operate(block: ReflectOperator<T>.() -> R): R {
            return ReflectOperator(delegate).run(block)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class ReflectOperator<T : Any>(val delegate: Class<T>) {
        companion object {
            private val constructorCache = mutableMapOf<String, ConstructorOps<*>>()
            private val fieldCache = mutableMapOf<String, FieldOps<*>>()
            private val methodCache = mutableMapOf<String, MethodOps<*>>()
        }

        fun constructor(vararg paramTypes: Class<*>): ConstructorOps<T>? {
            val fullName = "${delegate.getName()}${getParametersString(*paramTypes)}"
            if (constructorCache.containsKey(fullName)) {
                return constructorCache[fullName] as ConstructorOps<T>
            }

            return runCatching {
                val constructor = ConstructorOps(delegate.getDeclaredConstructor(*paramTypes))
                constructorCache[fullName] = constructor
                constructor
            }.onFailure {
                XposedEntry.log("Constructor not found: $fullName!")
            }.getOrNull()
        }

        fun constructors(): List<ConstructorOps<*>> {
            return delegate.constructors.map { ConstructorOps(it) }
        }

        fun declaredConstructors(): List<ConstructorOps<T>> {
            return (delegate.declaredConstructors as Array<Constructor<T>>).map { ConstructorOps(it) }
        }

        fun field(name: String): FieldOps<T>? {
            val fullName = "${delegate.getName()}#$name"
            if (fieldCache.containsKey(fullName)) {
                return fieldCache[fullName] as FieldOps<T>
            }

            val field = findRecursive {
                runCatching { it.getDeclaredField(name) }.getOrNull()
            }?.let { FieldOps<T>(it) }
            if (field == null) {
                XposedEntry.log("Field not found: $fullName!")
                return null
            }

            fieldCache[fullName] = field
            return field
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
            val fullName = "${delegate.getName()}#$name${getParametersString(*paramTypes)}"
            if (methodCache.containsKey(fullName)) {
                return methodCache[fullName] as MethodOps<T>
            }

            var result: Method? = null
            findRecursive {
                runCatching { it.getDeclaredMethod(name) }.getOrNull()?.let { dm -> return@findRecursive dm }
                for (method in it.getDeclaredMethods()) {
                    // compare name and parameters
                    if (method.name == name && (result == null || compareParameterTypes(
                            method.parameterTypes,
                            result!!.parameterTypes,
                            paramTypes
                        ) < 0)) {
                        // get accessible version of method
                        result = method
                    }
                }
                return@findRecursive null
            }?.let { result = it }

            return if (result != null) {
                val method = MethodOps<T>(result)
                methodCache[fullName] = method
                method
            } else {
                XposedEntry.log("Method not found: $fullName!")
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
            return "(${clazzes.joinToString(",") { it.getName() }})"
        }

        private fun <R> findRecursive(func: (Class<*>) -> R?): R? {
            var superClass: Class<*> = delegate
            do {
                func(superClass)?.let { return it }
            } while ((superClass.getSuperclass()?.also { superClass = it }) != null)
            return null
        }
    }

    class ConstructorOps<T : Any>(private val delegate: Constructor<T>) {
        fun parameterTypes(): Array<Class<*>> = delegate.parameterTypes

        fun new(vararg args: Any?): T {
            return delegate.apply { isAccessible = true }.newInstance(*args)
        }

        fun hook(callback: XC_MethodHook): ConstructorOps<T> {
            XposedBridge.hookMethod(delegate, callback)
            return this
        }
    }

    @Suppress("UNCHECKED_CAST")
    class FieldOps<T : Any>(private val delegate: Field) {
        fun type(): Class<*> = delegate.type

        operator fun <R> get(obj: T?, returnType: Class<R>): R? {
            return delegate.apply { isAccessible = true }[obj] as R?
        }

        operator fun set(obj: T?, value: Any?): FieldOps<T> {
            delegate.apply { isAccessible = true }[obj] = value
            return this
        }
    }

    class MethodOps<T : Any>(private val delegate: Method) {
        fun parameterTypes(): Array<Class<*>> = delegate.parameterTypes

        fun returnType(): Class<*> = delegate.returnType

        fun call(obj: T?, vararg args: Any?): Any? {
            return delegate.apply { isAccessible = true }.invoke(obj, *args)
        }

        fun callSuper(obj: T?, vararg args: Any?): Any? {
            return XposedBridge.invokeOriginalMethod(delegate, obj, args)
        }

        fun hook(callback: XC_MethodHook): MethodOps<T> {
            XposedBridge.hookMethod(delegate, callback)
            return this
        }
    }
}