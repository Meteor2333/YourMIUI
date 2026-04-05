package cc.meteormc.yourmiui.xposed.mms

import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.mms.feature.RemoveAd

object MMS : XposedScope(
    "com.android.mms"
) {
    override fun getFeatures(): Iterable<Feature> {
        return listOf(
            RemoveAd
        )
    }
}