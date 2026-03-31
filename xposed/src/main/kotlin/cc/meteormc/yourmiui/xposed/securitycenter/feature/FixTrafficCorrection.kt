package cc.meteormc.yourmiui.xposed.securitycenter.feature

import android.os.Looper
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import de.robv.android.xposed.XC_MethodHook

object FixTrafficCorrection : XposedFeature(
    nameRes = R.string.feature_securitycenter_fix_traffic_correction_name,
    descriptionRes = R.string.feature_securitycenter_fix_traffic_correction_description,
    testEnvironmentRes = R.string.feature_securitycenter_fix_traffic_correction_test_environment
) {
    override fun init() {
        helper("com.miui.sdk.tc.TcManager")?.operate {
            // modifier: public | signature: getAllInstructions(I)Ljava/util/List<Lcom/miui/sdk/tc/TcDirection;>;
            val refreshMethod = method("getAllInstructions")
            refreshMethod?.let {
                // modifier: private synchronized | signature: isInBlockNumberList(Ljava/lang/String;I)Z
                method("isInBlockNumberList")?.hook(object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        // 由于一些神秘原因 网络助手的校正号码白名单总是不更新
                        // 导致运营商的响应短信被过滤掉 (打印日志为`onProcessSms 解析失败 need block sms`)
                        // 所以我们来帮他手动校正
                        @Suppress("unused")
                        for (i in 0 until 5) {
                            val result = refreshMethod.call(param.thisObject, param.args[1]) as List<*>
                            if (result.isNotEmpty()) break
                            if (Looper.myLooper() != Looper.getMainLooper()) {
                                // 刷新有时会失败 多尝试几次
                                Thread.sleep(3000)
                            }
                        }
                    }
                })
            }
        }

        // for debug
//        helper("com.miui.networkassistant.service.tm.TrafficSimManager")?.operate {
//            // modifier: public | signature: checkCorrectTime(IZZII)I
//            method("checkCorrectTime")?.hook(XC_MethodReplacement.returnConstant(-1))
//        }
    }
}