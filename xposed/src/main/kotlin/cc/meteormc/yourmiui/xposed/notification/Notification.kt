package cc.meteormc.yourmiui.xposed.notification

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.xposed.notification.feature.DisableForceNotification

object Notification : Scope(
    "com.miui.notification" to "miui.notification.management.activity.NotificationAppListActivity"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisableForceNotification
        )
    }
}