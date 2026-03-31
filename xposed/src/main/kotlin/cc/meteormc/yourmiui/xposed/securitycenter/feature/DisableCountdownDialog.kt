package cc.meteormc.yourmiui.xposed.securitycenter.feature

import android.os.Handler
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import de.robv.android.xposed.XC_MethodHook

object DisableCountdownDialog : XposedFeature(
    nameRes = R.string.feature_securitycenter_disable_countdown_dialog_name,
    descriptionRes = R.string.feature_securitycenter_disable_countdown_dialog_description,
    testEnvironmentRes = R.string.feature_securitycenter_disable_countdown_dialog_test_environment
) {
    override fun init() {
        helper("com.miui.permcenter.privacymanager.InterceptBaseFragment")?.operate {
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hook(object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    // name: (obfuscated) | type: (obfuscated)
                    val handler = fields(Handler::class.java).firstOrNull()?.get(param.thisObject, Handler::class.java) ?: return
                    helper(handler.javaClass).operate {
                        // name: (obfuscated) | type: int
                        fields(Int::class.java).firstOrNull()?.set(handler, -1)
                    }
                    handler.removeMessages(100)
                    handler.sendEmptyMessage(100)
                }
            })
        }

        val hookGetter = { operator: ReflectOperator<Any> ->
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val thisObject = param.thisObject
                    // name: (obfuscated) | type: int
                    operator.fields(Int::class.java).firstOrNull { it[thisObject, Integer.TYPE] == 5 }?.set(thisObject, 1)
                    // name: (obfuscated) | type: android.os.Handler
                    val handler = operator.fields(Handler::class.java).firstOrNull()?.get(thisObject, Handler::class.java) ?: return
                    handler.removeMessages(100)
                    handler.sendEmptyMessage(100)
                }
            }
        }
        helper("com.miui.permcenter.install.AdbInputApplyActivity")?.operate {
            // modifier: public | signature: onClick(Landroid/view/View;)V
            method("onClick")?.hook(hookGetter(this))
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hook(hookGetter(this))
        }
        // 根据倒计时特征匹配到的 但也许我们从来没有见过的界面
        helper("com.miui.permcenter.root.RootApplyActivity")?.operate {
            // modifier: public | signature: onClick(Landroid/view/View;)V
            method("onClick")?.hook(hookGetter(this))
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hook(hookGetter(this))
        }
    }
}