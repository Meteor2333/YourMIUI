package cc.meteormc.yourmiui.xposed.nfc.feature

import android.os.Message
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

object AllowLockedAccess : Feature(
    key = "nfc_allow_locked_access",
    nameRes = R.string.feature_nfc_allow_locked_access_name,
    descriptionRes = R.string.feature_nfc_allow_locked_access_description,
    warningRes = R.string.feature_nfc_allow_locked_access_warning,
    testEnvironmentRes = R.string.feature_nfc_allow_locked_access_test_environment
) {
    private const val SCREEN_STATE_ON_UNLOCKED = 8
    private const val MSG_APPLY_SCREEN_STATE = 16

    override fun onLoadPackage() {
        operator($$"com.android.nfc.NfcService$NfcServiceHandler") {
            // modifier: public | signature: handleMessage(Landroid/os/Message;)V
            method("handleMessage")?.hookDoNothing {
                it.argByGenerics<Message>()?.what == MSG_APPLY_SCREEN_STATE
            }
        }

        operator("com.android.nfc.ScreenStateHelper") {
            // modifier: (default) | signature: checkScreenState()I
            method("checkScreenState")?.hookResult(SCREEN_STATE_ON_UNLOCKED)
        }
    }
}