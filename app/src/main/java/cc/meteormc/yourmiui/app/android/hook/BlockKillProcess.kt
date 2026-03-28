package cc.meteormc.yourmiui.app.android.hook

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Handler
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

object BlockKillProcess : Hook(
    name = R.string.android_block_kill_process_name,
    description = R.string.android_block_kill_process_description,
    testEnvironment = R.string.android_block_kill_process_test_environment,
    originalAuthor = "dantmnf"
) {
    private val blockedPackages = setOf(
        "com.github.metacubex.clash.meta"
    )

    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 这个类在系统框架中
        // 从 /system/framework/services.jar 提取而来
        val scopeProcessRecord = ReflectHelper.of("com.android.server.am.ProcessRecord", lpparam.classLoader) ?: return

        // 这个类在系统框架中 是 MIUI 独有的
        // 从 /system_ext/framework/miui-services.jar 提取而来
        ReflectHelper.of("com.android.server.am.ProcessCleanerBase", lpparam.classLoader)?.operate {
            val classProcessRecord = scopeProcessRecord.delegate
            val fieldProcessRecordInfo = scopeProcessRecord.operate {
                // name: info | type: android.content.pm.ApplicationInfo
                field("info")
            } ?: return@operate

            // modifier: (default) | signature: killOnce(Lcom/android/server/am/ProcessRecord;Ljava/lang/String;ILandroid/os/Handler;Landroid/content/Context;)V
            method(
                "killOnce",
                classProcessRecord,
                String::class.java,
                Integer.TYPE,
                Handler::class.java,
                Context::class.java
            )?.hook(object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val process = param.args[0]
                    val info = fieldProcessRecordInfo[process] as ApplicationInfo
                    if (blockedPackages.contains(info.packageName)) {
                        param.setResult(null)
                    }
                }
            })
        }
    }
}