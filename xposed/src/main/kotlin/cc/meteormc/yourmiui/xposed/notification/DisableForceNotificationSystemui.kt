package cc.meteormc.yourmiui.xposed.notification

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.NOTIFICATION,
    "@string/feature_notification_disable_force_notification_name",
    "@string/feature_notification_disable_force_notification_description"
)
@RequiredScope("com.android.systemui")
object DisableForceNotificationSystemui : Feature(
    key = "disable_force_notification",
    nameRes = R.string.feature_systemui_disable_force_notification_name,
    descriptionRes = R.string.feature_systemui_disable_force_notification_description,
    testEnvironmentRes = R.string.feature_systemui_disable_force_notification_test_environment
), FeatureHooker {
    override fun hook(packageName: String) {
        operator("miui.util.NotificationFilterHelper") {
            // modifier: public static | signature: isNotificationForcedFor(Landroid/content/Context;Ljava/lang/String;)Z
            method("isNotificationForcedFor")?.hookResult(false)
        }

        operator("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow") {
            // modifier: public static | signature: canBlock(Landroid/content/Context;Lcom/android/systemui/statusbar/notification/ExpandedNotification;)Z
            method("canBlock")?.hookResult(true)
        }
    }
}