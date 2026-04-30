package cc.meteormc.yourmiui.common.bridge

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.io.Serializable
import java.util.*

class Module(private val context: Context) : BroadcastReceiver() {
    private val pendings = mutableMapOf<UUID, ResponseCallback<Serializable>>()
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
    fun <REQ: Serializable, RES: Serializable> request(
        channel: Channel<REQ, RES>,
        packageName: String,
        onResponse: ResponseCallback<RES>,
        timeout: Long = 1000L,
        body: REQ = Bridge.EmptyBody as REQ
    ) {
        val id = UUID.randomUUID()
        pendings[id] = onResponse as ResponseCallback<Serializable>
        timeoutHandler.postDelayed({ pendings.remove(id)?.onFailure() }, timeout)

        val request = Intent(channel.action)
        request.setPackage(packageName)
        request.putExtra("id", id)
        request.putExtra("body", body)
        request.putExtra("validation", packageName)
        context.sendBroadcast(request)
    }

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getSerializableExtra("id") ?: return
        val body = intent.getSerializableExtra("body") ?: return

        pendings.remove(id)?.onSuccess(body)
    }
}