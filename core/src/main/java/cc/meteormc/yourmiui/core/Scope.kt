package cc.meteormc.yourmiui.core

interface Scope {
    fun getNameRes(): Int?

    fun getPackages(): Array<String>

    fun getFeatures(): Iterable<Feature>
}
