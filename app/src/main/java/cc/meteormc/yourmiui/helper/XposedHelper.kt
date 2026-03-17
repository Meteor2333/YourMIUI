package cc.meteormc.yourmiui.helper

import cc.meteormc.yourmiui.annotation.DontObfuscate

class XposedHelper {
    companion object {
        // This method is hooked in cn.coderstory.miwater.MiWater#handleLoadPackage()
        @DontObfuscate
        fun isXposedActive(): Boolean {
            return false
        }
    }
}
