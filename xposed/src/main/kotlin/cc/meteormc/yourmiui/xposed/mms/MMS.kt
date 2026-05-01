package cc.meteormc.yourmiui.xposed.mms

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.xposed.mms.feature.RemoveAds

object MMS : Scope(
    "com.android.mms" to "com.android.mms.ui.MmsTabActivity"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            RemoveAds
        )
    }
}