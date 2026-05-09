package cc.meteormc.yourmiui.xposed.contentextension

import android.content.ComponentName
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.data.RestartMethod
import cc.meteormc.yourmiui.xposed.contentextension.feature.FixLinkHandling

object ContentExtension : Scope(
    "com.miui.contentextension"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            FixLinkHandling
        )
    }

    override fun getRestartMethod() = RestartMethod.ViaComponent(
        ComponentName(
            "com.miui.contentextension",
            "com.miui.contentextension.setting.activity.MainSettingsActivity"
        )
    )
}