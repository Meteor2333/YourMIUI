package cn.coderstory.miwater

import android.content.pm.ApplicationInfo
import android.widget.TextView
import androidx.annotation.Keep
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.net.URL

@Keep
class MainHook : ReflectionHelper(), IXposedHookLoadPackage {
    var prefs = XSharedPreferences(BuildConfig.APPLICATION_ID, "conf")

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.android.thememanager" -> {
                if (prefs.getBoolean("removeThemeAd", true)) {
                    hookMethod(
                        "com.android.thememanager.basemodule.ad.model.AdInfoResponse",
                        lpparam.classLoader,
                        XC_MethodReplacement.returnConstant(false),
                        "isAdValid",
                    )
                    hookMethod(
                        "com.android.thememanager.basemodule.ad.model.AdInfoResponse",
                        lpparam.classLoader,
                        XC_MethodReplacement.returnConstant(null),
                        "checkAndGetAdInfo",
                    )
                }
            }
            "com.miui.packageinstaller" -> {
                if (prefs.getBoolean("removeInstallerAd", true)) {
                    hookConstructor(
                        "com.miui.packageInstaller.model.CloudParams",
                        lpparam.classLoader,
                        object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                val obj = param.thisObject
                                XposedHelpers.setBooleanField(obj, "showAdsBefore", false)
                                XposedHelpers.setBooleanField(obj, "showAdsAfter", false)
                                XposedHelpers.setBooleanField(obj, "singletonAuthShowAdsBefore", false)
                                XposedHelpers.setBooleanField(obj, "singletonAuthShowAdsAfter", true)
                                XposedHelpers.setBooleanField(obj, "useSystemAppRules", true)
                                XposedHelpers.setBooleanField(obj, "showSafeModeTip", false)
                                XposedHelpers.setBooleanField(obj, "openButton", true)
                                XposedHelpers.setObjectField(obj, "safeType", "1")
                            }
                        })
                }

                if (prefs.getBoolean("removeInstallerAuth", true)) {
                    hookMethod(
                        "java.net.URL",
                        lpparam.classLoader,
                        object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                val obj: URL = param.thisObject as URL
                                XposedBridge.log("current host is ${obj.host}")
                                if (obj.host == "api-installer.pt.xiaomi.com" || obj.host == "preview-api.installer.xiaomi.com") {
                                    XposedHelpers.setObjectField(obj, "host", "www.baidu.com")
                                }
                            }
                        },
                        "openConnection"
                    )
                }

                if (prefs.getBoolean("removeInstallerLimit", true)) {
                    hookMethod(
                        "android.net.Uri",
                        lpparam.classLoader,
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                if (param.args[0].toString().contains("com.miui.securitycenter")) {
                                    param.args[0] = "ddddd"
                                }
                            }
                        },
                        "parse",
                        String::class.java
                    )

                    // return (arg2.flags & 1) > 0 || arg2.uid < 10000;
                    hookMethod(
                        "com.android.packageinstaller.e",
                        lpparam.classLoader,
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                param.result = true
                            }
                        },
                        "a",
                        ApplicationInfo::class.java
                    )

                    // 隐藏开启纯净模式提示
                    //  SafeModeTipViewObject safeModeTipViewObject = new SafeModeTipViewObject(h10, pureModeElderTipViewObject.f5884m, null, null, 12, null);
                    //  safeModeTipViewObject.a();  a方法里的调用
                    hookMethod(
                        "com.miui.packageInstaller.ui.listcomponets.f0",
                        lpparam.classLoader,
                        object : XC_MethodHook() {
                            @Throws(Throwable::class)
                            override fun afterHookedMethod(param: MethodHookParam) {
                                super.afterHookedMethod(param)
                                XposedHelpers.setBooleanField(param.thisObject, "l", false)
                            }
                        },
                        "a"
                    )
                }
            }
            "com.android.mms" -> {
                if (prefs.getBoolean("removeMmsAd", true)) {
                    hookMethod(
                        "com.miui.smsextra.http.RequestResult",
                        lpparam.classLoader,
                        object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                if (param.result.toString().contains("modules")) {
                                    param.result = "{}"
                                }
                            }
                        },
                        "data",
                    )
                    hookMethod(
                        "com.miui.smsextra.ui.UnderstandButton",
                        lpparam.classLoader,
                        XC_MethodReplacement.returnConstant(false),
                        "requestAD",
                    )
                }
            }
            "com.miui.systemAdSolution" -> {
                if (prefs.getBoolean("removeSplashAd2", false)) {
                    hookMethod(
                        "com.xiaomi.ad.entity.cloudControl.cn.CNDeskFolderControlInfo",
                        lpparam.classLoader,
                        XC_MethodReplacement.returnConstant(true),
                        "isCloseAd"
                    )

                    hookMethod(
                        "com.xiaomi.ad.common.pojo.AdType",
                        lpparam.classLoader,
                        XC_MethodReplacement.returnConstant(0),
                        "valueOf",
                        Int::class.java
                    )
                }
            }
            "com.miui.securitycenter" -> {
                if (prefs.getBoolean("disableWaiting", true)) {
                    hookMethod(
                        "android.widget.TextView",
                        lpparam.classLoader,
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                param.args[0] = true
                            }
                        },
                        "setEnabled",
                        Boolean::class.java
                    )
                    hookMethod(
                        "android.widget.TextView",
                        lpparam.classLoader,
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                if (param.args.isNotEmpty() && param.args[0]?.toString()
                                        ?.startsWith("确定(") == true
                                ) {
                                    param.args[0] = "确定"
                                }
                            }
                        },
                        "setText",
                        CharSequence::class.java,
                        TextView.BufferType::class.java,
                        Boolean::class.java,
                        Int::class.java
                    )
                }
            }
        }

        // Global
        if (findClassWithoutLog("com.xiaomi.ad.server.AdLauncher", lpparam.classLoader) != null) {
            hookMethod(
                "com.xiaomi.ad.server.AdLauncher",
                lpparam.classLoader,
                XC_MethodReplacement.returnConstant(false),
                "initCrashMonitor"
            )
        }
    }
}
