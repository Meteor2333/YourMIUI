package cc.meteormc.yourmiui.xposed.android.feature

import android.content.pm.PermissionInfo
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.getIntResult
import cc.meteormc.yourmiui.xposed.operator

object DisableForceNotification : XposedFeature(
    key = "disable_force_notification",
    nameRes = R.string.feature_android_disable_force_notification_name,
    descriptionRes = R.string.feature_android_disable_force_notification_description,
    testEnvironmentRes = R.string.feature_android_disable_force_notification_test_environment
) {
    private const val NOTIFICATION_PERMISSION = "android.permission.POST_NOTIFICATIONS"
    private const val FLAG_PERMISSION_POLICY_FIXED = 1 shl 2
    private const val FLAG_PERMISSION_SYSTEM_FIXED = 1 shl 4
    private const val FLAG_PERMISSION_GRANTED_BY_DEFAULT = 1 shl 5
    private const val FLAG_PERMISSION_GRANTED_BY_ROLE = 1 shl 15

    override fun onLoadPackage() {
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
            // modifier: public | signature: hasPermission(I)Z
            method("hasPermission")?.hookResult(true)
        }

        operator("com.android.server.pm.permission.PermissionState") {
            // name: mPermission | type: com.android.server.pm.permission
            val permField = field("mPermission") ?: return@operator
            // modifier: public | signature: getFlags()I
            method("getFlags")?.replaceResult {
                val permission = permField[it.thisObject] ?: return@replaceResult Unit
                val permissionInfo = permInfoField[permission, PermissionInfo::class.java] ?: return@replaceResult Unit
                if (permissionInfo.name != NOTIFICATION_PERMISSION) return@replaceResult Unit
                it.getIntResult() and (FLAG_PERMISSION_POLICY_FIXED or FLAG_PERMISSION_SYSTEM_FIXED or FLAG_PERMISSION_GRANTED_BY_DEFAULT or FLAG_PERMISSION_GRANTED_BY_ROLE).inv()
            }
        }
    }
}