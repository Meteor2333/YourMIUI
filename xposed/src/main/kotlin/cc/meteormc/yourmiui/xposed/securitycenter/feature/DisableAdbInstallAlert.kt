package cc.meteormc.yourmiui.xposed.securitycenter.feature

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.operator
import cc.meteormc.yourmiui.xposed.securitycenter.helper.AlertActivityHelper

object DisableAdbInstallAlert : XposedFeature(
    key = "disable_adb_install_alert",
    nameRes = R.string.feature_securitycenter_disable_adb_install_alert_name,
    descriptionRes = R.string.feature_securitycenter_disable_adb_install_alert_description,
    testEnvironmentRes = R.string.feature_securitycenter_disable_adb_install_alert_test_environment
) {
    override fun onLoadPackage() {
        val messagerClass = operator("android.os.IMessenger")?.delegate ?: return
        val getBinderMethod = operator("com.miui.permcenter.compact.IntentCompat") {
            // modifier: public static | signature: getIBinderExtra(Landroid/content/Intent;Ljava/lang/String;)Landroid/os/IBinder;
            method("getIBinderExtra")
        } ?: return
        val asInterfaceMethod = operator($$"android.os.IMessenger$Stub") {
            // modifier: public static | signature: asInterface(Landroid/os/IBinder;)Landroid/os/IMessenger;
            method("asInterface")
        } ?: return
        AlertActivityHelper.disableAlert(classLoader, "com.miui.permcenter.install.AdbInstallActivity") {
            // name: (obfuscated) | type: int
            fields(Int::class.javaPrimitiveType!!).firstOrNull { field ->
                field.get<Int>(it) == 0
            }?.set(it, -1)

            val binder = getBinderMethod.call(null, it.intent, "observer")
            val messenger = asInterfaceMethod.call(null, binder)
            // name: (obfuscated) | type: android.os.IMessenger
            fields(messagerClass).firstOrNull()?.set(it, messenger)

            it.finish()
            true
        }
    }
}