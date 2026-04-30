package cc.meteormc.yourmiui.common.util

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

fun Intent.putExtra(name: String, value: Any) {
    when (value) {
        is Unit -> {
            putExtra($$"$$name$type", "Unit")
            putExtra(name, "ヾ(≧▽≦*)ゝ")
        }
        is ArrayList<*> -> {
            val type = value.firstOrNull()?.javaClass
            @Suppress("UNCHECKED_CAST")
            when {
                type == null -> {
                    putExtra($$"$$name$type", "List<Empty>")
                }
                Parcelable::class.java.isAssignableFrom(type) -> {
                    putExtra($$"$$name$type", "List<Parcelable>")
                    putExtra($$"$$name$element", type)
                    putParcelableArrayListExtra(name, value as ArrayList<Parcelable>)
                }
                java.io.Serializable::class.java.isAssignableFrom(type) -> {
                    putExtra($$"$$name$type", "List<Serializable>")
                    putExtra($$"$$name$element", type)
                    putExtra(name, value as ArrayList<Serializable>)
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
        is java.io.Serializable -> {
            putExtra($$"$$name$type", "Serializable")
            putExtra(name, value)
        }
        else -> throw IllegalArgumentException("Unsupported body type: ${value.javaClass}")
    }
}

fun Intent.getExtra(name: String): Any? {
    @Suppress("DEPRECATION")
    return when (getStringExtra($$"$$name$type")) {
        "Unit" -> Unit
        "List<Empty>" -> arrayListOf<Any>()
        "List<Parcelable>" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val classLoader = javaClass.classLoader ?: return null
                val className = getStringExtra($$"$$name$element") ?: return null
                val clazz = getClass(classLoader, className, false)
                if (clazz != null) {
                    return getParcelableArrayListExtra(name, clazz)
                }
            }

            getParcelableArrayListExtra<Parcelable>(name)
        }
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
    }
}