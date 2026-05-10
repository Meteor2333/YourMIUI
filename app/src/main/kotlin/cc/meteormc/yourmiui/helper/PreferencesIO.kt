package cc.meteormc.yourmiui.helper

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.preferences.SettingsPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object PreferencesIO {
    const val MINE_TYPE = "application/json"
    private const val FEATURE_NAME = Feature.PREFERENCES_NAME
    private const val SETTINGS_NAME = SettingsPreferences.PREFERENCES_NAME

    suspend fun export(context: Context, uri: Uri): Boolean {
        val json = JSONObject()
        context.exportSingleToJson(FEATURE_NAME, json)
        context.exportSingleToJson(SETTINGS_NAME, json)
        return withContext(Dispatchers.IO) {
            runCatching {
                val output = context.contentResolver.openOutputStream(uri)
                output.use {
                    it?.write(json.toString(2).toByteArray())
                    return@use it != null
                }
            }.getOrDefault(false)
        }
    }

    private fun Context.exportSingleToJson(name: String, json: JSONObject) {
        val single = JSONObject()
        this.getSharedPreferences(name, Context.MODE_PRIVATE)
            .all
            .entries
            .forEach {
                single.put(it.key, it.value)
            }
        json.put(name, single)
    }

    suspend fun import(context: Context, uri: Uri): Boolean {
        val json = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uri).use {
                    return@use if (it == null) null
                    else JSONObject(it.bufferedReader().readText())
                }
            }.getOrNull()
        } ?: return false
        if (json.length() <= 0) return false
        context.importSingleFromJson(FEATURE_NAME, json)
        context.importSingleFromJson(SETTINGS_NAME, json)
        return true
    }

    private fun Context.importSingleFromJson(name: String, json: JSONObject) {
        val single = json.optJSONObject(name) ?: return
        val preferences = this.getSharedPreferences(name, Context.MODE_PRIVATE)
        preferences.edit { clear() }
        single.keys().forEach {
            val value = single.opt(it) ?: return@forEach
            preferences.edit {
                when (value) {
                    is String -> putString(it, value)
                    is Int -> putInt(it, value)
                    is Long -> putLong(it, value)
                    is Float -> putFloat(it, value)
                    is Boolean -> putBoolean(it, value)
                }
            }
        }
    }
}