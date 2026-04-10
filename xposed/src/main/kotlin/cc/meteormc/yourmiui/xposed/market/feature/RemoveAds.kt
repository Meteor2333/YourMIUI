package cc.meteormc.yourmiui.xposed.market.feature

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature

object RemoveAds : XposedFeature(
    key = "market_remove_ads",
    nameRes = R.string.feature_market_remove_ads_name,
    descriptionRes = R.string.feature_market_remove_ads_description,
    testEnvironmentRes = R.string.feature_market_remove_ads_test_environment,
    originalAuthor = "owo233"
) {
    override fun init() {
        helper("com.xiaomi.market.common.network.retrofit.response.bean.AppDetailV3") {
            setOf(
                // modifier: public,final | signature: isBrowserMarketAdOff()Z
                "isBrowserMarketAdOff",
                // modifier: public,final | signature: isBrowserSourceFileAdOff()Z
                "isBrowserSourceFileAdOff",
                // modifier: private,final | signature: supportShowCompat64bitAlert()Z
                "supportShowCompat64bitAlert"
            ).forEach {
                method(it)?.hookResult(true)
            }

            setOf(
                // modifier: public,final | signature: isInternalAd()Z
                "isInternalAd",
                // modifier: public,final | signature: needShowAds()Z
                "needShowAds",
                // modifier: public,final | signature: needShowAdsWithSourceFile()Z
                "needShowAdsWithSourceFile",
                // modifier: public,final | signature: showComment()Z
                "showComment",
                // modifier: public,final | signature: showRecommend()Z
                "showRecommend",
                // modifier: public,final | signature: showTopBanner()Z
                "showTopBanner",
                // modifier: public,final | signature: showTopVideo()Z
                "showTopVideo",
                // modifier: public | signature: equals(Ljava/lang/Object;)Z
                "equals",
                // modifier: public,final | signature: getShowOpenScreenAd()Z
                "getShowOpenScreenAd",
                // modifier: public,final | signature: hasGoldLabel()Z
                "hasGoldLabel",
                // modifier: public,final | signature: isBottomButtonLayoutType()Z
                "isBottomButtonLayoutType",
                // modifier: public,final | signature: isPersonalization()Z
                "isPersonalization",
                // modifier: public,final | signature: isTopButtonLayoutType()Z
                "isTopButtonLayoutType",
                // modifier: public,final | signature: isTopSingleTabMultiButtonType()Z
                "isTopSingleTabMultiButtonType",
                // modifier: public,final | signature: needShowGrayBtn()Z
                "needShowGrayBtn",
                // modifier: public,final | signature: needShowPISafeModeStyle()Z
                "needShowPISafeModeStyle",
                // modifier: public,final | signature: supportShowCompatAlert()Z
                "supportShowCompatAlert",
                // modifier: public,final | signature: supportShowCompatChildForbidDownloadAlert()Z
                "supportShowCompatChildForbidDownloadAlert"
            ).forEach {
                method(it)?.hookResult(false)
            }
        }

        // 主页
        helper("com.xiaomi.market.business_ui.main.MarketTabActivity") {
            setOf(
                // modifier: public | signature: tryShowRecommend()V
                "tryShowRecommend",
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

        // 搜索建议页面
        helper("com.xiaomi.market.business_ui.search.NativeSearchSugFragment") {
            // modifier: public | signature: getRequestParams()Ljava/util/Map<Ljava/lang/String;Ljava/lang/Object;>;
            method("getRequestParams")?.hookAfter {
                @Suppress("UNCHECKED_CAST")
                val baseParametersForH5ToNative = (it.result as? Map<String, Any?>)?.toMutableMap() ?: return@hookAfter
                baseParametersForH5ToNative["adFlag"] = 0
                it.result = baseParametersForH5ToNative
            }
        }

        // 搜索结果页面
        helper("com.xiaomi.market.business_ui.search.NativeSearchResultFragment") {
            val componentClass = helper("com.xiaomi.market.common.component.componentbeans.ListAppComponent") {
                delegate
            } ?: return@helper
            // modifier: public | signature: parseResponseData(Lorg/json/JSONObject;Z)Ljava/util/List<Lcom/xiaomi/market/common/component/componentbeans/BaseNativeComponent;>;
            method("parseResponseData")?.hookAfter {
                @Suppress("UNCHECKED_CAST")
                val parsedComponents = (it.result as? List<Any>)?.toMutableList() ?: return@hookAfter
                parsedComponents.retainAll { component -> componentClass.isInstance(component) }
                it.result = parsedComponents
            }
        }

        // 搜索页面
        helper("com.xiaomi.market.business_ui.search.NativeSearchGuideFragment") {
            val componentClass = helper("com.xiaomi.market.common.component.componentbeans.SearchHistoryComponent") {
                delegate
            } ?: return@helper
            // modifier: public | signature: parseResponseData(Lorg/json/JSONObject;Z)Ljava/util/List<Lcom/xiaomi/market/common/component/componentbeans/BaseNativeComponent;>;
            method("parseResponseData")?.hookAfter {
                @Suppress("UNCHECKED_CAST")
                val parsedComponents = (it.result as? List<Any>)?.toMutableList() ?: return@hookAfter
                parsedComponents.retainAll { component -> componentClass.isInstance(component) }
                it.result = parsedComponents
            }

            // modifier: public | signature: isLoadMoreEndGone()Z
            method("isLoadMoreEndGone")?.hookResult(true)
        }

        // 更新页面
        helper("com.xiaomi.market.ui.UpdateListRvAdapter") {
            val stateEnum = helper($$"com.xiaomi.market.ui.UpdateListRvAdapter$PageCollapseState") {
                // name: Expand | type: com.xiaomi.market.ui.UpdateListRvAdapter$PageCollapseState
                field("Expand")?.get(null, delegate)
            }
            // modifier: public | signature: <init>(Lcom/xiaomi/market/common/component/base/INativeFragmentContext<Lcom/xiaomi/market/ui/BaseFragment;>;)V
            declaredConstructors().forEach {
                it.hookAfter { param ->
                    val thisObj = param.thisObject
                    helper(thisObj.javaClass) {
                        field("forceExpanded")?.set(thisObj, true)
                        field("foldButtonVisible")?.set(thisObj, false)
                        field("pageCollapseState")?.set(thisObj, stateEnum)
                    }
                }
            }

            // modifier: private,final | signature: generateRecommendGroupItems(Ljava/util/ArrayList;I)Z
            method("generateRecommendGroupItems")?.hookResult(null)
        }

        // 下载页面
        helper("com.xiaomi.market.ui.DownloadListFragment") {
            // modifier: private,final | signature: parseRecommendGroupResult(Lorg/json/JSONObject;)Lcom/xiaomi/market/viewmodels/RecommendGroupResult;
            method("parseRecommendGroupResult")?.hookResult(null)
        }

        // 应用详情页面
        helper("com.xiaomi.market.ui.detail.BaseDetailActivity") {
            val typeEnum = helper("com.xiaomi.market.business_ui.detail.DetailType") {
                // name: UNKNOWN | type: com.xiaomi.market.business_ui.detail.DetailType
                field("UNKNOWN")?.get(null, delegate)
            }
            // modifier: public | signature: initParams()Landroid/os/Bundle;
            method("initParams")?.hookAfter {
                // name: detailType | type: com.xiaomi.market.business_ui.detail.DetailType
                field("detailType")?.set(it.thisObject, typeEnum)
            }
        }
    }
}