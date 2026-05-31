package cc.meteormc.yourmiui.store

import androidx.lifecycle.MutableLiveData
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.data.FeatureInfo
import cc.meteormc.yourmiui.api.util.ClassUtil
import cc.meteormc.yourmiui.api.util.SingletonUtil
import cc.meteormc.yourmiui.common.bridge.Bridge
import java.util.EnumMap

object HostStore {
    val apiName = MutableLiveData("Unknown")
    val apiVersion = MutableLiveData(-1)
    val isActivated = MutableLiveData(false)
    val features = MutableLiveData<EnumMap<Category, List<FeatureInfo>>>()

    fun init() {
        val apiName = Bridge.apiName
        val apiVersion = Bridge.apiVersion
        if (apiName != null && apiVersion != null) {
            this.apiName.value = apiName
            this.apiVersion.value = apiVersion
            this.isActivated.value = true
        }

        this.features.value = ClassUtil.getClass(
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