package cc.meteormc.yourmiui.helper

import android.annotation.SuppressLint

enum class SysVersion(val code: Int) {
    UNSUPPORTED(0),
    MIUI_V11(11),
    MIUI_V12(12),
    MIUI_V13(13),
    MIUI_V14(14),
    HYPEROS(816),
    OTHER(-1);

    companion object {
        private const val VERSION_PROPERTY_KEY = "ro.miui.ui.version.code"
        private val currentSysVersion by lazy {
            var versionCode = runCatching {
                @SuppressLint("PrivateApi")
                Class.forName("android.os.SystemProperties").getDeclaredMethod(
                    "getInt",
                    String::class.java,
                    Int::class.javaPrimitiveType
                ).also { it.isAccessible = true }.invoke(null, VERSION_PROPERTY_KEY, -1) as Int
            }.getOrDefault(0)
            if (versionCode <= 0) OTHER
            else entries.find { it.code == versionCode } ?: UNSUPPORTED
        }

        fun getCurrent() = currentSysVersion
    }
}