package cc.meteormc.yourmiui.app.android.hook

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Handler
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

object BlockKillProcess: Hook(
    name = "阻止结束进程",
    description = "阻止系统或第三方应用结束本应用的进程",
    testEnvironment = "Android13的MIUI14版本",
    originalAuthor = "dantmnf"
) {
    private var classLoader: ClassLoader? = null
    private val blockedPackages = setOf(
        "com.github.metacubex.clash.meta"
    )

    private val SCOPE_PROCESS_RECORD by lazy {
        // 这个类在系统框架中
        // 从 /system/framework/services.jar 提取而来
        ReflectHelper.of("com.android.server.am.ProcessRecord", classLoader)
    }
    private val CLASS_PROCESS_RECORD by lazy {
        SCOPE_PROCESS_RECORD?.delegate
    }
    private val FIELD_PROCESS_RECORD_INFO by lazy {
        // name: info | type: android.content.pm.ApplicationInfo
        SCOPE_PROCESS_RECORD?.operate { field("info") }
    }

    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        this.classLoader = lpparam.classLoader
        // 这个类在系统框架中 是 MIUI 独有的
        // 从 /system_ext/framework/miui-services.jar 提取而来
        ReflectHelper.of("com.android.server.am.ProcessCleanerBase", classLoader)?.operate {
            val classProcessRecord = CLASS_PROCESS_RECORD
            val fieldProcessRecordInfo = FIELD_PROCESS_RECORD_INFO
            if (classProcessRecord == null || fieldProcessRecordInfo == null) return@operate Unit
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