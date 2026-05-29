package cc.meteormc.yourmiui.xposed.system

import android.app.KeyguardManager
import android.content.Context
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator
import cc.meteormc.yourmiui.xposed.securitycenter.helper.AlertActivityHelper

@FeatureRegister(
    Category.SYSTEM,
    "@string/feature_system_disable_adb_install_alert_name",
    "@string/feature_system_disable_adb_install_alert_description"
)
@RequiredScope("com.miui.securitycenter")
object DisableAdbInstallAlert : FeatureHooker {
    private var requireUnlock = false

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
        AlertActivityHelper.disableAlert(
            classLoader,
            "com.miui.permcenter.install.AdbInstallActivity"
        ) {
            val binder = getBinderMethod.call(null, it.intent, "observer")
            val messenger = asInterfaceMethod.call(null, binder)
            // name: (obfuscated) | type: android.os.IMessenger
            fields(messagerClass).firstOrNull()?.set(it, messenger)

            val km = it.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager?
            if (!requireUnlock || (km != null && !km.isKeyguardLocked)) {
                // name: (obfuscated) | type: int
                fields(Int::class.javaPrimitiveType!!).firstOrNull { field ->
                    field.get<Int>(it) == 0
                }?.set(it, -1)
            }

            it.finish()
            true
        }
    }

    fun getOptions(): List<Option<*>> {
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