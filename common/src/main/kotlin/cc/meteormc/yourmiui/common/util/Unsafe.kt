package cc.meteormc.yourmiui.common.util

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

object Unsafe {
    fun <T> Any?.cast(): T {
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    fun <T> Any?.safeCast(): T? {
        @Suppress("UNCHECKED_CAST")
        return this as? T
    }

    fun <T : Serializable> Bundle.getSerializableCompat(
        key: String,
        clazz: Class<T> = Serializable::class.java.cast()
    ): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializable(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            getSerializable(key)
        }.safeCast()
    }

    fun <T : Parcelable> Bundle.getParcelableCompat(
        key: String,
        clazz: Class<T> = Parcelable::class.java.cast()
    ): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelable(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            getParcelable<T>(key)
        }
    }

    fun <T : Parcelable> Bundle.getParcelableArrayListCompat(
        key: String,
        clazz: Class<T> = Parcelable::class.java.cast()
    ): ArrayList<T>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableArrayList(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            getParcelableArrayList<T>(key)
        }
    }

    fun <T : Serializable> Intent.getSerializableExtraCompat(
        key: String,
        clazz: Class<T> = Serializable::class.java.cast()
    ): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializableExtra(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            getSerializableExtra(key)
        }.safeCast()
    }

    fun <T : Parcelable> Intent.getParcelableExtraCompat(
        key: String,
        clazz: Class<T> = Parcelable::class.java.cast()
    ): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra<T>(key)
        }
    }

    fun <T : Parcelable> Intent.getParcelableArrayListExtraCompat(
        key: String,
        clazz: Class<T> = Parcelable::class.java.cast()
    ): ArrayList<T>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableArrayListExtra(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            getParcelableArrayListExtra<T>(key)
        }
    }
}