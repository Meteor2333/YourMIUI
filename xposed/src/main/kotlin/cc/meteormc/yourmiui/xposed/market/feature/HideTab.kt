package cc.meteormc.yourmiui.xposed.market.feature

import cc.meteormc.yourmiui.core.Option
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedOption
import org.json.JSONObject

object HideTab : XposedFeature(
    key = "market_hide_tab",
    nameRes = R.string.feature_market_hide_tab_name,
    descriptionRes = R.string.feature_market_hide_tab_description,
    testEnvironmentRes = R.string.feature_market_hide_tab_test_environment,
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
    private lateinit var keptTags: List<String>

    override fun init() {
        helper("com.xiaomi.market.model.PageConfig") {
            // modifier: private | signature: initTabs(Lorg/json/JSONObject;)V
            method("initTabs")?.hookBefore {
                val json = (it.args[0] as JSONObject).getJSONArray("tabs")
                var i = 0
                while (i < json.length()) {
                    val tag = json.getJSONObject(i).getString("tag")
                    if (keptTags.contains(tag)) i++
                    else json.remove(i)
                }
            }
        }
    }

    override fun getOptions(): Iterable<Option> {
        return listOf(
            XposedOption(
                "tab_kept",
                R.string.option_market_hide_tab_kept_tags_name,
                R.string.option_market_hide_tab_kept_tags_summary,
                Option.Type.STRING_LIST,
                listOf(
                    "native_market_home",
                    "native_market_mine"
                )
            ) { keptTags = it }
        )
    }
}