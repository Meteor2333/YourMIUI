package cc.meteormc.yourmiui.common.util

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import cc.meteormc.yourmiui.common.util.Unsafe.cast
import cc.meteormc.yourmiui.common.util.Unsafe.getParcelableArrayListCompat
import cc.meteormc.yourmiui.common.util.Unsafe.getParcelableArrayListExtraCompat
import cc.meteormc.yourmiui.common.util.Unsafe.getParcelableCompat
import cc.meteormc.yourmiui.common.util.Unsafe.getParcelableExtraCompat
import cc.meteormc.yourmiui.common.util.Unsafe.getSerializableCompat
import cc.meteormc.yourmiui.common.util.Unsafe.getSerializableExtraCompat
import cc.meteormc.yourmiui.common.util.Unsafe.safeCast
import java.io.Serializable

fun Bundle.putObject(name: String, value: Any) {
    Log.d("StorageUtil", "Putting object: name=$name, value=$value, type=${value.javaClass}")
    when (value) {
        is Unit -> {
            putString($$"$$name$type", "Unit")
            putString(name, "(≧∀≦)ゞ")
        }
        is List<*> -> {
            val type = value.firstOrNull()?.javaClass
            when {
                type == null -> {
                    putString($$"$$name$type", "List<Void>")
                    putInt($$"$$name$size", value.size)
                    putString(name, "q(≧▽≦q)")
                }
                Parcelable::class.java.isAssignableFrom(type) -> {
                    putString($$"$$name$type", "List<Parcelable>")
                    putSerializable($$"$$name$element", type)
                    putParcelableArrayList(
                        name,
                        value.cast<Collection<Parcelable>>().toCollection(ArrayList())
                    )
                }
                Serializable::class.java.isAssignableFrom(type) -> {
                    putString($$"$$name$type", "List<Serializable>")
                    putSerializable($$"$$name$element", type)
                    putSerializable(
                        name,
                        value.cast<Collection<Serializable>>().toCollection(ArrayList())
                    )
                }
                else -> throw IllegalArgumentException("Unsupported collection element type: $type")
            }

        }
        is Bundle -> {
            putString($$"$$name$type", "Bundle")
            putBundle(name, value)
        }
        is IBinder -> {
            putString($$"$$name$type", "IBinder")
            putBinder(name, value)
        }
        is Parcelable -> {
            putString($$"$$name$type", "Parcelable")
            putParcelable(name, value)
        }
        is Serializable -> {
            putString($$"$$name$type", "Serializable")
            putSerializable(name, value)
        }
        else -> throw IllegalArgumentException("Unsupported body type: ${value.javaClass}")
    }
}

fun <T : Any> Bundle.getObject(name: String): T? {
    return when (getString($$"$$name$type")) {
        "Unit" -> Unit
        "List<Empty>" -> {
            val size = getInt($$"$$name$size")
            arrayListOf<Any?>().apply { repeat(size) { add(null) } }
        }
        "List<Parcelable>" -> getParcelableArrayListCompat<Parcelable>(name)
        "List<Serializable>" -> getSerializableCompat(name, ArrayList::class.java)
        "Bundle" -> getBundle(name)
        "Parcelable" -> getParcelableCompat(name)
        "Serializable" -> getSerializableCompat(name)
        else -> null
    }.safeCast()
}

fun Intent.putExtra(name: String, value: Any) {
    Log.d("StorageUtil", "Putting extra: name=$name, value=$value, type=${value.javaClass}")
    when (value) {
        is Unit -> {
            putExtra($$"$$name$type", "Unit")
            putExtra(name, "ヾ(≧▽≦*)ゝ")
        }
        is List<*> -> {
            val type = value.firstOrNull()?.javaClass
            when {
                type == null -> {
                    putExtra($$"$$name$type", "List<Void>")
                    putExtra($$"$$name$size", value.size)
                    putExtra(name, "q(≧▽≦q)")
                }
                Parcelable::class.java.isAssignableFrom(type) -> {
                    putExtra($$"$$name$type", "List<Parcelable>")
                    putParcelableArrayListExtra(
                        name,
                        value.cast<Collection<Parcelable>>().toCollection(ArrayList())
                    )
                }
                Serializable::class.java.isAssignableFrom(type) -> {
                    putExtra($$"$$name$type", "List<Serializable>")
                    putExtra(name, value.cast<Collection<Serializable>>().toCollection(ArrayList()))
                }
                else -> throw IllegalArgumentException("Unsupported collection element type: $type")
            }

        }
        is Bundle -> {
            putExtra($$"$$name$type", "Bundle")
            putExtra(name, value)
        }
        is Parcelable -> {
            putExtra($$"$$name$type", "Parcelable")
            putExtra(name, value)
        }
        is Serializable -> {
            putExtra($$"$$name$type", "Serializable")
            putExtra(name, value)
        }
        else -> throw IllegalArgumentException("Unsupported body type: ${value.javaClass}")
    }
}

fun <T : Any> Intent.getExtra(name: String): T? {
    return when (getStringExtra($$"$$name$type")) {
        "Unit" -> Unit
        "List<Empty>" -> {
            val size = getIntExtra($$"$$name$size", 0)
            arrayListOf<Any?>().apply { repeat(size) { add(null) } }
        }
        "List<Parcelable>" -> getParcelableArrayListExtraCompat<Parcelable>(name)
        "List<Serializable>" -> getSerializableExtraCompat(name, ArrayList::class.java)
        "Bundle" -> getBundleExtra(name)
        "Parcelable" -> getParcelableExtraCompat(name)
        "Serializable" -> getSerializableExtraCompat(name)
        else -> null
    }.safeCast()
}