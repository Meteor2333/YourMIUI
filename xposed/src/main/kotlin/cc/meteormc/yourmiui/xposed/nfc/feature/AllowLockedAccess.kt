package cc.meteormc.yourmiui.xposed.nfc.feature

import android.os.Message
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.findArg

object AllowLockedAccess : XposedFeature(
    key = "nfc_allow_locked_access",
    nameRes = R.string.feature_nfc_allow_locked_access_name,
    descriptionRes = R.string.feature_nfc_allow_locked_access_description,
    testEnvironmentRes = R.string.feature_nfc_allow_locked_access_test_environment
) {
    private const val SCREEN_STATE_ON_UNLOCKED = 8
    private const val MSG_APPLY_SCREEN_STATE = 16

    override fun onLoadPackage() {
        operator($$"com.android.nfc.NfcService$NfcServiceHandler") {
            // modifier: public | signature: handleMessage(Landroid/os/Message;)V
            method("handleMessage")?.hookDoNothing {
                it.findArg(Message::class.java)?.what == MSG_APPLY_SCREEN_STATE
            }
        }

        operator("com.android.nfc.ScreenStateHelper") {
            // modifier: (default) | signature: checkScreenState()I
            method("checkScreenState")?.hookResult(SCREEN_STATE_ON_UNLOCKED)
        }
    }
}