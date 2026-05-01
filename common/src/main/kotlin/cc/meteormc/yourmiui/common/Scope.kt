package cc.meteormc.yourmiui.common

import java.io.Serializable

abstract class Scope(
    val nameRes: Int?,
    val restartable: Boolean,
    val packages: Array<Pair<String, String?>>
) : Serializable {
    constructor(
        vararg packages: Pair<String, String?>,
        nameRes: Int? = null,
        restartable: Boolean = true
    ) : this(
        nameRes,
        restartable,
        arrayOf(*packages)
    )

    abstract fun getFeatures(): List<Feature>
}