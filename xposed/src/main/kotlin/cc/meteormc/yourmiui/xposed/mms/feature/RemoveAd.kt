package cc.meteormc.yourmiui.xposed.mms.feature

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature

object RemoveAd : XposedFeature(
    key = "mms_remove_ad",
    nameRes = R.string.feature_mms_remove_ad_name,
    descriptionRes = R.string.feature_mms_remove_ad_description,
    testEnvironmentRes = R.string.feature_mms_remove_ad_test_environment
) {
    override fun init() {
        helper("com.miui.smsextra.ui.UnderstandButton") {
            // modifier: private | signature: needRequestAD(Ljava/lang/Object;Lcom/miui/smsextra/sdk/ItemExtra;Lcom/miui/smsextra/ui/UnderstandButton$ADCallback;)Z
            method("needRequestAD")?.hookResult(false)
        }

        helper("com.miui.smsextra.ui.BottomMenu") {
            // modifier: public | signature: requestMenu(Lcom/miui/smsextra/ui/BottomMenuOnLoadDataTaskCallBack;)V
            method("requestMenu")?.hookDoNothing()

            // modifier: public | signature: setCallBack(Lcom/miui/smsextra/ui/BottomMenu$OnLoadDataTaskCallBack;)V
            method("setCallBack")?.hookDoNothing()
        }
    }
}