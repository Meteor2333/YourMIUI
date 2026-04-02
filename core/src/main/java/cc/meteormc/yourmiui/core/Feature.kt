package cc.meteormc.yourmiui.core

interface Feature {
    fun getPreferenceKey(): String

    fun getNameRes(): Int

    fun getDescriptionRes(): Int

    fun getWarningRes(): Int?

    fun getTestEnvironmentRes(): Int?

    fun getOriginalAuthor(): String?
}