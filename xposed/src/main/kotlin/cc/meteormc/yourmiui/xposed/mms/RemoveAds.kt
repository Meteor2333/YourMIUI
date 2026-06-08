package cc.meteormc.yourmiui.xposed.mms

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.api.data.HookContext
import cc.meteormc.yourmiui.xposed.hookDoNothing
import cc.meteormc.yourmiui.xposed.hookResult
import cc.meteormc.yourmiui.xposed.reflect

@FeatureRegister(
    Category.MMS,
    "@string/feature_mms_remove_ads_name",
    "@string/feature_mms_remove_ads_description"
)
@RequiredScope("com.android.mms")
object RemoveAds : FeatureHooker {
    override fun hook(context: HookContext) {
        context.reflect("com.miui.smsextra.ui.UnderstandButton") {
            // modifier: private | signature: needRequestAD(Ljava/lang/Object;Lcom/miui/smsextra/sdk/ItemExtra;Lcom/miui/smsextra/ui/UnderstandButton$ADCallback;)Z
            method("needRequestAD")?.hookResult(false)
        }

        context.reflect("com.miui.smsextra.ui.BottomMenu") {
            // modifier: public | signature: requestMenu(Lcom/miui/smsextra/ui/BottomMenuOnLoadDataTaskCallBack;)V
            method("requestMenu")?.hookDoNothing()

            // modifier: public | signature: setCallBack(Lcom/miui/smsextra/ui/BottomMenu$OnLoadDataTaskCallBack;)V
            method("setCallBack")?.hookDoNothing()
        }
    }
}