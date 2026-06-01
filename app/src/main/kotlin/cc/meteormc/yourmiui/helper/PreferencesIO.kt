package cc.meteormc.yourmiui.helper

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import cc.meteormc.yourmiui.common.prefs.SharedPreferences
import cc.meteormc.yourmiui.preferences.SettingsPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object PreferencesIO {
    const val MINE_TYPE = "application/json"
    private const val SHARED_NAME = SharedPreferences.SHARED_PREFERENCES_NAME
    private const val SETTINGS_NAME = SettingsPreferences.PREFERENCES_NAME

    suspend fun export(context: Context, uri: Uri): Boolean {
        val json = JSONObject()
        context.exportSingleToJson(SHARED_NAME, json)
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
                var value = it.value
                if (value is Array<*>) value = value.toList()
                if (value is Collection<*>) value = JSONArray(value)
                single.put(it.key, value)
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
        context.importSingleFromJson(SHARED_NAME, json)
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
                    is Boolean -> putBoolean(it, value)
                    is Int -> putInt(it, value)
                    is Long -> putLong(it, value)
                    is Float, Double -> putFloat(it, (value as Number).toFloat())
                    is JSONArray -> putStringSet(
                        it,
                        List(value.length()) { i -> value.optString(i) }.toSet()
                    )
                }
            }
        }
    }
}