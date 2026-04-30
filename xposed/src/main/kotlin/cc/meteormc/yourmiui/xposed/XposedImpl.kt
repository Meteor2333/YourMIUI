package cc.meteormc.yourmiui.xposed

import android.content.res.XResources
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.common.Scope

abstract class XposedScope : Scope {
    private val nameRes: Int?
    private val restartable: Boolean
    private val packages: Array<Pair<String, String?>>

    constructor(
        vararg packages: Pair<String, String?>,
        nameRes: Int? = null,
        restartable: Boolean = true
    ) {
        this.nameRes = nameRes
        this.restartable = restartable
        this.packages = arrayOf(*packages)
    }

    final override fun getNameRes() = this.nameRes

    final override fun isRestartable() = this.restartable

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