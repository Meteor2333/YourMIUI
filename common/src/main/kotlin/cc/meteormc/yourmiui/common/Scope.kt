package cc.meteormc.yourmiui.common

import java.io.Serializable

interface Scope : Serializable {
    fun getNameRes(): Int?

    fun isRestartable(): Boolean

    fun getPackages(): Array<Pair<String, String?>>

    fun getFeatures(): Iterable<Any>
}
