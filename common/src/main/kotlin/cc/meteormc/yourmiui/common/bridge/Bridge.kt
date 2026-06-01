package cc.meteormc.yourmiui.common.bridge

object Bridge {
    internal const val RESPONSE_ACTION = "cc.meteormc.yourmiui.ACTION_RESPONSE"
    internal const val REQUIRED_PERMISSION = "cc.meteormc.yourmiui.permission.USE_BRIDGE"

    // todo: 标记一下 以后改个名
    val RESTART_SCOPE_CHANNEL = Channel<Unit, Unit>("cc.meteormc.yourmiui.ACTION_RESTART_SCOPE")

    var apiName: String? = null
        private set
    var apiVersion: Int? = null
        private set
}