package cc.meteormc.yourmiui.xposed.system

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Handler
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.SYSTEM,
    "@string/feature_system_block_process_kill_name",
    "@string/feature_system_block_process_kill_description"
)
@RequiredScope("android")
object BlockProcessKill : FeatureHooker {
    private lateinit var blockedPackages: Set<String>

    override fun hook(packageName: String) {
        // 从 /system_ext/framework/miui-services.jar 提取
        operator("com.android.server.am.ProcessCleanerBase") {
            // 从 /system/framework/services.jar 提取
            val operator = operator("com.android.server.am.ProcessRecord") ?: return@operator
            // name: info | type: android.content.pm.ApplicationInfo
            val infoField = operator.field("info") ?: return@operator
            val recordClass = operator.delegate

            // modifier: (default) | signature: killOnce(Lcom/android/server/am/ProcessRecord;Ljava/lang/String;ILandroid/os/Handler;Landroid/content/Context;)V
            method(
                "killOnce",
                recordClass,
                String::class.java,
                Int::class.javaPrimitiveType!!,
                Handler::class.java,
                Context::class.java
            )?.hookDoNothing {
                val process = it.argByClass(recordClass) ?: return@hookDoNothing false
                val info = infoField.get<ApplicationInfo>(process) ?: return@hookDoNothing false
                blockedPackages.contains(info.packageName)
            }
        }
    }

    fun getOptions(): List<Option<Set<String>>> {
        return listOf(
            Option(
                "blocked_packages",
                R.string.option_android_block_process_kill_blocked_packages_name,
                R.string.option_android_block_process_kill_blocked_packages_summary,
                Option.Type.AppList(),
                emptySet()
            ) { blockedPackages = it }
        )
    }
}