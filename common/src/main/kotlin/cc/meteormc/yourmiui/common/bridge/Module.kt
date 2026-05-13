package cc.meteormc.yourmiui.common.bridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import cc.meteormc.yourmiui.common.util.Unsafe.cast
import cc.meteormc.yourmiui.common.util.Unsafe.getSerializableExtraCompat
import cc.meteormc.yourmiui.common.util.getExtra
import cc.meteormc.yourmiui.common.util.putExtra
import java.util.UUID

class Module(private val context: Context) : BroadcastReceiver() {
    private val pendings = mutableMapOf<UUID, ResponseCallback<Any>>()
    private val timeoutHandler = Handler(Looper.getMainLooper())

    fun attach() {
        val filter = IntentFilter().apply { addAction(Bridge.RESPONSE_ACTION) }
        ContextCompat.registerReceiver(
            context, this,
            filter, ContextCompat.RECEIVER_EXPORTED
        )
    }

    fun <REQ : Any, RES : Any> request(
        channel: Channel<REQ, RES>,
        packageName: String,
        onResponse: ResponseCallback<RES>,
        timeout: Long = 1000L,
        body: REQ = Unit.cast()
    ) {
        val id = UUID.randomUUID()
        pendings[id] = onResponse.cast()
        timeoutHandler.postDelayed({ pendings.remove(id)?.onFailure() }, timeout)

        val request = Intent(channel.action)
        request.setPackage(packageName)
        request.putExtra("id", id)
        request.putExtra("validation", packageName)
        request.putExtra("body", body)
        context.sendBroadcast(request)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getSerializableExtraCompat("id", UUID::class.java) ?: return
        val callback = pendings.remove(id) ?: return
        intent.getExtra<Any>("body")?.let { callback.onSuccess(it) }
    }
}