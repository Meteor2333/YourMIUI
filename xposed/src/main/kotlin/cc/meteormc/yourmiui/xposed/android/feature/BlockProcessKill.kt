package cc.meteormc.yourmiui.xposed.android.feature

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Handler
import cc.meteormc.yourmiui.core.Option
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedOption

object BlockProcessKill : XposedFeature(
    key = "android_block_process_kill",
    nameRes = R.string.feature_android_block_process_kill_name,
    descriptionRes = R.string.feature_android_block_process_kill_description,
    testEnvironmentRes = R.string.feature_android_block_process_kill_test_environment,
    originalAuthor = "dantmnf"
) {
    private lateinit var blockedPackages: List<String>

    override fun init() {
        // 这个类在系统框架中 是 MIUI 独有的
        // 从 /system_ext/framework/miui-services.jar 提取而来
        helper("com.android.server.am.ProcessCleanerBase") {
            // 这个类在系统框架中
            // 从 /system/framework/services.jar 提取而来
            helper("com.android.server.am.ProcessRecord") inner@{
                // name: info | type: android.content.pm.ApplicationInfo
                val processRecordInfoField = field("info") ?: return@inner

                // modifier: (default) | signature: killOnce(Lcom/android/server/am/ProcessRecord;Ljava/lang/String;ILandroid/os/Handler;Landroid/content/Context;)V
                this@helper.method(
                    "killOnce",
                    this@helper.delegate,
                    String::class.java,
                    Integer.TYPE,
                    Handler::class.java,
                    Context::class.java
                )?.hookBefore {
                    val process = it.args[0]
                    val info = processRecordInfoField[process, ApplicationInfo::class.java] ?: return@hookBefore
                    if (blockedPackages.contains(info.packageName)) {
                        it.result = null
                    }
                }
            }
        }
    }

    override fun getOptions(): Iterable<Option> {
        return listOf(
            XposedOption(
                "blocked_packages",
                R.string.option_android_block_process_kill_blocked_packages_name,
                R.string.option_android_block_process_kill_blocked_packages_summary,
                Option.Type.APP_LIST(),
                emptyList()
            ) { blockedPackages = it }
        )
    }
}