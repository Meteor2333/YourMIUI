package cc.meteormc.yourmiui.common

import cc.meteormc.yourmiui.common.data.RestartMethod
import java.io.Serializable

abstract class Scope(
    val nameRes: Int?,
    val packages: Array<String>
) : Serializable {
    constructor(
        vararg packages: String,
        nameRes: Int? = null
    ) : this(
        nameRes,
        arrayOf(*packages)
    )

    abstract fun getFeatures(): List<Feature>

    abstract fun getRestartMethod(): RestartMethod
}