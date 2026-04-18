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
    private lateinit var blockedPackages: Set<String>

    override fun init() {
        // 这个类在系统框架中 是 MIUI 独有的
        // 从 /system_ext/framework/miui-services.jar 提取而来
        helper("com.android.server.am.ProcessCleanerBase") {
            // 这个类在系统框架中
            // 从 /system/framework/services.jar 提取而来
            val operator = helper("com.android.server.am.ProcessRecord") ?: return@helper
            // name: info | type: android.content.pm.ApplicationInfo
            val infoField = operator.field("info") ?: return@helper
            val recordClass = operator.delegate

            // modifier: (default) | signature: killOnce(Lcom/android/server/am/ProcessRecord;Ljava/lang/String;ILandroid/os/Handler;Landroid/content/Context;)V
            method(
                "killOnce",
                recordClass,
                String::class.java,
                Int::class.javaPrimitiveType!!,
                Handler::class.java,
                Context::class.java
            )?.hookBefore {
                val process = it.args[0]
                val info = infoField[process, ApplicationInfo::class.java] ?: return@hookBefore
                if (blockedPackages.contains(info.packageName)) {
                    it.result = null
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
                emptySet()
            ) { blockedPackages = it }
        )
    }
}