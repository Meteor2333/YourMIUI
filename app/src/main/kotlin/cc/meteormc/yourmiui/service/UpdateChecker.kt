package cc.meteormc.yourmiui.service

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import cc.meteormc.yourmiui.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object UpdateChecker {
    private const val CHECK_URL = "https://api.github.com/repos/Meteor2333/YourMIUI/releases/latest"
    private const val CACHE_TIME = 24 * 60 * 60 * 1000

    var latest: String? = null
    var downloadUrl: String? = null
    var timestamp: Long = 0
    private lateinit var prefs: SharedPreferences

    val hasUpdate: Boolean
        get() = latest != null && latest != BuildConfig.VERSION_NAME

    suspend fun fetch(context: Context) {
        if (!SettingsPreferences.updateCheckEnabled) return
        if (!::prefs.isInitialized) {
            this.prefs = context.getSharedPreferences("version_cache", Context.MODE_PRIVATE)
        }

        latest = prefs.getString("latest", null)
        downloadUrl = prefs.getString("downloadUrl", null)
        timestamp = prefs.getLong("timestamp", 0)
        if (latest != null && System.currentTimeMillis() - timestamp <= CACHE_TIME) {
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
                    latest = json.getString("tag_name").removePrefix("v")
                    downloadUrl = json.getString("html_url")
                    timestamp = System.currentTimeMillis()
                }
            }.onSuccess {
                prefs.edit {
                    putString("latest", latest)
                    putString("downloadUrl", downloadUrl)
                    putLong("timestamp", timestamp)
                }
            }
        }
    }
}