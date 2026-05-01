package cc.meteormc.yourmiui.xposed.securitycenter.helper

import android.app.Activity
import cc.meteormc.yourmiui.xposed.ReflectOperator
import cc.meteormc.yourmiui.xposed.getThisObject
import cc.meteormc.yourmiui.xposed.invokeSuper
import cc.meteormc.yourmiui.xposed.operator

object AlertActivityHelper {
    fun disableAlert(
        classLoader: ClassLoader,
        clazz: String,
        postHandler: ReflectOperator<Any>.(Activity) -> Boolean
    ) {
        val parentClass = operator(classLoader, "com.miui.common.base.AlertActivity")?.delegate ?: return
        operator(classLoader, clazz) {
            if (!parentClass.isAssignableFrom(delegate)) return@operator

            // modifier: public | signature: onCreate(Landroid/os/Bundle;)V
            method("onCreate")?.hookDoNothing {
                val activity = it.getThisObject(Activity::class.java)
                // 判断当前子类环境是否为所需的类
                if (activity.javaClass.name != clazz) return@hookDoNothing false
                // 调用super.onCreate以防止SuperNotCalledException报错
                it.invokeSuper()
                postHandler(activity)
            }
        }
    }
}