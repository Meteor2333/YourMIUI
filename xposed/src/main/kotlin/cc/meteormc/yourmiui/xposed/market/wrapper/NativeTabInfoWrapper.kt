package cc.meteormc.yourmiui.xposed.market.wrapper

class NativeTabInfoWrapper : Wrapper("com.xiaomi.market.common.network.retrofit.response.bean.NativeTabInfo") {
    var tag: String?
        get() = getField("tag")
        set(value) = updateField("tag", value)
}