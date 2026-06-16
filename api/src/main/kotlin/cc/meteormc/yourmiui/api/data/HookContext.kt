package cc.meteormc.yourmiui.api.data

import cc.meteormc.yourmiui.api.FeatureHooker

data class HookContext(
    val uid: Int,
    val packageName: String,
    val processName: String?,
    val classLoader: ClassLoader,
    val hooked: MutableList<FeatureHooker> = mutableListOf()
) {
    fun isHooked(feature: FeatureHooker): Boolean {
        return hooked.contains(feature)
    }
}