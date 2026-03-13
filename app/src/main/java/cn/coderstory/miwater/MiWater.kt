package cn.coderstory.miwater

import android.content.pm.ApplicationInfo
import androidx.annotation.Keep
import cn.coderstory.miwater.helper.ReflectHelper
import cn.coderstory.miwater.helper.XposedHelper
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.net.URL

@Keep
class MiWater: IXposedHookLoadPackage {
    private val apps = listOf<App>(
        // ...
    ).associateBy { it.getPackageName() }
    private val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID, "conf")

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        if (packageName == BuildConfig.APPLICATION_ID) {
            ReflectHelper.of(XposedHelper.Companion::class.java.name, lpparam.classLoader)?.operate {
                method("isXposedActive")?.hook(XC_MethodReplacement.returnConstant(true))
            }
        } else {
            apps[packageName]?.init(lpparam)
        }

        when (packageName) {
            "com.android.thememanager" -> {
                if (prefs.getBoolean("removeThemeAd", true)) {
                    ReflectHelper.of("com.android.thememanager.basemodule.ad.model.AdInfoResponse", lpparam.classLoader)?.operate {
                        method("isAdValid")?.hook(XC_MethodReplacement.returnConstant(false))
                        method("checkAndGetAdInfo")?.hook(XC_MethodReplacement.returnConstant(null))
                    }
                }
            }
            "com.miui.packageinstaller" -> {
                if (prefs.getBoolean("removeInstallerAd", true)) {
                    ReflectHelper.of("com.miui.packageInstaller.model.CloudParams", lpparam.classLoader)?.operate {
                        declaredConstructors().forEach { it.hook(object : XC_MethodHook() {
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
                        }) }
                    }
                }

                if (prefs.getBoolean("removeInstallerAuth", true)) {
                    ReflectHelper.of("java.net.URL", lpparam.classLoader)?.operate {
                        method("openConnection")?.hook(object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                val obj: URL = param.thisObject as URL
                                XposedBridge.log("current host is ${obj.host}")
                                if (obj.host == "api-installer.pt.xiaomi.com" || obj.host == "preview-api.installer.xiaomi.com") {
                                    XposedHelpers.setObjectField(obj, "host", "www.baidu.com")
                                }
                            }
                        })
                    }
                }

                if (prefs.getBoolean("removeInstallerLimit", true)) {
                    ReflectHelper.of("android.net.Uri", lpparam.classLoader)?.operate {
                        method("parse", String::class.java)?.hook(object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                if (param.args[0].toString().contains("com.miui.securitycenter")) {
                                    param.args[0] = "nothing"
                                }
                            }
                        })
                    }
                    // return (arg2.flags & 1) > 0 || arg2.uid < 10000;
                    ReflectHelper.of("com.android.packageinstaller.e", lpparam.classLoader)?.operate {
                        method("a", ApplicationInfo::class.java)?.hook(XC_MethodReplacement.returnConstant(true))
                    }
                    // 隐藏开启纯净模式提示
                    //  SafeModeTipViewObject safeModeTipViewObject = new SafeModeTipViewObject(h10, pureModeElderTipViewObject.f5884m, null, null, 12, null);
                    //  safeModeTipViewObject.a();  a方法里的调用
                    ReflectHelper.of("com.miui.packageInstaller.ui.listcomponets.f0", lpparam.classLoader)?.operate {
                        method("a")?.hook(object : XC_MethodHook() {
                            @Throws(Throwable::class)
                            override fun afterHookedMethod(param: MethodHookParam) {
                                super.afterHookedMethod(param)
                                XposedHelpers.setBooleanField(param.thisObject, "l", false)
                            }
                        })
                    }
                }
            }
            "com.android.mms" -> {
                if (prefs.getBoolean("removeMmsAd", true)) {
                    ReflectHelper.of("com.miui.smsextra.http.RequestResult", lpparam.classLoader)?.operate {
                        method("data")?.hook(object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                if (param.result.toString().contains("modules")) {
                                    param.result = "{}"
                                }
                            }
                        })
                    }
                    ReflectHelper.of("com.miui.smsextra.ui.UnderstandButton", lpparam.classLoader)?.operate {
                        method("requestAD")?.hook(XC_MethodReplacement.returnConstant(false))
                    }
                }
            }
            "com.miui.systemAdSolution" -> {
                if (prefs.getBoolean("removeSplashAd2", false)) {
                    ReflectHelper.of("com.xiaomi.ad.entity.cloudControl.cn.CNDeskFolderControlInfo", lpparam.classLoader)?.operate {
                        method("isCloseAd")?.hook(XC_MethodReplacement.returnConstant(true))
                    }
                    ReflectHelper.of("com.xiaomi.ad.common.pojo.AdType", lpparam.classLoader)?.operate {
                        method("valueOf", Int::class.java)?.hook(XC_MethodReplacement.returnConstant(0))
                    }
                }
            }
        }

        // Global
        ReflectHelper.of("com.xiaomi.ad.server.AdLauncher", lpparam.classLoader)?.operate {
            method("initCrashMonitor")?.hook(XC_MethodReplacement.returnConstant(false))
        }
    }
}
