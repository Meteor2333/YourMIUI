package cn.coderstory.miwater.helper

import androidx.annotation.Keep

@Keep
class XposedHelper {
    companion object {
        // This method is hooked in cn.coderstory.miwater.MiWater#handleLoadPackage()
        fun isXposedActive(): Boolean {
            return false
        }
    }
}
