@file:Suppress("UNCHECKED_CAST")

package cc.meteormc.yourmiui.common.util

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
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
            @Suppress("UNCHECKED_CAST")
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
                        (value as Collection<Parcelable>).toCollection(ArrayList())
                    )
                }
                Serializable::class.java.isAssignableFrom(type) -> {
                    putString($$"$$name$type", "List<Serializable>")
                    putSerializable($$"$$name$element", type)
                    putSerializable(
                        name,
                        (value as Collection<Serializable>).toCollection(ArrayList())
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
    @Suppress("DEPRECATION")
    return when (getString($$"$$name$type")) {
        "Unit" -> Unit
        "List<Empty>" -> {
            val size = getInt($$"$$name$size")
            arrayListOf<Any?>().apply { repeat(size) { add(null) } }
        }
        "List<Parcelable>" -> getParcelableArrayList<Parcelable>(name)
        "List<Serializable>" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializable(name, ArrayList::class.java)
        } else {
            getSerializable(name)
        }
        "Bundle" -> getBundle(name)
        "Parcelable" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelable(name, Parcelable::class.java)
        } else {
            getParcelable(name)
        }
        "Serializable" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializable(name, Serializable::class.java)
        } else {
            getSerializable(name)
        }

        else -> null
    } as? T
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
            @Suppress("UNCHECKED_CAST")
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
                        (value as Collection<Parcelable>).toCollection(ArrayList())
                    )
                }
                Serializable::class.java.isAssignableFrom(type) -> {
                    putExtra($$"$$name$type", "List<Serializable>")
                    putExtra(name, (value as Collection<Serializable>).toCollection(ArrayList()))
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
    @Suppress("DEPRECATION")
    return when (getStringExtra($$"$$name$type")) {
        "Unit" -> Unit
        "List<Empty>" -> {
            val size = getIntExtra($$"$$name$size", 0)
            arrayListOf<Any?>().apply { repeat(size) { add(null) } }
        }
        "List<Parcelable>" -> getParcelableArrayListExtra<Parcelable>(name)
        "List<Serializable>" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializableExtra(name, ArrayList::class.java)
        } else {
            getSerializableExtra(name)
        }
        "Bundle" -> getBundleExtra(name)
        "Parcelable" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(name, Parcelable::class.java)
        } else {
            getParcelableExtra(name)
        }
        "Serializable" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializableExtra(name, Serializable::class.java)
        } else {
            getSerializableExtra(name)
        }

        else -> null
    } as T?
}