package cc.meteormc.yourmiui.store

import androidx.lifecycle.MutableLiveData
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.api.data.FeatureInfo
import cc.meteormc.yourmiui.common.bridge.Bridge
import cc.meteormc.yourmiui.common.util.ClassUtil

object HostStore {
    val apiName = MutableLiveData("Unknown")
    val apiVersion = MutableLiveData(-1)
    val isActivated = MutableLiveData(false)

    fun init() {
        val apiName = Bridge.apiName
        val apiVersion = Bridge.apiVersion
        if (apiName == null || apiVersion == null) {
            this.isActivated.value = false
            return
        }

        this.apiName.value = apiName
        this.apiVersion.value = apiVersion
        this.isActivated.value = true

        // todo
        ClassUtil.getClass(
            javaClass.classLoader!!,
            "${BuildConfig.APPLICATION_ID}.FeatureRegistry",
            true
        )?.run {
            val instance = getDeclaredField("INSTANCE").get(null) ?: return@run null

            @Suppress("UNCHECKED_CAST")
            val feature =
                getDeclaredMethod("getFeatures").invoke(instance) as? Map<String, List<Any>>
                    ?: return@run null
            return@run feature.values
                .flatten()
                .distinct()
                .map { FeatureInfo.fromSource(it.javaClass) }
                .groupBy { it.category }
        } ?: emptyMap()
    }
}