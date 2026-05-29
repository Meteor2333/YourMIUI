package cc.meteormc.yourmiui.api.data

import java.io.Serializable

data class AppInfo(
    val packageName: String,
    val label: String
) : Serializable {
    override fun equals(other: Any?) = other is AppInfo && packageName == other.packageName

    override fun hashCode() = packageName.hashCode()
}