package io.github.libxposed.api

import android.content.SharedPreferences

open class XposedInterfaceWrapper : XposedInterface {
    override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
        throw UnsupportedOperationException("WHAT!!!")
    }

    override fun getRemotePreferences(group: String): SharedPreferences {
        throw UnsupportedOperationException("WHAT!!!")
    }
}