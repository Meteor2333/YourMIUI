package cc.meteormc.yourmiui.common

import cc.meteormc.yourmiui.common.data.RestartMethod
import java.io.Serializable

abstract class Scope(
    val nameRes: Int?,
    val packages: Array<out String>
) : Serializable {
    constructor(
        vararg packages: String,
        nameRes: Int? = null
    ) : this(nameRes, packages)

    abstract fun getFeatures(): List<Feature>

    abstract fun getRestartMethod(): RestartMethod
}