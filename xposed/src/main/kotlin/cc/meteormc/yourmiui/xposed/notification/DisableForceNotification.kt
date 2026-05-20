package cc.meteormc.yourmiui.xposed.notification

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.operator

object DisableForceNotification : Feature() {
    private const val NOTIFICATION_PERMISSION = "android.permission.POST_NOTIFICATIONS"
    private const val FLAG_PERMISSION_POLICY_FIXED = 1 shl 2
    private const val FLAG_PERMISSION_SYSTEM_FIXED = 1 shl 4
    private const val FLAG_PERMISSION_GRANTED_BY_DEFAULT = 1 shl 5
    private const val FLAG_PERMISSION_GRANTED_BY_ROLE = 1 shl 15

    override fun onLoadPackage(packageName: String) {
        // 调用链:
        // (通知管理) -> d.a.b.g.h.a(d.a.b.h$b, miui.notification.management.model.AppItem, int, android.widget.CompoundButton, boolean)
        // (通知管理) -> miui.notification.management.activity.NotificationAppListActivity.a(d.a.b.f)
        // (通知管理) -> miui.notification.management.activity.NotificationAppListActivity.a(android.content.Context, android.view.View, d.a.b.f$a)
        // (通知管理) -> d.a.a.c.e.c(android.content.Context, java.lang.String, boolean)
        // (miui-framework) -> miui.util.NotificationFilterHelper.enableNotifications(android.content.Context, java.lang.String, boolean)
        // (services) -> com.android.server.notification.NotificationManagerService$10.setNotificationsEnabledForPackage(java.lang.String, int, boolean)
        // (services) -> com.android.server.notification.PermissionHelper.setNotificationPermission(java.lang.String, int, boolean, boolean)

        when (packageName) {
            "android" -> hookAndroid()
            "com.miui.notification" -> hookNotification()
            "com.android.settings" -> hookSettings()
            "com.android.systemui" -> hookSystemUI()
        }
    }

    private fun hookAndroid() {
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

    private fun hookNotification() {
        operator("miui.util.NotificationFilterHelper") {
            // modifier: public static | signature: isNotificationForcedFor(Landroid/content/Context;Ljava/lang/String;)Z
            method("isNotificationForcedFor")?.hookResult(false)
        }

        operator("miui.notification.management.model.AppItem") {
            // modifier: public | signature: isSystemApp()Z
            method("isSystemApp")?.hookResult(false)
        }
    }

    private fun hookSettings() {
        operator("miui.util.NotificationFilterHelper") {
            // modifier: public static | signature: isNotificationForcedFor(Landroid/content/Context;Ljava/lang/String;)Z
            method("isNotificationForcedFor")?.hookResult(false)
        }

        operator("com.android.settings.notification.BaseNotificationSettings") {
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hookAfter {
                // name: mHasNotifPermission | type: boolean
                field("mHasNotifPermission")?.set(it.instance, true)
            }
        }

        operator("com.android.settings.notification.MiuiNotificationBackend") {
            // 此方法会加载基本数据
            // modifier: public | signature: loadAppRow(Landroid/content/Context;Landroid/content/pm/PackageManager;Landroid/content/pm/ApplicationInfo;)Lcom/android/settings/notification/MiuiNotificationBackend$AppRow;
            val loadMethod = method(
                "loadAppRow",
                Context::class.java,
                PackageManager::class.java,
                ApplicationInfo::class.java
            ) ?: return@operator
            // 此方法在前者的基础上额外加载了我们不希望它加载的数据(如是否为系统应用)
            // modifier: public | signature: loadAppRow(Landroid/content/Context;Landroid/content/pm/PackageManager;Landroid/content/pm/PackageInfo;)Lcom/android/settings/notification/MiuiNotificationBackend$AppRow;
            method(
                "loadAppRow",
                Context::class.java,
                PackageManager::class.java,
                PackageInfo::class.java
            )?.overrideResult {
                // 所以将冗余逻辑去掉 直接返回基本数据即可
                loadMethod.call(
                    it.instance,
                    it.argByGenerics<Context>(),
                    it.argByGenerics<PackageManager>(),
                    it.argByGenerics<PackageInfo>()?.applicationInfo
                )
            }
        }
    }

    private fun hookSystemUI() {
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