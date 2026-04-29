package cc.meteormc.yourmiui.core.bridge

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import java.io.Serializable

class Host(private val context: Context) : BroadcastReceiver() {
    private val filter = IntentFilter()
    private val handlers = mutableMapOf<String, ChannelHandler<Serializable, Serializable>>()

    fun attach() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(this, filter, Bridge.REQUIRED_PERMISSION, null, Context.RECEIVER_EXPORTED)
        } else {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(this, filter, Bridge.REQUIRED_PERMISSION, null)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <REQ: Serializable, RES: Serializable> register(
        channel: Channel<REQ, RES>,
        packageName: String = context.packageName,
        handler: ChannelHandler<REQ, RES>
    ): Host {
        if (context.packageName != packageName) return this
        val action = channel.action
        filter.addAction(action)
        handlers[action] = handler as ChannelHandler<Serializable, Serializable>
        return this
    }

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getSerializableExtra("id") ?: return
        val body = intent.getSerializableExtra("body") ?: return
        val validation = intent.getStringExtra("validation") ?: return
        if (context.packageName != validation) {
            return
        }

        val data = handlers[intent.action]?.handle(body)
        val response = Intent(Bridge.RESPONSE_ACTION)
        response.putExtra("id", id)
        response.putExtra("body", data)
        context.sendBroadcast(response, Bridge.REQUIRED_PERMISSION)
    }
}