package cc.meteormc.yourmiui.xposed.securitycenter.feature

import android.os.Handler
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.ReflectOperator
import cc.meteormc.yourmiui.xposed.XposedFeature
import de.robv.android.xposed.XC_MethodHook

object DisableCountdownDialog : XposedFeature(
    key = "securitycenter_disable_countdown_dialog",
    nameRes = R.string.feature_securitycenter_disable_countdown_dialog_name,
    descriptionRes = R.string.feature_securitycenter_disable_countdown_dialog_description,
    testEnvironmentRes = R.string.feature_securitycenter_disable_countdown_dialog_test_environment
) {
    override fun init() {
        helper("com.miui.permcenter.privacymanager.InterceptBaseFragment") {
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hookAfter {
                // name: (obfuscated) | type: (obfuscated)
                val handler = fields(Handler::class.java)
                    .firstOrNull()
                    ?.get(it.thisObject, Handler::class.java) ?: return@hookAfter
                helper(handler.javaClass) {
                    // name: (obfuscated) | type: int
                    fields(Int::class.java).firstOrNull()?.set(handler, -1)
                }
                handler.removeMessages(100)
                handler.sendEmptyMessage(100)
            }
        }

        fun hookGetter(operator: ReflectOperator<Any>): (param: XC_MethodHook.MethodHookParam) -> Unit {
            return {
                val thisObject = it.thisObject
                // name: (obfuscated) | type: int
                operator.fields(Int::class.java).firstOrNull { field ->
                    field[thisObject, Integer.TYPE] == 5
                }?.set(thisObject, 1)
                // name: (obfuscated) | type: android.os.Handler
                val handler = operator.fields(Handler::class.java)
                    .firstOrNull()
                    ?.get(thisObject, Handler::class.java)
                handler?.removeMessages(100)
                handler?.sendEmptyMessage(100)
            }
        }

        helper("com.miui.permcenter.install.AdbInputApplyActivity") {
            // modifier: public | signature: onClick(Landroid/view/View;)V
            method("onClick")?.hookAfter(hookGetter(this))
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hookAfter(hookGetter(this))
        }
        // 根据倒计时特征匹配到的 但从来没有见过这个界面 为什么呢
        helper("com.miui.permcenter.root.RootApplyActivity") {
            // modifier: public | signature: onClick(Landroid/view/View;)V
            method("onClick")?.hookAfter(hookGetter(this))
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hookAfter(hookGetter(this))
        }
    }
}