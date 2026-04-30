package cc.meteormc.yourmiui.common.bridge

import java.io.Serializable

data class Channel<REQ: Serializable, RES: Serializable>(
    val action: String
)