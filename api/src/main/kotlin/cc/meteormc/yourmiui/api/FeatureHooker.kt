package cc.meteormc.yourmiui.api

import cc.meteormc.yourmiui.api.data.HookContext

interface FeatureHooker {
    fun hook(context: HookContext) {

    }

    fun unhook() {

    }
}