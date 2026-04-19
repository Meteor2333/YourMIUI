package cc.meteormc.yourmiui.xposed.settings.feature

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature

object DisableForceNotification : XposedFeature(
    key = "disable_force_notification",
    nameRes = R.string.feature_settings_disable_force_notification_name,
    descriptionRes = R.string.feature_settings_disable_force_notification_description,
    testEnvironmentRes = R.string.feature_settings_disable_force_notification_test_environment
) {
    override fun onLoadPackage() {
        helper("miui.util.NotificationFilterHelper") {
            // modifier: public static | signature: isNotificationForcedFor(Landroid/content/Context;Ljava/lang/String;)Z
            method("isNotificationForcedFor")?.hookResult(false)
        }

        helper("com.android.settings.notification.MiuiNotificationBackend") {
            // 加载基本数据
            // modifier: public | signature: loadAppRow(Landroid/content/Context;Landroid/content/pm/PackageManager;Landroid/content/pm/ApplicationInfo;)Lcom/android/settings/notification/MiuiNotificationBackend$AppRow;
            val loadMethod = method("loadAppRow", Context::class.java, PackageManager::class.java, ApplicationInfo::class.java) ?: return@helper
            // 在前者的基础上额外加载了我们不希望它加载的数据(如是否为系统应用)
            // modifier: public | signature: loadAppRow(Landroid/content/Context;Landroid/content/pm/PackageManager;Landroid/content/pm/PackageInfo;)Lcom/android/settings/notification/MiuiNotificationBackend$AppRow;
            method("loadAppRow", Context::class.java, PackageManager::class.java, PackageInfo::class.java)?.hookBefore {
                // 所以将冗余逻辑去掉 直接返回基本数据即可
                it.result = loadMethod.call(
                    it.thisObject,
                    it.args[0],
                    it.args[1],
                    (it.args[2] as PackageInfo).applicationInfo
                )
            }
        }
    }
}