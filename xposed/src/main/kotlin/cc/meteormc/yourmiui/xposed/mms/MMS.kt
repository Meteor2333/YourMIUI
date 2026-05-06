package cc.meteormc.yourmiui.xposed.mms

import android.content.ComponentName
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.data.RestartMethod
import cc.meteormc.yourmiui.xposed.mms.feature.RemoveAds

object MMS : Scope(
    "com.android.mms"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            RemoveAds
        )
    }

    override fun getRestartMethod() = RestartMethod.ViaComponent(
        ComponentName("com.android.mms", "com.android.mms.ui.MmsTabActivity")
    )
}