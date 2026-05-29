package cc.meteormc.yourmiui.xposed.mms

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.MMS,
    "@string/feature_mms_remove_ads_name",
    "@string/feature_mms_remove_ads_description"
)
@RequiredScope("com.android.mms")
object RemoveAds : Feature(
    key = "remove_mms_ads",
    nameRes = R.string.feature_mms_remove_ads_name,
    descriptionRes = R.string.feature_mms_remove_ads_description,
    testEnvironmentRes = R.string.feature_mms_remove_ads_test_environment
), FeatureHooker {
    override fun hook(packageName: String) {
        operator("com.miui.smsextra.ui.UnderstandButton") {
            // modifier: private | signature: needRequestAD(Ljava/lang/Object;Lcom/miui/smsextra/sdk/ItemExtra;Lcom/miui/smsextra/ui/UnderstandButton$ADCallback;)Z
            method("needRequestAD")?.hookResult(false)
        }

        operator("com.miui.smsextra.ui.BottomMenu") {
            // modifier: public | signature: requestMenu(Lcom/miui/smsextra/ui/BottomMenuOnLoadDataTaskCallBack;)V
            method("requestMenu")?.hookDoNothing()

            // modifier: public | signature: setCallBack(Lcom/miui/smsextra/ui/BottomMenu$OnLoadDataTaskCallBack;)V
            method("setCallBack")?.hookDoNothing()
        }
    }
}