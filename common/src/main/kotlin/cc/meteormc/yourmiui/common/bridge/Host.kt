package cc.meteormc.yourmiui.common.bridge

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import cc.meteormc.yourmiui.common.util.getExtra
import cc.meteormc.yourmiui.common.util.putExtra

class Host(private val context: Context) : BroadcastReceiver() {
    private val filter = IntentFilter()
    private val handlers = mutableMapOf<String, ChannelHandler<Any, Any>>()

    fun attach() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(this, filter, Bridge.REQUIRED_PERMISSION, null, Context.RECEIVER_EXPORTED)
        } else {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(this, filter, Bridge.REQUIRED_PERMISSION, null)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <REQ : Any, RES : Any> register(
        channel: Channel<REQ, RES>,
        vararg packages: String = arrayOf(context.packageName),
        handler: ChannelHandler<REQ, RES>
    ): Host {
        if (!packages.contains(context.packageName)) return this
        val action = channel.action
        filter.addAction(action)
        handlers[action] = handler as ChannelHandler<Any, Any>
        return this
    }

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getSerializableExtra("id") ?: return
        val validation = intent.getStringExtra("validation") ?: return
        if (context.packageName != validation) {
            return
        }

        val handler = handlers[intent.action] ?: return
        val body = intent.getExtra<Any>("body")?.let { handler.handle(it) } ?: return
        val response = Intent(Bridge.RESPONSE_ACTION)
        response.putExtra("id", id)
        response.putExtra("body", body)
        context.sendBroadcast(response, Bridge.REQUIRED_PERMISSION)
    }
}