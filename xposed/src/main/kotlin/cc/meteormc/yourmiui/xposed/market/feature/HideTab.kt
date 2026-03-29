package cc.meteormc.yourmiui.xposed.market.feature

import cc.meteormc.yourmiui.xposed.ConfigurableHookFeature
import cc.meteormc.yourmiui.xposed.ReflectHelper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.json.JSONObject

object HideTab : ConfigurableHookFeature(
    name = "market_hide_tab_name",
    description = "market_hide_tab_description",
    testEnvironment= "market_hide_tab_test_environment",
    originalAuthor = "owo233"
) {
    /**
     * native_market_mine    我的
     * native_market_home    主页
     * native_market_video   视频号
     * native_market_agent   智能体
     * native_app_assemble   应用号
     * native_market_game    游戏
     * native_market_rank    榜单
     */
    private val keptTags = setOf("native_market_home", "native_market_mine")

    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        ReflectHelper.of("com.xiaomi.market.model.PageConfig", lpparam.classLoader)?.operate {
            // modifier: private | signature: initTabs(Lorg/json/JSONObject;)V
            method("initTabs")?.hook(object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val json = (param.args[0] as JSONObject).getJSONArray("tabs")
                    var i = 0
                    while (i < json.length()) {
                        val tag = json.getJSONObject(i).getString("tag")
                        if (keptTags.contains(tag)) i++
                        else json.remove(i)
                    }
                }
            })
        }
    }
}