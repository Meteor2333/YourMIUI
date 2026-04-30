package cc.meteormc.yourmiui.ui.data

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Bitmap,
    val launchIntent: String? = null
) : Parcelable