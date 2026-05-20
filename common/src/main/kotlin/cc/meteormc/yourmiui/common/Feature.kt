package cc.meteormc.yourmiui.common

import java.io.Serializable

abstract class Feature : Serializable {
    val id: String = this.javaClass.simpleName

    companion object {
        const val PREFERENCES_NAME = "features"

        fun enabledKeyOf(featureKey: String) = "pref_${featureKey}_enabled"

        fun optionKeyOf(featureKey: String, optionKey: String) = "pref_${featureKey}_option_${optionKey}"
    }

    @Transient
    lateinit var classLoader: ClassLoader

    open fun onLoadPackage(packageName: String) {

    }

    open fun getOptions(): List<Option<*>> = emptyList()
}