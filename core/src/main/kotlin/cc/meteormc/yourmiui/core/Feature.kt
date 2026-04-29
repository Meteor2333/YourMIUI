package cc.meteormc.yourmiui.core

import java.io.Serializable

interface Feature : Serializable {
    companion object {
        const val PREFERENCE_TAG = "features"

        fun enabledKeyOf(featureKey: String) = "pref_${featureKey}_enabled"

        fun optionKeyOf(featureKey: String, optionKey: String) = "pref_${featureKey}_option_${optionKey}"
    }

    fun getPreferenceKey(): String

    fun getNameRes(): Int

    fun getDescriptionRes(): Int

    fun getWarningRes(): Int?

    fun getTestEnvironmentRes(): Int?

    fun getOriginalAuthor(): String?

    fun getOptions(): Iterable<Any>
}