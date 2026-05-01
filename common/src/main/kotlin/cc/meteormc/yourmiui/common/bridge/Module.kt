package cc.meteormc.yourmiui.common.bridge

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import cc.meteormc.yourmiui.common.util.getExtra
import cc.meteormc.yourmiui.common.util.putExtra
import java.util.*

class Module(private val context: Context) : BroadcastReceiver() {
    private val pendings = mutableMapOf<UUID, ResponseCallback<Any>>()
    private val timeoutHandler = Handler(Looper.getMainLooper())

    fun attach() {
        val filter = IntentFilter().apply { addAction(Bridge.RESPONSE_ACTION) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(this, filter, Context.RECEIVER_EXPORTED)
        } else {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(this, filter)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <REQ : Any, RES : Any> request(
        channel: Channel<REQ, RES>,
        packageName: String,
        onResponse: ResponseCallback<RES>,
        timeout: Long = 1000L,
        body: REQ = Unit as REQ
    ) {
        val id = UUID.randomUUID()
        pendings[id] = onResponse as ResponseCallback<Any>
        timeoutHandler.postDelayed({ pendings.remove(id)?.onFailure() }, timeout)

        val request = Intent(channel.action)
        request.setPackage(packageName)
        request.putExtra("id", id)
        request.putExtra("validation", packageName)
        request.putExtra("body", body)
        context.sendBroadcast(request)
    }

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getSerializableExtra("id") ?: return
        val callback = pendings.remove(id) ?: return
        intent.getExtra<Any>("body")?.let { callback.onSuccess(it) }
    }
}