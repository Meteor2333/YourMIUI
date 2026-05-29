package cc.meteormc.yourmiui.xposed.settings

import android.os.Handler
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.api.data.HookParam
import cc.meteormc.yourmiui.xposed.ReflectOperator
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.SETTINGS,
    "@string/feature_settings_disable_countdown_dialog_name",
    "@string/feature_settings_disable_countdown_dialog_description"
)
@RequiredScope("com.miui.securitycenter")
object DisableCountdownDialog : FeatureHooker {
    override fun hook(packageName: String) {
        operator("com.miui.permcenter.privacymanager.InterceptBaseFragment") {
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hookAfter {
                // name: (obfuscated) | type: (obfuscated)
                val handler = fields(Handler::class.java)
                    .firstOrNull()
                    ?.get<Handler>(it.instance) ?: return@hookAfter
                operator(handler.javaClass) {
                    // name: (obfuscated) | type: int
                    fields(Int::class.java).firstOrNull()?.set(handler, -1)
                }
                handler.removeMessages(100)
                handler.sendEmptyMessage(100)
            }
        }

        fun hookGetter(operator: ReflectOperator<Any>): (param: HookParam) -> Unit {
            return tag@{
                val instance = it.instance
                // name: (obfuscated) | type: int
                operator.fields(Int::class.javaPrimitiveType!!).firstOrNull { field ->
                    field.get<Int>(instance) == 5
                }?.set(instance, 1)
                // name: (obfuscated) | type: android.os.Handler
                val handler = operator.fields(Handler::class.java)
                    .firstOrNull()
                    ?.get<Handler>(instance) ?: return@tag
                handler.removeMessages(100)
                handler.sendEmptyMessage(100)
            }
        }

        operator("com.miui.permcenter.install.AdbInputApplyActivity") {
            // modifier: public | signature: onClick(Landroid/view/View;)V
            method("onClick")?.hookAfter(hookGetter(this))
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hookAfter(hookGetter(this))
        }
        // 根据倒计时特征匹配到的 但从来没有见过这个界面 为什么呢
        operator("com.miui.permcenter.root.RootApplyActivity") {
            // modifier: public | signature: onClick(Landroid/view/View;)V
            method("onClick")?.hookAfter(hookGetter(this))
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hookAfter(hookGetter(this))
        }
    }
}