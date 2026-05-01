package cc.meteormc.yourmiui.xposed.mirror.feature

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

object DisableClipTip : Feature(
    key = "disable_clip_tip",
    nameRes = R.string.feature_mirror_disable_clip_tip_name,
    descriptionRes = R.string.feature_mirror_disable_clip_tip_description,
    testEnvironmentRes = R.string.feature_mirror_disable_clip_tip_test_environment
) {
    override fun onLoadPackage() {
        operator("com.xiaomi.mirror.widget.ClipTipHelper") {
            method("showToast")?.hookDoNothing()
        }
    }
}