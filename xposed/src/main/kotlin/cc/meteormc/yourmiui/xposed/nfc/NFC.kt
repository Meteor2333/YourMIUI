package cc.meteormc.yourmiui.xposed.nfc

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.nfc.feature.AllowLockedAccess

object NFC : XposedScope(
    "com.android.nfc" to null
) {
    override fun getFeatures(): List<XposedFeature> {
        return listOf(
            AllowLockedAccess
        )
    }
}