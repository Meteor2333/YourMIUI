package cc.meteormc.yourmiui.core

interface Scope {
    fun getNameRes(): Int?

    fun isRestartable(): Boolean

    fun getPackages(): Array<String>

    fun getFeatures(): Iterable<Any>
}
