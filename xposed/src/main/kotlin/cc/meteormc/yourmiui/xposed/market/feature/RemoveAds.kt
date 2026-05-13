package cc.meteormc.yourmiui.xposed.market.feature

import android.content.Intent
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.common.Option.Type
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.market.wrapper.NativeTabInfoWrapper
import cc.meteormc.yourmiui.xposed.market.wrapper.TabInfoWrapper
import cc.meteormc.yourmiui.xposed.operator
import org.json.JSONArray
import org.json.JSONObject

object RemoveAds : Feature(
    key = "remove_market_ads",
    nameRes = R.string.feature_market_remove_ads_name,
    descriptionRes = R.string.feature_market_remove_ads_description,
    testEnvironmentRes = R.string.feature_market_remove_ads_test_environment
) {
    private lateinit var hiddenTags: Set<String>

    private val adTags = setOf(
        "native_market_recommend",
        "native_market_mine#native_market_myFavorite"
    )
    private val adComponents = setOf(
        "com.xiaomi.market.common.component.componentbeans.RecAppsComponent",
        "com.xiaomi.market.common.component.componentbeans.NativeSearchTopAdNormalAppComponent",
        "com.xiaomi.market.common.component.componentbeans.RecommendCollectionComponent"
    )

    override fun onLoadPackage() {
        operator("com.xiaomi.market.model.TabInfo") {
            val tabInfo = TabInfoWrapper()
            // modifier: public static | signature: fromJSON(Lorg/json/JSONArray;)Ljava/util/List;
            method("fromJSON", JSONArray::class.java)?.hookAfter {
                it.result<MutableList<Any>>()?.removeIf { tab ->
                    tabInfo.from(tab)
                    hiddenTags.contains(tabInfo.tag)
                }
            }
        }

        operator("com.xiaomi.market.ui.PagerTabsInfo") {
            // modifier: public | signature: fromNativeTabs(Ljava/util/List;)Lcom/xiaomi/market/ui/PagerTabsInfo;
            method("fromNativeTabs")?.hookBefore {
                it.argByGenerics<MutableList<Any>>()?.removeIf { tab ->
                    val wrapper = NativeTabInfoWrapper()
                    wrapper.from(tab)
                    adTags.contains(wrapper.tag)
                }
            }

            val tabInfo = TabInfoWrapper()
            val subTabInfo = TabInfoWrapper()
            val emptyTabInfo by lazy {
                TabInfoWrapper().new(classLoader)!!
            }
            // modifier: public static | signature: fromTabInfo(Lcom/xiaomi/market/model/TabInfo;)Lcom/xiaomi/market/ui/PagerTabsInfo;
            method("fromTabInfo")?.hookBefore {
                val tabInfoClass = operator(tabInfo.className)?.delegate ?: return@hookBefore
                val arg = it.argByClass(tabInfoClass) ?: return@hookBefore
                tabInfo.from(arg)

                val subTabs = tabInfo.subTabs.toMutableList()
                subTabs.removeIf { tab ->
                    subTabInfo.from(tab)
                    adTags.contains("${tabInfo.tag}#${subTabInfo.tag}")
                }

                // 放一个空TabInfo
                // 以防止com.xiaomi.market.model.PageConfig#getBottomFragmentClazz()处的检查出错
                subTabs.add(emptyTabInfo)
                tabInfo.subTabs = subTabs
            }
        }

        operator("com.xiaomi.market.business_ui.base.NativeFeedFragment") {
            // modifier: protected | signature: parseResponseData(Lorg/json/JSONObject;Z)Ljava/util/List;
            method("parseResponseData")?.hookAfter {
                it.result<MutableList<Any>>()?.removeIf { component ->
                    adComponents.contains(component.javaClass.name)
                }
            }
        }

        operator("com.xiaomi.market.business_ui.main.MarketTabActivity") {
            setOf(
                // modifier: private | signature: tryShowRecallReCommend()V
                "tryShowRecallReCommend",
                // modifier: private | signature: trySplash()V
                "trySplash",
                // modifier: private | signature: fetchSearchHotList()V
                "fetchSearchHotList"
            ).forEach {
                method(it)?.hookResult(null)
            }
        }

        operator("com.xiaomi.market.common.network.retrofit.response.bean.AppDetailV3") {
            setOf(
                // modifier: public final | signature: isBrowserMarketAdOff()Z
                "isBrowserMarketAdOff",
                // modifier: public final | signature: isBrowserSourceFileAdOff()Z
                "isBrowserSourceFileAdOff"
            ).forEach {
                method(it)?.hookResult(true)
            }

            setOf(
                // modifier: public final | signature: isInternalAd()Z
                "isInternalAd",
                // modifier: public final | signature: needShowAds()Z
                "needShowAds",
                // modifier: public final | signature: needShowAdsWithSourceFile()Z
                "needShowAdsWithSourceFile",
                // modifier: public final | signature: showComment()Z
                "showComment",
                // modifier: public final | signature: showRecommend()Z
                "showRecommend",
                // modifier: public final | signature: showTopBanner()Z
                "showTopBanner",
                // modifier: public final | signature: showTopVideo()Z
                "showTopVideo",
                // modifier: public final | signature: getShowOpenScreenAd()Z
                "getShowOpenScreenAd"
            ).forEach {
                method(it)?.hookResult(false)
            }
        }

        // 搜索页推荐广告
        operator("com.xiaomi.market.business_ui.base.NativeViewModel") {
            val keptComponentClass = operator(
                "com.xiaomi.market.common.component.componentbeans.SearchHistoryComponent"
            )?.delegate ?: return@operator
            // modifier: private final | signature: modifySearchSugData(Lcom/xiaomi/market/common/component/base/INativeFragmentContext;Ljava/util/List;)V
            method("modifySearchSugData")?.hookBefore {
                val components = it.argByGenerics<MutableList<Any>>() ?: return@hookBefore
                val keptComponents = components.filter { component -> keptComponentClass.isInstance(component) }
                if (keptComponents.isNotEmpty()) {
                    components.retainAll(keptComponents)
                }
            }
        }

        // 软件页轮播广告
        operator("com.xiaomi.market.common.webview.WebEvent") {
            // modifier: public | signature: sendDataToCallback(Ljava/lang/String;Ljava/lang/String;)V
            method("sendDataToCallback")?.hookBefore {
                val callback = it.stringArg(0) ?: return@hookBefore
                val data = it.stringArg(1) ?: return@hookBefore
                if (!callback.contains("software_sub5")) return@hookBefore

                val json = JSONObject(data)
                val list = json.getJSONArray("list")

                var i = 0
                while (i < list.length()) {
                    val element = list.getJSONObject(i)
                    if (element.getString("type") != "carouselRecommend") {
                        i++
                        continue
                    }

                    list.remove(i)
                }

                it.stringArg(callback, 0)
                it.stringArg(json.toString(), 1)
            }
        }

        // 个人页横幅广告
        operator("com.xiaomi.market.business_ui.main.mine.NativeMinePagerFragment") {
            // modifier: private final | signature: parseMenuData(Lorg/json/JSONArray;)Ljava/util/Map;
            method("parseMenuData")?.hookResult(emptyMap<Any, Any>())
        }

        // 下载队列页推荐广告
        operator("com.xiaomi.market.ui.DownloadListFragment") {
            // modifier: private final | signature: parseRecommendGroupResult(Lorg/json/JSONObject;)Lcom/xiaomi/market/viewmodels/RecommendGroupResult;
            method("parseRecommendGroupResult")?.hookResult(null)
        }

        // 应用升级页推荐广告
        operator("com.xiaomi.market.ui.UpdateListRvAdapter") {
            // modifier: private final | signature: generateRecommendGroupItems(Ljava/util/ArrayList;I)Z
            method("generateRecommendGroupItems")?.hookResult(null)
        }

        // 应用详情页推荐广告
        operator($$"com.xiaomi.market.business_ui.detail.DetailType$Companion") {
            // modifier: public final | signature: getDetailType(Landroid/content/Intent;Ljava/lang/Boolean;Ljava/lang/Boolean;)Lcom/xiaomi/market/business_ui/detail/DetailType;
            method(
                "getDetailType",
                Intent::class.java,
                Boolean::class.javaPrimitiveType!!,
                Boolean::class.javaPrimitiveType!!
            )?.hookResult(
                operator("com.xiaomi.market.business_ui.detail.DetailType") {
                    // name: UNKNOWN | type: com.xiaomi.market.business_ui.detail.DetailType
                    field("UNKNOWN")?.get(null)
                }
            )
        }
    }

    override fun getOptions(): List<Option<*>> {
        return listOf(
            Option(
                "hidden_tags",
                R.string.option_market_remove_ads_hidden_tags_name,
                R.string.option_market_remove_ads_hidden_tags_summary,
                Type.MultiChoiceList(
                    "native_market_home" to R.string.option_market_remove_ads_hidden_tags_home,
                    "native_market_video" to R.string.option_market_remove_ads_hidden_tags_video,
                    "native_market_agent" to R.string.option_market_remove_ads_hidden_tags_agent,
                    "native_app_assemble" to R.string.option_market_remove_ads_hidden_tags_assemble,
                    "native_market_game" to R.string.option_market_remove_ads_hidden_tags_game,
                    "native_market_rank" to R.string.option_market_remove_ads_hidden_tags_rank,
                    "software_sub5" to R.string.option_market_remove_ads_hidden_tags_software,
                    "native_market_mine" to R.string.option_market_remove_ads_hidden_tags_mine
                ),
                setOf(
                    "native_market_video",
                    "native_market_agent",
                    "native_app_assemble",
                    "native_market_game",
                    "native_market_rank"
                )
            ) { hiddenTags = it }
        )
    }
}