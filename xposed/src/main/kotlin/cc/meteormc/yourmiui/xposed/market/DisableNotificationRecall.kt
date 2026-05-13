package cc.meteormc.yourmiui.xposed.market

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

object DisableNotificationRecall : Feature(
    key = "disable_notification_recall",
    nameRes = R.string.feature_disable_notification_recall_name,
    descriptionRes = R.string.feature_disable_notification_recall_description,
    testEnvironmentRes = R.string.feature_disable_notification_recall_test_environment
) {
    override fun onLoadPackage() {
        operator("com.xiaomi.market.data.NotificationRecallController") {
            // modifier: public static | signature: tryShowDialog(Lcom/xiaomi/market/ui/BaseActivity;I)Z
            method("tryShowDialog")?.hookDoNothing()
        }
    }
}