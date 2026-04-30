package cc.meteormc.yourmiui.service

import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.core.graphics.drawable.toBitmap
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.YourMIUI
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.bridge.Bridge
import cc.meteormc.yourmiui.common.bridge.Module
import cc.meteormc.yourmiui.common.bridge.ResponseCallback
import cc.meteormc.yourmiui.ui.data.AppInfo
import java.util.concurrent.CountDownLatch

class HostDataStore {
    var apiName = "Unknown"
        private set
    var apiVersion = -1
        private set
    var isActivated = false
        private set
    var scopes: Map<Scope, List<AppInfo>> = emptyMap()
        private set

    private val latch = CountDownLatch(1)
    private val listeners = mutableListOf<HostDataStore.() -> Unit>()

    init {
        Thread {
            latch.await()
            Handler(Looper.getMainLooper()).post {
                listeners.forEach { it() }
            }
        }.start()
    }

    fun observe(listener: HostDataStore.() -> Unit) {
        if (latch.count <= 0L) {
            listener()
            return
        }

        listeners.add(listener)
    }

    fun fetch(bridge: Module) {
        bridge.request(
            Bridge.GET_API_STATUS_CHANNEL,
            BuildConfig.APPLICATION_ID,
            object : ResponseCallback<Pair<String, Int>> {
                override fun onSuccess(data: Pair<String, Int>) {
                    apiName = data.first
                    apiVersion = data.second
                    isActivated = true
                    loadScopes(bridge)
                }

                override fun onFailure() {
                    isActivated = false
                    while (true) {
                        if (latch.count <= 0) break
                        latch.countDown()
                    }
                }
            },
            300L
        )
    }

    private fun loadScopes(bridge: Module) {
        bridge.request(
            Bridge.GET_SCOPES_CHANNEL,
            BuildConfig.APPLICATION_ID,
            object : ResponseCallback<ArrayList<Scope>> {
                override fun onSuccess(data: ArrayList<Scope>) {
                    scopes = data.associateWith {
                        // 获取目标应用的名称和图标
                        val pm = YourMIUI.get().packageManager
                        it.getPackages().mapNotNull { pkg ->
                            val info = runCatching {
                                pm.getApplicationInfo(pkg.first, PackageManager.GET_META_DATA)
                            }.getOrNull() ?: return@mapNotNull null
                            AppInfo(
                                pkg.first,
                                pm.getApplicationLabel(info).toString(),
                                pm.getApplicationIcon(info).toBitmap(),
                                pkg.second
                            )
                        }
                    }.filterValues {
                        // 过滤掉未安装的应用
                        it.isNotEmpty()
                    }

                    latch.countDown()
                }

                override fun onFailure() {
                    latch.countDown()
                }
            }
        )
    }
}