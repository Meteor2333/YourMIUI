package cc.meteormc.yourmiui.xposed.miuiplus

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.MIUI_PLUS,
    "@string/feature_miuiplus_disable_clip_tip_name",
    "@string/feature_miuiplus_disable_clip_tip_description"
)
@RequiredScope("com.xiaomi.mirror")
object DisableClipTip : Feature(
    key = "disable_clip_tip",
    nameRes = R.string.feature_mirror_disable_clip_tip_name,
    descriptionRes = R.string.feature_mirror_disable_clip_tip_description,
    testEnvironmentRes = R.string.feature_mirror_disable_clip_tip_test_environment
), FeatureHooker {
    override fun hook(packageName: String) {
        operator("com.xiaomi.mirror.widget.ClipTipHelper") {
            // modifier: private static | signature: showToast(Ljava/lang/String;)V
            method("showToast")?.hookDoNothing()
        }
    }
}