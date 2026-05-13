package cc.meteormc.yourmiui.common.bridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import cc.meteormc.yourmiui.common.util.Unsafe.cast
import cc.meteormc.yourmiui.common.util.Unsafe.getSerializableExtraCompat
import cc.meteormc.yourmiui.common.util.getExtra
import cc.meteormc.yourmiui.common.util.putExtra
import java.util.UUID

class Host : BroadcastReceiver() {
    private val filter = IntentFilter()
    private val handlers = mutableMapOf<String, ChannelHandler<Any, Any>>()

    fun attach(context: Context) {
        ContextCompat.registerReceiver(
            context, this,
            filter, Bridge.REQUIRED_PERMISSION,
            null, ContextCompat.RECEIVER_EXPORTED
        )
    }

    fun <REQ : Any, RES : Any> register(
        channel: Channel<REQ, RES>,
        handler: ChannelHandler<REQ, RES>
    ): Host {
        val action = channel.action
        filter.addAction(action)
        handlers[action] = handler.cast()
        return this
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getSerializableExtraCompat("id", UUID::class.java) ?: return
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