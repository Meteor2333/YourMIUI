package cc.meteormc.yourmiui.helper

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.service.SettingsPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object UpdateChecker {
    private const val CACHE_TIME = 24 * 60 * 60 * 1000
    private const val CHECK_URL = "https://api.github.com/repos/Xposed-Modules-Repo/${BuildConfig.APPLICATION_ID}/releases/latest"

    var latest: Int = 0
    var timestamp: Long = 0
    var downloadUrl: String? = null
    private lateinit var prefs: SharedPreferences

    val hasUpdate: Boolean
        get() = latest > BuildConfig.VERSION_CODE

    suspend fun fetch(context: Context) {
        if (!SettingsPreferences.updateCheckEnabled) return
        if (!::prefs.isInitialized) {
            this.prefs = context.getSharedPreferences("version_cache", Context.MODE_PRIVATE)
        }

        runCatching {
            latest = prefs.getInt("latest", 0)
            timestamp = prefs.getLong("timestamp", 0)
            downloadUrl = prefs.getString("downloadUrl", null)
        }
        if (System.currentTimeMillis() - timestamp <= CACHE_TIME) {
            return
        }

        withContext(Dispatchers.IO) {
            runCatching {
                URL(CHECK_URL).openConnection().apply {
                    setConnectTimeout(10000)
                    setReadTimeout(10000)
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                }.getInputStream().use {
                    val json = JSONObject(it.bufferedReader().use { reader -> reader.readText() })
                    latest = json.getString("tag_name")
                        .split("-")
                        .firstNotNullOfOrNull { split ->
                            split.toIntOrNull()
                        } ?: 0
                    timestamp = System.currentTimeMillis()
                    downloadUrl = json.getString("html_url")
                }
            }.onSuccess {
                prefs.edit {
                    putInt("latest", latest)
                    putLong("timestamp", timestamp)
                    putString("downloadUrl", downloadUrl)
                }
            }
        }
    }
}