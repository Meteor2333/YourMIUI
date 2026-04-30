package cc.meteormc.yourmiui.xposed.mms

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.mms.feature.RemoveAds

object MMS : XposedScope(
    "com.android.mms" to "com.android.mms.ui.MmsTabActivity"
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            RemoveAds
        )
    }
}