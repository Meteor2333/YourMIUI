package cc.meteormc.yourmiui.common.bridge

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.util.getClass
import java.io.Serializable

object Bridge {
    internal const val RESPONSE_ACTION = "cc.meteormc.yourmiui.ACTION_RESPONSE"
    internal const val REQUIRED_PERMISSION = "cc.meteormc.yourmiui.permission.USE_BRIDGE"

    val GET_SCOPES_CHANNEL = Channel<Unit, ArrayList<Scope>>("cc.meteormc.yourmiui.ACTION_GET_SCOPES")
    val RESTART_SCOPE_CHANNEL = Channel<Unit, Unit>("cc.meteormc.yourmiui.ACTION_RESTART_SCOPE")

    var apiName: String? = null
        private set
    var apiVersion: Int? = null
        private set

    fun saveBody(body: Any, intent: Intent) {
        when (body) {
            is Unit -> {
                intent.putExtra("type", "Unit")
                intent.putExtra("body", "ヾ(≧▽≦*)ゝ")
            }
            is ArrayList<*> -> {
                val type = body.firstOrNull()?.javaClass
                @Suppress("UNCHECKED_CAST")
                when {
                    type == null -> {
                        intent.putExtra("type", "List<Empty>")
                    }
                    Parcelable::class.java.isAssignableFrom(type) -> {
                        intent.putExtra("type", "List<Parcelable>")
                        intent.putExtra("element", type)
                        intent.putParcelableArrayListExtra("body", body as ArrayList<Parcelable>)
                    }
                    Serializable::class.java.isAssignableFrom(type) -> {
                        intent.putExtra("type", "List<Serializable>")
                        intent.putExtra("element", type)
                        intent.putExtra("body", body as ArrayList<Serializable>)
                    }
                    else -> throw IllegalArgumentException("Unsupported collection element type: $type")
                }

            }
            is Bundle -> {
                intent.putExtra("type", "Bundle")
                intent.putExtra("body", body)
            }
            is Parcelable -> {
                intent.putExtra("type", "Parcelable")
                intent.putExtra("body", body)
            }
            is Serializable -> {
                intent.putExtra("type", "Serializable")
                intent.putExtra("body", body)
            }
            else -> throw IllegalArgumentException("Unsupported body type: ${body.javaClass}")
        }
    }

    fun parseBody(intent: Intent): Any? {
        @Suppress("DEPRECATION")
        return when (intent.getStringExtra("type")) {
            "Unit" -> Unit
            "List<Empty>" -> arrayListOf<Any>()
            "List<Parcelable>" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val classLoader = javaClass.classLoader ?: return null
                    val className = intent.getStringExtra("class") ?: return null
                    val clazz = getClass(classLoader, className, false)
                    if (clazz != null) {
                        return intent.getParcelableArrayListExtra("body", clazz)
                    }
                }

                intent.getParcelableArrayListExtra<Parcelable>("body")
            }
            "List<Serializable>" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("body", ArrayList::class.java)
            } else {
                intent.getSerializableExtra("body")
            }
            "Bundle" -> intent.getBundleExtra("body")
            "Parcelable" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("body", Parcelable::class.java)
            } else {
                intent.getParcelableExtra("body")
            }
            "Serializable" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("body", Serializable::class.java)
            } else {
                intent.getSerializableExtra("body")
            }

            else -> null
        }
    }
}