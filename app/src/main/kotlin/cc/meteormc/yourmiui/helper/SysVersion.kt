package cc.meteormc.yourmiui.helper

import android.os.Build
import cc.meteormc.yourmiui.api.util.PropertiesUtil

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
            var versionCode = PropertiesUtil.getInt(VERSION_PROPERTY_KEY)
            if (versionCode <= 0) OTHER
            else entries.find { it.code == versionCode } ?: MIUI_UNSUPPORTED
        }

        fun getCurrent() = currentSysVersion
    }
}