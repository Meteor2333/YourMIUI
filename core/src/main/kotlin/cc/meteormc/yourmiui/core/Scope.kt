package cc.meteormc.yourmiui.core

import java.io.Serializable

interface Scope : Serializable {
    fun getNameRes(): Int?

    fun isRestartable(): Boolean

    fun getPackages(): Array<String>

    fun getFeatures(): Iterable<Any>
}
