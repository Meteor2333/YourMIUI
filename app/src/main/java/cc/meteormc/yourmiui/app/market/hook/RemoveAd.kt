package cc.meteormc.yourmiui.app.market.hook

import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.callbacks.XC_LoadPackage

object RemoveAd: Hook(
    name = "去广告",
    description = "去除应用商店中的各种广告和赞助商软件推荐",
    testEnvironment= "4.112.7版本",
    originalAuthor = "owo233"
) {
    private var classLoader: ClassLoader? = null
    
    private val CLASS_LIST_APP_COMPONENT by lazy {
        ReflectHelper.of("com.xiaomi.market.common.component.componentbeans.ListAppComponent", classLoader)?.delegate
    }
    private val CLASS_SEARCH_HISTORY_COMPONENT by lazy {
        ReflectHelper.of("com.xiaomi.market.common.component.componentbeans.SearchHistoryComponent", classLoader)?.delegate
    }
    private val ENUM_PAGE_STATE_EXPAND by lazy {
        ReflectHelper.of("com.xiaomi.market.ui.UpdateListRvAdapter\$PageCollapseState", classLoader)?.operate {
            // name: Expand | type: com.xiaomi.market.ui.UpdateListRvAdapter$PageCollapseState
            field("Expand")?.get(null)
        }
    }
    private val ENUM_DETAIL_TYPE_UNKNOWN by lazy {
        ReflectHelper.of("com.xiaomi.market.business_ui.detail.DetailType", classLoader)?.operate {
            // name: UNKNOWN | type: com.xiaomi.market.business_ui.detail.DetailType
            field("UNKNOWN")?.get(null)
        }
    }

    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        this.classLoader = lpparam.classLoader
        ReflectHelper.of("com.xiaomi.market.common.network.retrofit.response.bean.AppDetailV3", classLoader)?.operate {
            setOf(
                // modifier: public,final | signature: isBrowserMarketAdOff()Z
                "isBrowserMarketAdOff",
                // modifier: public,final | signature: isBrowserSourceFileAdOff()Z
                "isBrowserSourceFileAdOff",
                // modifier: private,final | signature: supportShowCompat64bitAlert()Z
                "supportShowCompat64bitAlert"
            ).forEach {
                method(it)?.hook(XC_MethodReplacement.returnConstant(true))
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
                // modifier: public,final | signature: supportAutoLoadDeepLink()Z
                "supportAutoLoadDeepLink",
                // modifier: public,final | signature: supportShowCompatAlert()Z
                "supportShowCompatAlert",
                // modifier: public,final | signature: supportShowCompatChildForbidDownloadAlert()Z
                "supportShowCompatChildForbidDownloadAlert"
            ).forEach {
                method(it)?.hook(XC_MethodReplacement.returnConstant(false))
            }
        }

        ReflectHelper.of("com.xiaomi.market.ui.splash.DetailSplashAdManager", classLoader)?.operate {
            setOf(
                // modifier: public,static,final | signature: canRequestSplashAd(Lcom/xiaomi/market/ui/splash/DetailSplashAdManager$Env;)Z
                "canRequestSplashAd",
                // modifier: public,static,final | signature: isRequesting()Z
                "isRequesting",
                // modifier: private,final | signature: isOpenFromMsa(Lcom/xiaomi/market/ui/splash/DetailSplashAdManager$Env;)Z
                "isOpenFromMsa"
            ).forEach {
                method(it)?.hook(XC_MethodReplacement.returnConstant(false))
            }

            // modifier: public,static,final | signature: isOpenFromMsa(Lcom/xiaomi/market/ui/BaseActivity;Ljava/lang/Runnable;Ljava/lang/Runnable;)V
            method("tryToRequestSplashAd")?.hook(XC_MethodReplacement.returnConstant(null))
        }

        ReflectHelper.of("com.xiaomi.market.ui.splash.SplashManager", classLoader)?.operate {
            setOf(
                // modifier: public,static | signature: canShowSplash(Landroid/app/Activity;)Z
                "canShowSplash",
                // modifier: public | signature: canShowSplash(Landroid/app/Activity;)Z
                "needShowSplash",
                // modifier: public,static | signature: needRequestFocusVideo(Ljava/lang/String;Landroid/app/Activity;)Z
                "needRequestFocusVideo",
                // modifier: public,static | signature: isPassiveSplashAd(Landroid/app/Activity;)Z
                "isPassiveSplashAd"
            ).forEach {
                method(it)?.hook(XC_MethodReplacement.returnConstant(false))
            }

            setOf(
                // modifier: public | signature: tryAdSplash(Landroid/app/Activity;Ljava/lang/String;)V
                "tryAdSplash",
                // modifier: public | signature: trySplashWhenApplicationForeground(Landroid/app/Activity;)V
                "trySplashWhenApplicationForeground"
            ).forEach {
                method(it)?.hook(XC_MethodReplacement.returnConstant(null))
            }
        }

        ReflectHelper.of("com.xiaomi.market.business_ui.main.MarketTabActivity", classLoader)?.operate {
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
                method(it)?.hook(XC_MethodReplacement.returnConstant(null))
            }
        }

        // 搜索建议页面
        ReflectHelper.of("com.xiaomi.market.business_ui.search.NativeSearchSugFragment", classLoader)?.operate {
            // modifier: public | signature: getRequestParams()Ljava/util/Map<Ljava/lang/String;Ljava/lang/Object;>;
            method("getRequestParams")?.hook(object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    @Suppress("UNCHECKED_CAST")
                    val baseParametersForH5ToNative = (param.result as? Map<String, Any?>)?.toMutableMap() ?: return
                    baseParametersForH5ToNative["adFlag"] = 0
                    param.result = baseParametersForH5ToNative
                }
            })
        }

        // 搜索结果页面
        ReflectHelper.of("com.xiaomi.market.business_ui.search.NativeSearchResultFragment", classLoader)?.operate {
            // modifier: public | signature: parseResponseData(Lorg/json/JSONObject;Z)Ljava/util/List<Lcom/xiaomi/market/common/component/componentbeans/BaseNativeComponent;>;
            method("parseResponseData")?.hook(object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    @Suppress("UNCHECKED_CAST")
                    val parsedComponents = (param.result as? List<Any>)?.toMutableList() ?: return
                    parsedComponents.retainAll {
                        CLASS_LIST_APP_COMPONENT?.isInstance(it) ?: true
                    }
                    param.result = parsedComponents
                }
            })
        }

        // 搜索页面
        ReflectHelper.of("com.xiaomi.market.business_ui.search.NativeSearchGuideFragment", classLoader)?.operate {
            // modifier: public | signature: parseResponseData(Lorg/json/JSONObject;Z)Ljava/util/List<Lcom/xiaomi/market/common/component/componentbeans/BaseNativeComponent;>;
            method("parseResponseData")?.hook(object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    @Suppress("UNCHECKED_CAST")
                    val parsedComponents = (param.result as? List<Any>)?.toMutableList() ?: return
                    parsedComponents.retainAll {
                        CLASS_SEARCH_HISTORY_COMPONENT?.isInstance(it) ?: true
                    }
                    param.result = parsedComponents
                }
            })

            // modifier: public | signature: isLoadMoreEndGone()Z
            method("isLoadMoreEndGone")?.hook(XC_MethodReplacement.returnConstant(true))
        }

        // 更新页面
        ReflectHelper.of("com.xiaomi.market.ui.UpdateListRvAdapter", classLoader)?.operate {
            // modifier: public | signature: <init>(Lcom/xiaomi/market/common/component/base/INativeFragmentContext<Lcom/xiaomi/market/ui/BaseFragment;>;)V
            declaredConstructors().forEach {
                it.hook(object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val thisObj = param.thisObject
                        thisObj.javaClass.getDeclaredField("forceExpanded").apply {
                            isAccessible = true
                        }[thisObj] = true
                        thisObj.javaClass.getDeclaredField("foldButtonVisible").apply {
                            isAccessible = true
                        }[thisObj] = false
                        thisObj.javaClass.getDeclaredField("pageCollapseState").apply {
                            isAccessible = true
                        }[thisObj] = ENUM_PAGE_STATE_EXPAND
                    }
                })
            }

            // modifier: private,final | signature: generateRecommendGroupItems(Ljava/util/ArrayList;I)Z
            method("generateRecommendGroupItems")?.hook(XC_MethodReplacement.returnConstant(null))
        }

        // 下载页面
        ReflectHelper.of("com.xiaomi.market.ui.DownloadListFragment", classLoader)?.operate {
            // modifier: private,final | signature: parseRecommendGroupResult(Lorg/json/JSONObject;)Lcom/xiaomi/market/viewmodels/RecommendGroupResult;
            method("parseRecommendGroupResult")?.hook(XC_MethodReplacement.returnConstant(null))
        }

        // 应用详情页面
        ReflectHelper.of("com.xiaomi.market.ui.detail.BaseDetailActivity", classLoader)?.operate {
            // modifier: public | signature: initParams()Landroid/os/Bundle;
            method("initParams")?.hook(object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    field("detailType")?.set(param.thisObject, ENUM_DETAIL_TYPE_UNKNOWN)
                }
            })
        }
    }
}