package cc.meteormc.yourmiui.core

abstract class Feature(
    val name: String,
    val description: String,
    val warning: String? = null,
    val testEnvironment: String? = null,
    val originalAuthor: String? = null
)