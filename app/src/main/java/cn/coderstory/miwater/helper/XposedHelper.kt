package cn.coderstory.miwater.helper

import cn.coderstory.miwater.annotation.DontObfuscate

class XposedHelper {
    companion object {
        // This method is hooked in cn.coderstory.miwater.MiWater#handleLoadPackage()
        @DontObfuscate
        fun isXposedActive(): Boolean {
            return false
        }
    }
}
