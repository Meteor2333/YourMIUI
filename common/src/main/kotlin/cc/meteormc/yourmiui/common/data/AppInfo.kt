package cc.meteormc.yourmiui.common.data

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Bitmap
) : Serializable, Parcelable {
    override fun equals(other: Any?) = other is AppInfo && packageName == other.packageName

    override fun hashCode() = packageName.hashCode()
}