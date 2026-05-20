package cc.meteormc.yourmiui.xposed.miuiplus

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.operator

object DisableClipTip : Feature() {
    override fun onLoadPackage(packageName: String) {
        operator("com.xiaomi.mirror.widget.ClipTipHelper") {
            // modifier: private static | signature: showToast(Ljava/lang/String;)V
            method("showToast")?.hookDoNothing()
        }
    }
}