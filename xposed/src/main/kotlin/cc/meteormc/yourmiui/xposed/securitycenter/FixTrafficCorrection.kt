package cc.meteormc.yourmiui.xposed.securitycenter

import android.os.Looper
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

object FixTrafficCorrection : Feature(
    key = "fix_traffic_correction",
    nameRes = R.string.feature_securitycenter_fix_traffic_correction_name,
    descriptionRes = R.string.feature_securitycenter_fix_traffic_correction_description,
    testEnvironmentRes = R.string.feature_securitycenter_fix_traffic_correction_test_environment
) {
    override fun onLoadPackage() {
        operator("com.miui.sdk.tc.TcManager") {
            // modifier: public | signature: getAllInstructions(I)Ljava/util/List<Lcom/miui/sdk/tc/TcDirection;>;
            val refreshMethod = method("getAllInstructions") ?: return@operator

            // modifier: private synchronized | signature: isInBlockNumberList(Ljava/lang/String;I)Z
            method("isInBlockNumberList")?.hookBefore {
                // 由于一些神秘原因 网络助手的校正号码白名单总是不更新
                // 导致运营商的响应短信被过滤掉 (打印日志为`onProcessSms 解析失败 need block sms`)
                // 所以我们来帮他手动校正
                repeat(3) { _ ->
                    val result = refreshMethod.call(it.instance, it.intArg()) as? List<*>
                    if (!result.isNullOrEmpty()) return@hookBefore
                    if (!Looper.getMainLooper().isCurrentThread) {
                        // 刷新有时会失败 多尝试几次
                        Thread.sleep(3000)
                    }
                }
            }
        }

        // for debug
//        operator("com.miui.networkassistant.service.tm.TrafficSimManager") {
//            // modifier: public | signature: checkCorrectTime(IZZII)I
//            method("checkCorrectTime")?.hookResult(-1)
//        }
    }
}