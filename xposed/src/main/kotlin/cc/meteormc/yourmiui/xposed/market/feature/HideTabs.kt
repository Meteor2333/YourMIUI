package cc.meteormc.yourmiui.xposed.market.feature

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.common.Option.Type
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.findArg
import cc.meteormc.yourmiui.xposed.operator
import org.json.JSONObject

object HideTabs : Feature(
    key = "hide_market_tabs",
    nameRes = R.string.feature_market_hide_tabs_name,
    descriptionRes = R.string.feature_market_hide_tabs_description,
    testEnvironmentRes = R.string.feature_market_hide_tabs_test_environment,
    originalAuthor = "owo233"
) {
    private lateinit var reservedTags: Set<String>

    override fun onLoadPackage() {
        operator("com.xiaomi.market.model.PageConfig") {
            // modifier: private | signature: initTabs(Lorg/json/JSONObject;)V
            method("initTabs")?.hookBefore {
                val json = it.findArg(JSONObject::class.java)?.getJSONArray("tabs") ?: return@hookBefore
                var i = 0
                while (i < json.length()) {
                    val tag = json.getJSONObject(i).getString("tag")
                    if (reservedTags.contains(tag)) i++
                    else json.remove(i)
                }
            }
        }
    }

    override fun getOptions(): List<Option<Set<String>>> {
        return listOf(
            Option(
                "reserved_tags",
                R.string.option_market_hide_tabs_reserved_tags_name,
                R.string.option_market_hide_tabs_reserved_tags_summary,
                Type.MultiChoiceList(
                    "native_market_home" to R.string.option_market_hide_tabs_reserved_tags_home,
                    "native_market_video" to R.string.option_market_hide_tabs_reserved_tags_video,
                    "native_market_agent" to R.string.option_market_hide_tabs_reserved_tags_agent,
                    "native_app_assemble" to R.string.option_market_hide_tabs_reserved_tags_assemble,
                    "native_market_game" to R.string.option_market_hide_tabs_reserved_tags_game,
                    "native_market_rank" to R.string.option_market_hide_tabs_reserved_tags_rank,
                    "native_market_mine" to R.string.option_market_hide_tabs_reserved_tags_mine
                ),
                setOf(
                    "native_market_home",
                    "native_market_mine"
                )
            ) { reservedTags = it }
        )
    }
}