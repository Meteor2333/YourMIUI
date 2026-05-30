package cc.meteormc.yourmiui.xposed.notification

import android.content.pm.PermissionInfo
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.xposed.get
import cc.meteormc.yourmiui.xposed.hookResult
import cc.meteormc.yourmiui.xposed.operator
import cc.meteormc.yourmiui.xposed.replaceResult

@FeatureRegister(
    Category.NOTIFICATION,
    "@string/feature_notification_disable_force_notification_name",
    "@string/feature_notification_disable_force_notification_description"
)
@RequiredScope("android")
object DisableForceNotificationAndroid : FeatureHooker {
    private const val NOTIFICATION_PERMISSION = "android.permission.POST_NOTIFICATIONS"
    private const val FLAG_PERMISSION_POLICY_FIXED = 1 shl 2
    private const val FLAG_PERMISSION_SYSTEM_FIXED = 1 shl 4
    private const val FLAG_PERMISSION_GRANTED_BY_DEFAULT = 1 shl 5
    private const val FLAG_PERMISSION_GRANTED_BY_ROLE = 1 shl 15

    override fun hook(packageName: String) {
        // 调用链:
        // (通知管理) -> d.a.b.g.h.a(d.a.b.h$b, miui.notification.management.model.AppItem, int, android.widget.CompoundButton, boolean)
        // (通知管理) -> miui.notification.management.activity.NotificationAppListActivity.a(d.a.b.f)
        // (通知管理) -> miui.notification.management.activity.NotificationAppListActivity.a(android.content.Context, android.view.View, d.a.b.f$a)
        // (通知管理) -> d.a.a.c.e.c(android.content.Context, java.lang.String, boolean)
        // (miui-framework) -> miui.util.NotificationFilterHelper.enableNotifications(android.content.Context, java.lang.String, boolean)
        // (services) -> com.android.server.notification.NotificationManagerService$10.setNotificationsEnabledForPackage(java.lang.String, int, boolean)
        // (services) -> com.android.server.notification.PermissionHelper.setNotificationPermission(java.lang.String, int, boolean, boolean)

        val permInfoField = operator("com.android.server.pm.permission.Permission") {
            // name: mPermissionInfo | type: android.content.pm.PermissionInfo
            field("mPermissionInfo")
        } ?: return

        operator("com.android.server.notification.PermissionHelper") {
            // modifier: private | signature: packageRequestsNotificationPermission(Ljava/lang/String;I)Z
            method("packageRequestsNotificationPermission")?.hookResult(true)
        }

        operator("com.android.server.pm.permission.PermissionState") {
            // name: mPermission | type: com.android.server.pm.permission.Permission
            val permField = field("mPermission") ?: return@operator
            // modifier: public | signature: getFlags()I
            method("getFlags")?.replaceResult {
                val permission = permField.get<Any>(it.instance) ?: return@replaceResult Unit
                val permissionInfo = permInfoField.get<PermissionInfo>(permission) ?: return@replaceResult Unit
                if (permissionInfo.name != NOTIFICATION_PERMISSION) return@replaceResult Unit
                it.intResult and (FLAG_PERMISSION_POLICY_FIXED or FLAG_PERMISSION_SYSTEM_FIXED or FLAG_PERMISSION_GRANTED_BY_DEFAULT or FLAG_PERMISSION_GRANTED_BY_ROLE).inv()
            }
        }
    }
}