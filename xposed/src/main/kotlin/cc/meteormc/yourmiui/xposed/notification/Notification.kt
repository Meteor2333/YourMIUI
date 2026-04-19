package cc.meteormc.yourmiui.xposed.notification

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.notification.feature.DisableForceNotification

object Notification : XposedScope(
    "com.miui.notification"
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            DisableForceNotification
        )
    }
}