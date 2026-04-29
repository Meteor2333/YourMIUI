package cc.meteormc.yourmiui.core.bridge

import java.io.Serializable

fun interface ChannelHandler<REQ: Serializable, RES: Serializable> {
    fun handle(request: REQ): RES
}