package cc.meteormc.yourmiui.xposed.nfc

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.xposed.nfc.feature.AllowLockedAccess

object NFC : Scope(
    "com.android.nfc" to null
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            AllowLockedAccess
        )
    }
}