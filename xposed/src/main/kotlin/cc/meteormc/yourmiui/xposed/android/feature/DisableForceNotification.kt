package cc.meteormc.yourmiui.xposed.android.feature

import android.content.pm.PermissionInfo
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature

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

        val permInfoField = helper("com.android.server.pm.permission.Permission") {
            // name: mPermissionInfo | type: android.content.pm.PermissionInfo
            field("mPermissionInfo")
        } ?: return

        helper("com.android.server.pm.permission.PermissionState") {
            // name: mPermission | type: com.android.server.pm.permission
            val permField = field("mPermission") ?: return@helper
            // modifier: public | signature: getFlags()I
            method("getFlags")?.hookAfter {
                // 原理解释:
                // 系统底层在判断某个软件的通知是否允许被关闭时
                // 会使用从android.permission.POST_NOTIFICATIONS获取的掩码对以下的4个flag进行运算
                // 只要有任何一个符合条件 就强制它不能被关闭
                // 所以只需要拦截并修改这个值即可
                val permission = permField[it.thisObject, Any::class.java] ?: return@hookAfter
                val permissionInfo = permInfoField[permission, PermissionInfo::class.java] ?: return@hookAfter
                if (permissionInfo.name != NOTIFICATION_PERMISSION) return@hookAfter

                var flags = it.result as Int
                flags = flags and (FLAG_PERMISSION_POLICY_FIXED or FLAG_PERMISSION_SYSTEM_FIXED or FLAG_PERMISSION_GRANTED_BY_DEFAULT or FLAG_PERMISSION_GRANTED_BY_ROLE).inv()
                it.result = flags
            }
        }
    }
}