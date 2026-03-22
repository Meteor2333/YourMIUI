package cc.meteormc.yourmiui.app.android

import cc.meteormc.yourmiui.app.App
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.app.android.hook.BlockKillProcess

object Android: App(
    "android"
) {
    override fun getHooks(): Iterable<Hook> {
        return listOf(
            BlockKillProcess
        )
    }
}