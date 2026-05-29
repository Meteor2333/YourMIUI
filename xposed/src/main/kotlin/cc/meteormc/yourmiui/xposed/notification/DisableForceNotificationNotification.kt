package cc.meteormc.yourmiui.xposed.notification

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.NOTIFICATION,
    "@string/feature_notification_disable_force_notification_name",
    "@string/feature_notification_disable_force_notification_description"
)
@RequiredScope("com.miui.notification")
object DisableForceNotificationNotification : FeatureHooker {
    override fun hook(packageName: String) {
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