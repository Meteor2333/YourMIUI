package cc.meteormc.yourmiui.core.bridge

import cc.meteormc.yourmiui.core.Scope
import java.io.Serializable

object Bridge {
    internal const val RESPONSE_ACTION = "cc.meteormc.yourmiui.ACTION_RESPONSE"
    internal const val REQUIRED_PERMISSION = "cc.meteormc.yourmiui.permission.USE_BRIDGE"

    val API_NAME_CHANNEL = Channel<EmptyBody, String>("cc.meteormc.yourmiui.ACTION_GET_API_NAME")
    val API_VERSION_CHANNEL = Channel<EmptyBody, Int>("cc.meteormc.yourmiui.ACTION_GET_API_VERSION")
    val SCOPES_CHANNEL = Channel<EmptyBody, ArrayList<Scope>>("cc.meteormc.yourmiui.ACTION_GET_SCOPES")
    val RESTART_SCOPE_CHANNEL = Channel<EmptyBody, EmptyBody>("cc.meteormc.yourmiui.ACTION_RESTART_SCOPE")

    object EmptyBody : Serializable
}