package cc.meteormc.yourmiui.xposed.nfc

import android.os.Message
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.operator

object AllowLockedAccess : Feature() {
    private const val SCREEN_STATE_ON_UNLOCKED = 8
    private const val MSG_APPLY_SCREEN_STATE = 16

    override fun onLoadPackage(packageName: String) {
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