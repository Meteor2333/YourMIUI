package cc.meteormc.yourmiui.xposed.market.feature

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.MARTET,
    "@string/feature_market_disable_notification_recall_name",
    "@string/feature_market_disable_notification_recall_description"
)
@RequiredScope("com.xiaomi.market")
object DisableNotificationRecall : Feature(
    key = "disable_notification_recall",
    nameRes = R.string.feature_disable_notification_recall_name,
    descriptionRes = R.string.feature_disable_notification_recall_description,
    testEnvironmentRes = R.string.feature_disable_notification_recall_test_environment
), FeatureHooker {
    override fun hook(packageName: String) {
        operator("com.xiaomi.market.data.NotificationRecallController") {
            // modifier: public static | signature: tryShowDialog(Lcom/xiaomi/market/ui/BaseActivity;I)Z
            method("tryShowDialog")?.hookDoNothing()
        }
    }
}