package cc.meteormc.yourmiui.common.bridge

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

object IntentExt {
    fun Intent.putExtra(name: String, value: Any) {
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
                    java.io.Serializable::class.java.isAssignableFrom(type) -> {
                        putExtra($$"$$name$type", "List<Serializable>")
                        putExtra(name, (value as Collection<java.io.Serializable>).toCollection(ArrayList()))
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
}