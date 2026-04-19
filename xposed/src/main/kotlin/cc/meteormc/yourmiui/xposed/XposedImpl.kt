package cc.meteormc.yourmiui.xposed

import android.content.res.XResources
import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.core.Option
import cc.meteormc.yourmiui.core.Scope
import cc.meteormc.yourmiui.core.util.getClass
import de.robv.android.xposed.XposedBridge

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

    protected fun <T : Any> operator(clazz: Class<T>): ReflectOperator<T> {
        return ReflectOperator(clazz)
    }

    protected fun operator(className: String): ReflectOperator<Any>? {
        val clazz = getClass(classLoader, className, false)
        return if (clazz != null) {
            @Suppress("UNCHECKED_CAST")
            ReflectOperator(clazz as Class<Any>)
        } else {
            XposedBridge.log("[YourMIUI] Class not found: $className!")
            null
        }
    }

    protected fun <T : Any, R> operator(clazz: Class<T>, operator: ReflectOperator<T>.() -> R): R {
        return this.operator(clazz).run(operator)
    }

    protected fun <R> operator(className: String, operator: ReflectOperator<Any>.() -> R): R? {
        return this.operator(className)?.run(operator)
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