package cc.meteormc.yourmiui.app.securitycenter.hook

import android.os.Handler
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.ReflectHelper
import cc.meteormc.yourmiui.helper.ReflectScope
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

object DisableCountdownDialog: Hook(
    name = "取消倒计时对话框",
    description = "取消更改危险设置时的对话框确定倒计时",
    testEnvironment = "7.5.4-230317.0.2版本"
) {
    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        ReflectHelper.of("com.miui.permcenter.privacymanager.InterceptBaseFragment", lpparam.classLoader)?.operate {
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hook(object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val handler = field(Handler::class.java).firstOrNull()?.get(param.thisObject) as Handler? ?: return
                    ReflectHelper.fromJava(handler.javaClass).operate {
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
                    scope.field(Int::class.java).firstOrNull { it[thisObject] == 5 }?.set(thisObject, 1)
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