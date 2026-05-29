package cc.meteormc.yourmiui.store

import android.content.pm.PackageManager
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.YourMIUI
import cc.meteormc.yourmiui.api.data.AppInfo
import cc.meteormc.yourmiui.api.data.FeatureInfo
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.bridge.Bridge
import cc.meteormc.yourmiui.common.bridge.ResponseCallback
import cc.meteormc.yourmiui.common.util.ClassUtil

object HostStore {
    val apiName = MutableLiveData("Unknown")
    val apiVersion = MutableLiveData(-1)
    val isActivated = MutableLiveData(false)
    val scopes = MutableLiveData(emptyMap<Scope, List<AppInfo>>())

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
        fetchScopes()

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

    private fun fetchScopes() {
        YourMIUI.get().moduleBridge.request(
            Bridge.GET_SCOPES_CHANNEL,
            BuildConfig.APPLICATION_ID,
            object : ResponseCallback<ArrayList<Scope>> {
                override fun onSuccess(data: ArrayList<Scope>) {
                    scopes.value = data.associateWith {
                        // 获取目标应用的名称和图标
                        val pm = YourMIUI.get().packageManager
                        it.packages.mapNotNull { pkg ->
                            val info = runCatching {
                                pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)
                            }.getOrNull() ?: return@mapNotNull null
                            AppInfo(
                                pkg,
                                pm.getApplicationLabel(info).toString(),
                                pm.getApplicationIcon(info).toBitmap()
                            )
                        }
                    }.filterValues {
                        // 过滤掉未安装的应用
                        it.isNotEmpty()
                    }
                }

                override fun onFailure() {
                }
            }
        )
    }
}