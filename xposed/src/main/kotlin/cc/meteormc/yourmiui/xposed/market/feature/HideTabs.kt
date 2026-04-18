package cc.meteormc.yourmiui.xposed.market.feature

import cc.meteormc.yourmiui.core.Option
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedOption
import org.json.JSONObject

object HideTabs : XposedFeature(
    key = "market_hide_tabs",
    nameRes = R.string.feature_market_hide_tabs_name,
    descriptionRes = R.string.feature_market_hide_tabs_description,
    testEnvironmentRes = R.string.feature_market_hide_tabs_test_environment,
    originalAuthor = "owo233"
) {
    private lateinit var keptTags: Set<String>

    override fun onLoadPackage() {
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

    override fun getOptions(): Iterable<XposedOption<Set<String>>> {
        return listOf(
            XposedOption(
                "kept_tags",
                R.string.option_market_hide_tabs_kept_tags_name,
                R.string.option_market_hide_tabs_kept_tags_summary,
                Option.Type.MULTI_LIST(
                    "native_market_home" to R.string.option_market_hide_tabs_kept_tags_home,
                    "native_market_video" to R.string.option_market_hide_tabs_kept_tags_video,
                    "native_market_agent" to R.string.option_market_hide_tabs_kept_tags_agent,
                    "native_app_assemble" to R.string.option_market_hide_tabs_kept_tags_assemble,
                    "native_market_game" to R.string.option_market_hide_tabs_kept_tags_game,
                    "native_market_rank" to R.string.option_market_hide_tabs_kept_tags_rank,
                    "native_market_mine" to R.string.option_market_hide_tabs_kept_tags_mine
                ),
                setOf(
                    "native_market_home",
                    "native_market_mine"
                )
            ) { keptTags = it }
        )
    }
}