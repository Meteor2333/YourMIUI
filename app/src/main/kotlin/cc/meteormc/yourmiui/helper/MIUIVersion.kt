package cc.meteormc.yourmiui.helper

import android.annotation.SuppressLint

enum class MIUIVersion(val code: Int) {
    UNKNOWN(-1),
    UNSUPPORTED(-1),
    MIUI_V11(11),
    MIUI_V12(12),
    MIUI_V13(13),
    MIUI_V14(14);

    companion object {
        private const val VERSION_PROPERTY_KEY = "ro.miui.ui.version.code"

        val currentVersion by lazy {
            var versionCode = runCatching {
                @SuppressLint("PrivateApi")
                Class.forName("android.os.SystemProperties").getDeclaredMethod(
                    "getInt",
                    String::class.java,
                    Integer.TYPE
                ).also { it.isAccessible = true }.invoke(null, VERSION_PROPERTY_KEY, -1) as Int
            }.getOrDefault(0)
            if (versionCode <= 0) UNKNOWN
            else entries.find { it.code == versionCode } ?: UNSUPPORTED
        }
    }
}