@file:Suppress("DEPRECATION")

package cc.meteormc.yourmiui.xposed.securitycenter.feature

import android.app.Activity
import android.os.AsyncTask
import cc.meteormc.yourmiui.xposed.MethodOps
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature

object RemoveAdbSwitchRestrictions : XposedFeature(
    key = "securitycenter_remove_adb_switch_restrictions",
    nameRes = R.string.feature_securitycenter_remove_adb_switch_restrictions_name,
    descriptionRes = R.string.feature_securitycenter_remove_adb_switch_restrictions_description,
    testEnvironmentRes = R.string.feature_securitycenter_remove_adb_switch_restrictions_test_environment
) {
    override fun onLoadPackage() {
        // 由于这个类被严重混淆 所以在保证兼容性的情况下只能使用一种比较monkey的方法
        // 效果不太好而且毫无可读性 无奈之举
        helper("com.miui.permcenter.install.AdbInstallVerifyActivity") {
            // 这个onCreate方法在AdbInstallVerifyActivity的父级类AlertActivity中
            // hook它是因为这是为数不多的没有被混淆 或者说不能被混淆的方法了
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            val onCreateMethod = method("onCreate") ?: return@helper
            onCreateMethod.hookBefore {
                val activity = it.thisObject as Activity
                // 由于hook的是父级方法 所以判断当前环境是否为AdbInstallVerifyActivity
                if (!delegate.isInstance(activity)) return@hookBefore

                // 调用super.onCreate以防止SuperNotCalledException报错
                onCreateMethod.callSuper(it.thisObject, *it.args)

                // name: (obfuscated) | type: (obfuscated)
                val taskField = fields(AsyncTask::class.java).firstOrNull() ?: return@hookBefore
                helper(taskField.type()) suboperate@{
                    // 由于当前hook的位置还没有初始化各种字段 所以手动创建一个$AsyncTask实例
                    // modifier: (default) | signature: <init>(Lcom/miui/permcenter/install/AdbInstallVerifyActivity;)V
                    val task = constructor(this@helper.delegate)?.new(activity) ?: return@suboperate
                    // 在onPostExecute中有操作adb开关的逻辑 并且这个方法没有混淆 所以直接找到并调用它
                    // 并且里面已经finish掉这个Activity了 无需重复操作
                    // modifier: public | signature: onPostExecute(Ljava/lang/String;)V
                    @Suppress("UNCHECKED_CAST")
                    (method("onPostExecute") as MethodOps<Any>?)?.call(task, null)
                }

                // 不要继续初始化AlertActivity了 直接返回
                it.result = null
            }
        }
    }
}