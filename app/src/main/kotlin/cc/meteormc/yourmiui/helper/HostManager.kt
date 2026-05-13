package cc.meteormc.yourmiui.helper

import android.content.pm.PackageManager
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.YourMIUI
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.bridge.Bridge
import cc.meteormc.yourmiui.common.bridge.ResponseCallback
import cc.meteormc.yourmiui.common.data.AppInfo
import io.github.libxposed.service.XposedService

object HostManager {
    private val fetchedScopes = MutableLiveData<Map<Scope, List<AppInfo>>>()

    fun fetchScopes() {
        YourMIUI.get().moduleBridge.request(
            Bridge.GET_SCOPES_CHANNEL,
            BuildConfig.APPLICATION_ID,
            object : ResponseCallback<ArrayList<Scope>> {
                override fun onSuccess(data: ArrayList<Scope>) {
                    fetchedScopes.value = data.associateWith {
                        // 获取目标应用的名称和图标
                        val pm = YourMIUI.Companion.get().packageManager
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

    val activated
        get() = XposedService.activated || Bridge.apiVersion != null || Bridge.frameworkName != null

    val apiVersion
        get() = XposedService.apiVersion ?: Bridge.apiVersion ?: -1

    val frameworkName
        get() = XposedService.frameworkName ?: Bridge.frameworkName ?: "Unknown"

    val scopes: LiveData<Map<Scope, List<AppInfo>>>
        get() {
            val enabledScope = XposedService.scopes
            return MutableLiveData<Map<Scope, List<AppInfo>>>().apply {
                fun filterScope(scopes: Map<Scope, List<AppInfo>>) = scopes.filter { scope ->
                    if (enabledScope == null) return@filter true
                    scope.value
                        .map { it.packageName }
                        .any { it in enabledScope }
                }

                fetchedScopes.observeForever {
                    value = filterScope(it)
                }
            }
        }
}