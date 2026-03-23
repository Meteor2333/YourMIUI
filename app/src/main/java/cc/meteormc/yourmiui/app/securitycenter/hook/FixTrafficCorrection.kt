package cc.meteormc.yourmiui.app.securitycenter.hook

import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

object FixTrafficCorrection: Hook(
    name = "修复流量校正",
    description = "修复网络助手的校正总是失败的问题",
    testEnvironment = "7.5.4-230317.0.2版本"
) {
    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        ReflectHelper.of("com.miui.sdk.tc.TcManager", lpparam.classLoader)?.operate {
            // modifier: public | signature: getAllInstructions(I)Ljava/util/List<Lcom/miui/sdk/tc/TcDirection;>;
            val refreshMethod = method("getAllInstructions")
            refreshMethod?.let {
                // modifier: private synchronized | signature: isInBlockNumberList(Ljava/lang/String;I)Z
                method("isInBlockNumberList")?.hook(object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        // 由于一些神秘原因 网络助手的校正号码白名单总是不更新
                        // 导致运营商的响应短信被过滤掉 (打印日志为`onProcessSms 解析失败 need block sms`)
                        // 所以我们来帮他手动校正
                        refreshMethod.call(param.thisObject, param.args[1])
                    }
                })
            }
        }

        // for debug
//        ReflectHelper.of("com.miui.networkassistant.service.tm.TrafficSimManager", lpparam.classLoader)?.operate {
//            // modifier: public | signature: checkCorrectTime(IZZII)I
//            method("checkCorrectTime")?.hook(XC_MethodReplacement.returnConstant(-1))
//        }
    }
}