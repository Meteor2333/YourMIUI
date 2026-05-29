package cc.meteormc.yourmiui.xposed.system

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.api.annotation.SwitchOptionRegister
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.SYSTEM,
    "@string/feature_system_disable_adb_install_alert_name",
    "@string/feature_system_disable_adb_install_alert_description"
)
@RequiredScope("com.miui.securitycenter")
object DisableAdbInstallAlert : FeatureHooker {
    @SwitchOptionRegister(
        "@string/option_system_disable_adb_install_alert_require_unlock_name",
        "@string/option_system_disable_adb_install_alert_require_unlock_description",
        true
    )
    private var requireUnlock = true

    override fun hook(packageName: String) {
        val messagerClass = operator("android.os.IMessenger")?.delegate ?: return
        val getBinderMethod = operator("com.miui.permcenter.compact.IntentCompat") {
            // modifier: public static | signature: getIBinderExtra(Landroid/content/Intent;Ljava/lang/String;)Landroid/os/IBinder;
            method("getIBinderExtra")
        } ?: return
        val asInterfaceMethod = operator($$"android.os.IMessenger$Stub") {
            // modifier: public static | signature: asInterface(Landroid/os/IBinder;)Landroid/os/IMessenger;
            method("asInterface")
        } ?: return

        operator("com.miui.common.base.AlertActivity") {
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hookAfter {
                val activity = it.instance<Activity>()
                // 判断当前子类环境是否为所需的类
                if (activity.javaClass.name != "com.miui.permcenter.install.AdbInstallActivity") {
                    return@hookAfter
                }

                val binder = getBinderMethod.call(null, activity.intent, "observer")
                val messenger = asInterfaceMethod.call(null, binder)
                // name: (obfuscated) | type: android.os.IMessenger
                fields(messagerClass).firstOrNull()?.set(it, messenger)

                val km = activity.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager?
                if (!requireUnlock || (km != null && !km.isKeyguardLocked)) {
                    // name: (obfuscated) | type: int
                    fields(Int::class.javaPrimitiveType!!).firstOrNull { field ->
                        field.get<Int>(it) == 0
                    }?.set(it, -1)
                }

                activity.finish()
            }
        }
    }
}