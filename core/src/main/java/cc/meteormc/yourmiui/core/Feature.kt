package cc.meteormc.yourmiui.core

abstract class Feature(
    val nameRes: Int,
    val descriptionRes: Int,
    val warningRes: Int?,
    val testEnvironmentRes: Int?,
    val originalAuthor: String?
)