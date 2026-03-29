package cc.meteormc.yourmiui.xposed.securitycenter.feature

import android.os.Handler
import cc.meteormc.yourmiui.xposed.HookFeature
import cc.meteormc.yourmiui.xposed.ReflectHelper
import cc.meteormc.yourmiui.xposed.ReflectScope
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

object DisableCountdownDialog : HookFeature(
    name = "securitycenter_disable_countdown_dialog_name",
    description = "securitycenter_disable_countdown_dialog_description",
    testEnvironment = "securitycenter_disable_countdown_dialog_test_environment"
) {
    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        ReflectHelper.of("com.miui.permcenter.privacymanager.InterceptBaseFragment", lpparam.classLoader)?.operate {
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hook(object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    // name: (obfuscated) | type: (obfuscated)
                    val handler = field(Handler::class.java).firstOrNull()?.get(param.thisObject) as Handler? ?: return
                    ReflectHelper.fromJava(handler.javaClass).operate {
                        // name: (obfuscated) | type: int
                        field(Int::class.java).firstOrNull()?.set(handler, -1)
                    }
                    handler.removeMessages(100)
                    handler.sendEmptyMessage(100)
                }
            })
        }

        val hookGetter = { scope: ReflectScope<Any> ->
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val thisObject = param.thisObject
                    // name: (obfuscated) | type: int
                    scope.field(Int::class.java).firstOrNull { it[thisObject] == 5 }?.set(thisObject, 1)
                    // name: (obfuscated) | type: android.os.Handler
                    val handler = scope.field(Handler::class.java).firstOrNull()?.get(thisObject) as Handler? ?: return
                    handler.removeMessages(100)
                    handler.sendEmptyMessage(100)
                }
            }
        }
        ReflectHelper.of("com.miui.permcenter.install.AdbInputApplyActivity", lpparam.classLoader)?.operate {
            // modifier: public | signature: onClick(Landroid/view/View;)V
            method("onClick")?.hook(hookGetter(this))
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hook(hookGetter(this))
        }
        // 根据倒计时特征匹配到的 但也许我们从来没有见过的界面
        ReflectHelper.of("com.miui.permcenter.root.RootApplyActivity", lpparam.classLoader)?.operate {
            // modifier: public | signature: onClick(Landroid/view/View;)V
            method("onClick")?.hook(hookGetter(this))
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hook(hookGetter(this))
        }
    }
}