package cc.meteormc.yourmiui.xposed.notification

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.notification.feature.DisableForceNotification

object Notification : XposedScope(
    "com.miui.notification" to "miui.notification.management.activity.NotificationAppListActivity"
) {
    override fun getFeatures(): List<XposedFeature> {
        return listOf(
            DisableForceNotification
        )
    }
}