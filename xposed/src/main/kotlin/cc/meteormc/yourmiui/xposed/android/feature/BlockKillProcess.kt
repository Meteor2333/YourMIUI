package cc.meteormc.yourmiui.xposed.android.feature

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Handler
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import de.robv.android.xposed.XC_MethodHook

object BlockKillProcess : XposedFeature(
    key = "android_block_kill_process",
    nameRes = R.string.feature_android_block_kill_process_name,
    descriptionRes = R.string.feature_android_block_kill_process_description,
    testEnvironmentRes = R.string.feature_android_block_kill_process_test_environment,
    originalAuthor = "dantmnf"
) {
    private val blockedPackages = setOf(
        "com.github.metacubex.clash.meta"
    )

    override fun init() {
        // 这个类在系统框架中 是 MIUI 独有的
        // 从 /system_ext/framework/miui-services.jar 提取而来
        helper("com.android.server.am.ProcessCleanerBase")?.operate {
            // 这个类在系统框架中
            // 从 /system/framework/services.jar 提取而来
            val helperProcessRecord = helper("com.android.server.am.ProcessRecord")
            val fieldProcessRecordInfo = helperProcessRecord?.operate {
                // name: info | type: android.content.pm.ApplicationInfo
                field("info")
            } ?: return@operate

            // modifier: (default) | signature: killOnce(Lcom/android/server/am/ProcessRecord;Ljava/lang/String;ILandroid/os/Handler;Landroid/content/Context;)V
            method(
                "killOnce",
                helperProcessRecord.delegate,
                String::class.java,
                Integer.TYPE,
                Handler::class.java,
                Context::class.java
            )?.hook(object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val process = param.args[0]
                    val info = fieldProcessRecordInfo[process, ApplicationInfo::class.java] ?: return
                    if (blockedPackages.contains(info.packageName)) {
                        param.setResult(null)
                    }
                }
            })
        }
    }
}