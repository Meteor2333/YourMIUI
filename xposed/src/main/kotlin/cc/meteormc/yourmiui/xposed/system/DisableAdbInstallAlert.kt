package cc.meteormc.yourmiui.xposed.system

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.api.annotation.SwitchOptionRegister
import cc.meteormc.yourmiui.api.data.HookContext
import cc.meteormc.yourmiui.xposed.call
import cc.meteormc.yourmiui.xposed.get
import cc.meteormc.yourmiui.xposed.hookAfter
import cc.meteormc.yourmiui.xposed.reflect

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

    override fun hook(context: HookContext) {
        val messagerClass = context.reflect("android.os.IMessenger")?.delegate ?: return
        val getBinderMethod = context.reflect("com.miui.permcenter.compact.IntentCompat") {
            // modifier: public static | signature: getIBinderExtra(Landroid/content/Intent;Ljava/lang/String;)Landroid/os/IBinder;
            method("getIBinderExtra")
        } ?: return
        val asInterfaceMethod = context.reflect($$"android.os.IMessenger$Stub") {
            // modifier: public static | signature: asInterface(Landroid/os/IBinder;)Landroid/os/IMessenger;
            method("asInterface")
        } ?: return

        val targetClass = "com.miui.permcenter.install.AdbInstallActivity"
        context.reflect(targetClass) {
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hookAfter {
                val activity = it.instance<Activity>()
                // 判断当前子类环境是否为所需的类
                if (activity.javaClass.name != targetClass) {
                    return@hookAfter
                }

                val binder = getBinderMethod.call(null, activity.intent, "observer")
                val messenger = asInterfaceMethod.call(null, binder)
                // name: (obfuscated) | type: android.os.IMessenger
                fields(messagerClass).firstOrNull()?.set(activity, messenger)

                val km = activity.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager?
                if (!requireUnlock || (km != null && !km.isKeyguardLocked)) {
                    // name: (obfuscated) | type: int
                    fields(Int::class.javaPrimitiveType!!).firstOrNull { field ->
                        field.get<Int>(activity) == 0
                    }?.set(activity, -1)
                }

                activity.finish()
            }
        }
    }
}