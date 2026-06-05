package cc.meteormc.yourmiui.store

import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.data.FeatureInfo
import cc.meteormc.yourmiui.api.util.ClassUtil
import cc.meteormc.yourmiui.api.util.SingletonUtil
import cc.meteormc.yourmiui.common.bridge.Bridge
import java.util.EnumMap

object HostStore {
    var apiName = "Unknown"
        private set
    var apiVersion = -1
        private set
    var isActivated = false
        private set
    lateinit var features: EnumMap<Category, List<FeatureInfo>>
        private set

    fun init() {
        val apiName = Bridge.apiName
        val apiVersion = Bridge.apiVersion
        if (apiName != null && apiVersion != null) {
            this.apiName = apiName
            this.apiVersion = apiVersion
            this.isActivated = true
        }

        this.features = ClassUtil.getClass(
            javaClass.classLoader!!,
            "${BuildConfig.APPLICATION_ID}.FeatureRegistry",
            true
        )?.run {
            val instance = SingletonUtil.getInstance(this)
            @Suppress("UNCHECKED_CAST")
            getDeclaredMethod("getFeatures").invoke(instance) as? Map<String, List<FeatureHooker>>
        }.orEmpty()
            .values
            .flatten()
            .distinct()
            .map { FeatureInfo.fromHooker(it) }
            .groupByTo(EnumMap(Category::class.java)) { it.category }
            .run { EnumMap(this) }
    }
}