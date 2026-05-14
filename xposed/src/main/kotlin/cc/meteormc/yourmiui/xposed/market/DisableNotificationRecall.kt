package cc.meteormc.yourmiui.xposed.market

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.operator

object DisableNotificationRecall : Feature() {
    override fun onLoadPackage() {
        operator("com.xiaomi.market.data.NotificationRecallController") {
            // modifier: public static | signature: tryShowDialog(Lcom/xiaomi/market/ui/BaseActivity;I)Z
            method("tryShowDialog")?.hookDoNothing()
        }
    }
}