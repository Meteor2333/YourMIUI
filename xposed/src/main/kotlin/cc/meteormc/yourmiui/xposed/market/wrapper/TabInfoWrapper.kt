package cc.meteormc.yourmiui.xposed.market.wrapper

class TabInfoWrapper : Wrapper("com.xiaomi.market.model.TabInfo") {
    var tag: String?
        get() = getField("tag")
        set(value) = updateField("tag", value)
    var subTabs: List<Any>
        get() = getField("subTabs") ?: emptyList()
        set(value) = updateField("subTabs", value)
}