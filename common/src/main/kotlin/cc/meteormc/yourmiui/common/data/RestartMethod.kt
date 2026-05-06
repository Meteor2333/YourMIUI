package cc.meteormc.yourmiui.common.data

import android.content.ComponentName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
sealed class RestartMethod : Serializable, Parcelable {
    object Reboot : RestartMethod()

    object DoNothing : RestartMethod()

    class ViaComponent(vararg val components: ComponentName) : RestartMethod()
}