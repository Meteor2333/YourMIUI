package cc.meteormc.yourmiui.app.settings

import cc.meteormc.yourmiui.app.App
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.app.settings.hook.RemoveNewVersionTag

object Settings : App(
    "com.android.settings"
) {
    override fun getHooks(): Iterable<Hook> {
        return listOf(
            RemoveNewVersionTag
        )
    }
}