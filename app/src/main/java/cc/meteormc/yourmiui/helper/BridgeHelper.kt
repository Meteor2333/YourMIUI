package cc.meteormc.yourmiui.helper

import cc.meteormc.yourmiui.annotation.DontObfuscate

@DontObfuscate
class BridgeHelper {
    // These method is hooked in cn.coderstory.miwater.MiWater#handleLoadPackage()
    companion object {
        fun getApiName(): String? {
            return null
        }

        fun getApiVersion(): Int {
            return -1
        }

        fun isModuleActive(): Boolean {
            return false
        }
    }
}
