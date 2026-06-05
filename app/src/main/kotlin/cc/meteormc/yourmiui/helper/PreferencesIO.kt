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

    private val preferenceNames = setOf(
        Feature.PREFERENCES_NAME,
        SettingsPreferences.PREFERENCES_NAME
    )

    suspend fun export(context: Context, uri: Uri): Boolean {
        val json = JSONObject()
        preferenceNames.forEach {
            val single = JSONObject()
            context.getSharedPreferences(it, Context.MODE_PRIVATE)
                .all
                .entries
                .forEach {
                    single.put(it.key, it.value)
                }
            json.put(it, single)
        }

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
        preferenceNames.forEach {
            val single = json.optJSONObject(it) ?: return@forEach
            val preferences = context.getSharedPreferences(it, Context.MODE_PRIVATE)
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

        return true
    }

    fun reset(context: Context) {
        preferenceNames.forEach {
            context.getSharedPreferences(it, Context.MODE_PRIVATE).edit { clear() }
        }
    }
}