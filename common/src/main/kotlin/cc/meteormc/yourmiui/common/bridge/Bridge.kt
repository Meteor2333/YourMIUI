package cc.meteormc.yourmiui.common.bridge

import cc.meteormc.yourmiui.common.Scope

object Bridge {
    internal const val RESPONSE_ACTION = "cc.meteormc.yourmiui.ACTION_RESPONSE"
    internal const val REQUIRED_PERMISSION = "cc.meteormc.yourmiui.permission.USE_BRIDGE"

    val GET_SCOPES_CHANNEL = Channel<Unit, ArrayList<Scope>>("cc.meteormc.yourmiui.ACTION_GET_SCOPES")
    val RESTART_SCOPE_CHANNEL = Channel<Unit, Unit>("cc.meteormc.yourmiui.ACTION_RESTART_SCOPE")

    var apiVersion: Int? = null
        private set
    var frameworkName: String? = null
        private set
}