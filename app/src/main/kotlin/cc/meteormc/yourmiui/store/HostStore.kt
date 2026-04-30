package cc.meteormc.yourmiui.store

import android.content.pm.PackageManager
import androidx.core.graphics.drawable.toBitmap
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.YourMIUI
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.bridge.Bridge
import cc.meteormc.yourmiui.common.bridge.ResponseCallback
import cc.meteormc.yourmiui.ui.data.AppInfo

object HostStore {
    val apiName = DataField("Unknown")
    val apiVersion = DataField(-1)
    val isActivated = DataField(false)
    val scopes = DataField(emptyMap<Scope, List<AppInfo>>())

    fun init() {
        val apiName = Bridge.apiName
        val apiVersion = Bridge.apiVersion
        if (apiName == null || apiVersion == null) {
            this.isActivated.updateValue(false)
            return
        }

        this.apiName.updateValue(apiName)
        this.apiVersion.updateValue(apiVersion)
        this.isActivated.updateValue(true)
        fetchScopes()
    }

    private fun fetchScopes() {
        YourMIUI.get().moduleBridge.request(
            Bridge.GET_SCOPES_CHANNEL,
            BuildConfig.APPLICATION_ID,
            object : ResponseCallback<ArrayList<Scope>> {
                override fun onSuccess(data: ArrayList<Scope>) {
                    val newScopes = data.associateWith {
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

                    scopes.updateValue(newScopes)
                }

                override fun onFailure() {

                }
            }
        )
    }
}