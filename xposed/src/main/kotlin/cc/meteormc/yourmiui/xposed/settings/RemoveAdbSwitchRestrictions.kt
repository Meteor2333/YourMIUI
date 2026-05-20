package cc.meteormc.yourmiui.xposed.settings

import android.app.Activity
import android.os.AsyncTask
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.MethodWrapper
import cc.meteormc.yourmiui.xposed.operator

object RemoveAdbSwitchRestrictions : Feature() {
    @Suppress("UNCHECKED_CAST")
    override fun onLoadPackage(packageName: String) {
        operator(classLoader, "com.miui.common.base.AlertActivity") outer@{
            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hookDoNothing {
                val activity = it.instance<Activity>()
                // 判断当前子类环境是否为所需的类
                if (activity.javaClass.name != "com.miui.permcenter.install.AdbInstallVerifyActivity") {
                    return@hookDoNothing false
                }

                // 调用super.onCreate以防止SuperNotCalledException报错
                it.callSuper()

                // name: (obfuscated) | type: (obfuscated)
                val taskField = fields(AsyncTask::class.java).firstOrNull() ?: return@hookDoNothing false
                operator(taskField.type()) {
                    // 由于当前hook的位置还没有初始化各种字段 所以手动创建一个$AsyncTask实例
                    // modifier: (default) | signature: <init>(Lcom/miui/permcenter/install/AdbInstallVerifyActivity;)V
                    val task = constructor(this@outer.delegate)?.new(it) ?: return@operator false
                    // 在onPostExecute中有操作adb开关的逻辑 并且这个方法没有混淆 所以直接找到并调用它
                    // 并且里面已经finish掉这个Activity了 无需重复操作
                    // modifier: public | signature: onPostExecute(Ljava/lang/String;)V
                    (method("onPostExecute") as? MethodWrapper<Any>?)?.call(task, null)
                    return@operator true
                }
            }
        }
    }
}