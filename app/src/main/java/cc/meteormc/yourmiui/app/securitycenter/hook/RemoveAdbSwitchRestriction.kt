@file:Suppress("DEPRECATION")

package cc.meteormc.yourmiui.app.securitycenter.hook

import android.app.Activity
import android.os.AsyncTask
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.MethodOps
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

object RemoveAdbSwitchRestriction : Hook(
    name = R.string.securitycenter_remove_adb_switch_restriction_name,
    description = R.string.securitycenter_remove_adb_switch_restriction_description,
    testEnvironment = R.string.securitycenter_remove_adb_switch_restriction_test_environment
) {
    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 由于这个类被严重混淆 所以在保证兼容性的情况下只能使用一种比较monkey的方法
        // 效果不太好而且毫无可读性 无奈之举
        ReflectHelper.of("com.miui.permcenter.install.AdbInstallVerifyActivity", lpparam.classLoader)?.operate {
            // 这个onCreate方法在AdbInstallVerifyActivity的父级类AlertActivity中
            // hook它是因为这是为数不多的没有被混淆 或者说不能被混淆的方法了
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hook(object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as Activity
                    // 由于hook的是父级方法 所以判断当前环境是否为AdbInstallVerifyActivity
                    if (!delegate.isInstance(activity)) return

                    // 调用super.onCreate以防止SuperNotCalledException报错
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args)

                    @Suppress("UNCHECKED_CAST")
                    // name: (obfuscated) | type: (obfuscated)
                    field(AsyncTask::class.java).firstOrNull()?.let { field ->
                        ReflectHelper.fromJava(field.delegate.type).operate suboperate@ {
                            // 由于当前hook的位置还没有初始化各种字段 所以手动创建一个$AsyncTask实例
                            // modifier: (default) | signature: <init>(Lcom/miui/permcenter/install/AdbInstallVerifyActivity;)V
                            val task = constructor(this@operate.delegate)?.new(activity) ?: return@suboperate
                            // 在onPostExecute中有操作adb开关的逻辑 并且这个方法没有混淆 所以直接找到并调用它
                            // 并且里面已经finish掉这个Activity了 无需重复操作
                            // modifier: public | signature: onPostExecute(Ljava/lang/String;)V
                            (method("onPostExecute") as MethodOps<Any>?)?.call(task, null)
                        }
                    }

                    // 你也别初始化画面和乱七八糟的开关判断了 直接返回吧
                    param.result = null
                }
            })
        }
    }
}