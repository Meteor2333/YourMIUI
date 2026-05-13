package io.github.libxposed.api

import android.content.SharedPreferences

/**
 * Xposed interface for modules to operate on application processes.
 */
interface XposedInterface {
    /**
     * Gets shared preferences.
     *
     * @param name the name
     * @param mode the mode
     * @return the shared preferences
     */
    fun getSharedPreferences(name: String, mode: Int): SharedPreferences

    /**
     * Gets remote preferences stored in Xposed framework. Note that those are read-only in hooked apps.
     *
     * @param group Group name
     * @return The preferences
     * @throws UnsupportedOperationException If the framework is embedded
     */
    fun getRemotePreferences(group: String): SharedPreferences
}
