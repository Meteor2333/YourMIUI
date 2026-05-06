package cc.meteormc.yourmiui.xposed.notification

import android.content.ComponentName
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.data.RestartMethod
import cc.meteormc.yourmiui.xposed.notification.feature.DisableForceNotification

object Notification : Scope(
    "com.miui.notification"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisableForceNotification
        )
    }

    override fun getRestartMethod() = RestartMethod.ViaComponent(
        ComponentName("com.miui.notification", "miui.notification.management.activity.NotificationAppListActivity")
    )
}