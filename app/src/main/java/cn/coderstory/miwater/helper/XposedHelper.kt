package cn.coderstory.miwater.helper

import androidx.annotation.Keep

class XposedHelper {
    companion object {
        // This method is hooked in cn.coderstory.miwater.MiWater#handleLoadPackage()
        @Keep
        fun isXposedActive(): Boolean {
            return false
        }
    }
}
