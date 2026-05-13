package cc.meteormc.yourmiui.xposed.securitycenter.feature

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.util.Unsafe.safeCast
import cc.meteormc.yourmiui.xposed.MethodWrapper
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator
import cc.meteormc.yourmiui.xposed.securitycenter.helper.AlertActivityHelper

object RemoveAdbSwitchRestrictions : Feature(
    key = "remove_adb_switch_restrictions",
    nameRes = R.string.feature_securitycenter_remove_adb_switch_restrictions_name,
    descriptionRes = R.string.feature_securitycenter_remove_adb_switch_restrictions_description,
    testEnvironmentRes = R.string.feature_securitycenter_remove_adb_switch_restrictions_test_environment
) {
    override fun onLoadPackage() {
        AlertActivityHelper.disableAlert(
            classLoader,
            "com.miui.permcenter.install.AdbInstallVerifyActivity"
        ) {
            // name: (obfuscated) | type: (obfuscated)
            @Suppress("DEPRECATION")
            val taskField = fields(android.os.AsyncTask::class.java).firstOrNull() ?: return@disableAlert false
            operator(taskField.type()) {
                // 由于当前hook的位置还没有初始化各种字段 所以手动创建一个$AsyncTask实例
                // modifier: (default) | signature: <init>(Lcom/miui/permcenter/install/AdbInstallVerifyActivity;)V
                val task = constructor(this@disableAlert.delegate)?.new(it) ?: return@operator false
                // 在onPostExecute中有操作adb开关的逻辑 并且这个方法没有混淆 所以直接找到并调用它
                // 并且里面已经finish掉这个Activity了 无需重复操作
                // modifier: public | signature: onPostExecute(Ljava/lang/String;)V
                method("onPostExecute").safeCast<MethodWrapper<Any>?>()?.call(task, null)
                return@operator true
            }
        }
    }
}