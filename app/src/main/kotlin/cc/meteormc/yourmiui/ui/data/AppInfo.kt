package cc.meteormc.yourmiui.ui.data

import android.graphics.Bitmap
import java.io.Serializable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Bitmap,
    val launchIntent: String? = null
) : Serializable