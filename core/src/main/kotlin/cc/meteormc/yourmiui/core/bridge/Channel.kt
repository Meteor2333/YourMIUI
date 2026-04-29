package cc.meteormc.yourmiui.core.bridge

import java.io.Serializable

data class Channel<REQ: Serializable, RES: Serializable>(
    val action: String
)