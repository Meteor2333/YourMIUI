package cc.meteormc.yourmiui.xposed.notification

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

object DisableForceNotificationNotification : Feature(
    key = "disable_force_notification",
    nameRes = R.string.feature_notification_disable_force_notification_name,
    descriptionRes = R.string.feature_notification_disable_force_notification_description,
    testEnvironmentRes = R.string.feature_notification_disable_force_notification_test_environment
) {
    override fun onLoadPackage() {
        operator("miui.util.NotificationFilterHelper") {
            // modifier: public static | signature: isNotificationForcedFor(Landroid/content/Context;Ljava/lang/String;)Z
            method("isNotificationForcedFor")?.hookResult(false)
        }

        operator("miui.notification.management.model.AppItem") {
            // modifier: public | signature: isSystemApp()Z
            method("isSystemApp")?.hookResult(false)
        }
    }
}