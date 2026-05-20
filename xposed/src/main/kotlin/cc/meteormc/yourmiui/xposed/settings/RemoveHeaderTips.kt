package cc.meteormc.yourmiui.xposed.settings

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.operator

object RemoveHeaderTips : Feature() {
    override fun onLoadPackage(packageName: String) {
        operator("com.android.settings.SettingsFragment") {
            // modifier: private | signature: updateTips(ZLjava/lang/String;IIILandroid/view/View$OnClickListener;)V
            method("updateTips")?.hookDoNothing()
        }
    }
}