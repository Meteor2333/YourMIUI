package cc.meteormc.yourmiui.helper

import android.os.Build
import cc.meteormc.yourmiui.common.util.Unsafe.cast
import cc.meteormc.yourmiui.common.util.getClass

enum class SysVersion(val code: Int, val prefix: String) {
    MIUI_UNSUPPORTED(0, "V"),
    MIUI_11(11, "V11"),
    MIUI_12(12, "V12"),
    MIUI_13(13, "V13"),
    MIUI_14(14, "V14"),
    HYPEROS(816, "OS"),
    OTHER(-1, "");

    val fullName: String
        get() {
            val incremental = Build.VERSION.INCREMENTAL
            return if (this == OTHER) incremental
            else "${name.replace("_", "")}${incremental.removePrefix(prefix)}"
        }

    companion object {
        private const val VERSION_PROPERTY_KEY = "ro.miui.ui.version.code"
        private val currentSysVersion by lazy {
            val propClass = getClass(null, "android.os.SystemProperties", true)
            var versionCode = propClass?.getDeclaredMethod(
                "getInt",
                String::class.java,
                Int::class.javaPrimitiveType
            )?.also {
                it.isAccessible = true
            }?.invoke(
                null,
                VERSION_PROPERTY_KEY,
                -1
            ).cast<Int?>()
            if (versionCode == null) OTHER
            else entries.find { it.code == versionCode } ?: MIUI_UNSUPPORTED
        }

        fun getCurrent() = currentSysVersion
    }
}