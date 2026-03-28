package cc.meteormc.yourmiui.app.superwallpaper

import cc.meteormc.yourmiui.app.App
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.app.superwallpaper.hook.DisablePause

object SuperWallpaper : App(
    "com.miui.miwallpaper.earth",
    "com.miui.miwallpaper.geometry",
    "com.miui.miwallpaper.mars",
    "com.miui.miwallpaper.saturn",
    "com.miui.miwallpaper.snowmountain"
) {
    override fun getHooks(): Iterable<Hook> {
        return listOf(
            DisablePause
        )
    }
}