package cc.meteormc.yourmiui.xposed.settings.feature

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

object DisableForceNotification : Feature(
    key = "disable_force_notification",
    nameRes = R.string.feature_settings_disable_force_notification_name,
    descriptionRes = R.string.feature_settings_disable_force_notification_description,
    testEnvironmentRes = R.string.feature_settings_disable_force_notification_test_environment
) {
    override fun onLoadPackage() {
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
}