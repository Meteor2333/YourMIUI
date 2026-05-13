package io.github.libxposed.service

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import java.util.Collections
import java.util.TreeMap
import java.util.WeakHashMap
import java.util.concurrent.Executors

@Suppress("UNCHECKED_CAST")
internal class RemotePreferences(
    private val group: String,
    private var map: Map<String, *>
) : SharedPreferences {
    private val listeners = Collections.newSetFromMap(
        WeakHashMap<OnSharedPreferenceChangeListener, Boolean>()
    )

    override fun contains(key: String): Boolean {
        return map.containsKey(key)
    }

    override fun getAll(): Map<String, *> {
        return TreeMap(map)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return map.getOrDefault(key, defValue) as Boolean
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return map.getOrDefault(key, defValue) as Float
    }

    override fun getInt(key: String, defValue: Int): Int {
        return map.getOrDefault(key, defValue) as Int
    }

    override fun getLong(key: String, defValue: Long): Long {
        return map.getOrDefault(key, defValue) as Long
    }

    override fun getString(key: String, defValue: String?): String? {
        return map.getOrDefault(key, defValue) as String?
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return map.getOrDefault(key, defValues) as Set<String>?
    }

    override fun edit(): SharedPreferences.Editor {
        return Editor(this)
    }

    override fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        listeners.add(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        listeners.remove(listener)
    }

    class Editor(private val prefs: RemotePreferences) : SharedPreferences.Editor {
        val delete = mutableSetOf<String>()
        val put = mutableMapOf<String, Any?>()
        private val executor = Executors.newSingleThreadExecutor()

        override fun apply() {
            update()
            executor.execute { XposedService.updateRemotePreferences(prefs.group, this) }
        }

        override fun commit(): Boolean {
            update()
            XposedService.updateRemotePreferences(prefs.group, this)
            return true
        }

        private fun update() {
            synchronized(this) {
                val newMap = prefs.map.toMutableMap()
                delete.forEach { newMap.remove(it) }
                newMap.putAll(put)
                prefs.map = newMap
            }

            val changes = delete.toSet() + put.keys
            synchronized(prefs.listeners) {
                prefs.listeners.toList()
            }.forEach {
                for (change in changes) {
                    it.onSharedPreferenceChanged(prefs, change)
                }
            }
        }

        override fun clear(): SharedPreferences.Editor {
            delete.addAll(prefs.map.keys)
            put.clear()
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            delete.add(key)
            put.remove(key)
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            put(key, value)
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            put(key, value)
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            put(key, value)
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            put(key, value)
            return this
        }

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            if (value == null) remove(key)
            else put(key, value)
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
            if (values == null) remove(key)
            else put(key, values)
            return this
        }

        private fun put(key: String, value: Any) {
            put[key] = value
            delete.remove(key)
        }
    }
}