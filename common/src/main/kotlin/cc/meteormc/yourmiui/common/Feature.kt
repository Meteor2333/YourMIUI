package cc.meteormc.yourmiui.common

import android.content.res.Resources
import java.io.Serializable

abstract class Feature(
    val key: String,
    val nameRes: Int,
    val descriptionRes: Int,
    val warningRes: Int? = null,
    val testEnvironmentRes: Int? = null,
    val originalAuthor: String? = null
) : Serializable {
    companion object {
        const val PREFERENCES_NAME = "features"

        fun enabledKeyOf(featureKey: String) = "pref_${featureKey}_enabled"

        fun optionKeyOf(featureKey: String, optionKey: String) = "pref_${featureKey}_option_${optionKey}"
    }

    @Transient
    lateinit var resources: Resources

    @Transient
    lateinit var classLoader: ClassLoader

    open fun onInitResources() {

    }

    open fun onLoadPackage() {

    }

    open fun getOptions(): List<Option<*>> = emptyList()
}