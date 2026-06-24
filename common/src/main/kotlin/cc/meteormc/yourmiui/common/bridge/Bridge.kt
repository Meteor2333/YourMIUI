package cc.meteormc.yourmiui.common.bridge

object Bridge {
    internal const val RESPONSE_ACTION = "cc.meteormc.yourmiui.ACTION_RESPONSE"
    internal const val REQUIRED_PERMISSION = "cc.meteormc.yourmiui.permission.USE_BRIDGE"

    val FORCE_STOP_CHANNEL = Channel<Unit, Unit>("cc.meteormc.yourmiui.ACTION_FORCE_STOP")
    var NOTIFY_FEATURE_CHANGED_CHANNEL = Channel<String, Unit>("cc.meteormc.yourmiui.ACTION_NOTIFY_FEATURE_CHANGED")

    var apiName: String? = null
        private set
    var apiVersion: Int? = null
        private set
}