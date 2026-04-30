package cc.meteormc.yourmiui.common.bridge

fun interface ChannelHandler<REQ : Any, RES : Any> {
    fun handle(request: REQ): RES
}