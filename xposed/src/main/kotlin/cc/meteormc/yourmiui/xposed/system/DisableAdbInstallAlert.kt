package cc.meteormc.yourmiui.xposed.system

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

object DisableAdbInstallAlert : Feature() {
    private var requireUnlock = false

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
        operator(classLoader, "com.miui.common.base.AlertActivity") outer@{
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hookDoNothing {
                val activity = it.instance<Activity>()
                // 判断当前子类环境是否为所需的类
                if (activity.javaClass.name != "com.miui.permcenter.install.AdbInstallActivity") {
                    return@hookDoNothing false
                }

                // 调用super.onCreate以防止SuperNotCalledException报错
                it.callSuper()

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
                true
            }
        }
    }

    override fun getOptions(): List<Option<*>> {
        return listOf(
            Option(
                "require_unlock",
                R.string.option_securitycenter_disable_adb_install_alert_require_unlock_name,
                R.string.option_securitycenter_disable_adb_install_alert_require_unlock_summary,
                Option.Type.Switch(),
                true
            ) { requireUnlock = it }
        )
    }
}