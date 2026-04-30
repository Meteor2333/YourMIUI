package cc.meteormc.yourmiui.common.bridge

import java.io.Serializable

fun interface ChannelHandler<REQ: Serializable, RES: Serializable> {
    fun handle(request: REQ): RES
}