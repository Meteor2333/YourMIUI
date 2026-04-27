package cc.meteormc.yourmiui.xposed.securitycenter.helper

import cc.meteormc.yourmiui.xposed.ReflectOperator
import cc.meteormc.yourmiui.xposed.invokeSuper
import cc.meteormc.yourmiui.xposed.operator
import de.robv.android.xposed.XC_MethodHook

object AlertActivityHelper {
    fun disableAlert(
        classLoader: ClassLoader,
        clazz: String,
        postHandler: ReflectOperator<Any>.(XC_MethodHook.MethodHookParam) -> Boolean
    ) {
        val parentClass = operator(classLoader, "com.miui.common.base.AlertActivity")?.delegate ?: return
        operator(classLoader, clazz) {
            if (!parentClass.isAssignableFrom(delegate)) return@operator

            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hookDoNothing {
                // 判断当前子类环境是否为所需的类
                if (it.thisObject.javaClass.name != clazz) return@hookDoNothing false
                // 调用super.onCreate以防止SuperNotCalledException报错
                it.invokeSuper()
                postHandler(it)
            }
        }
    }
}