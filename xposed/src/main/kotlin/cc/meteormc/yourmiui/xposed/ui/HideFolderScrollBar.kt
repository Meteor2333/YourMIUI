package cc.meteormc.yourmiui.xposed.ui

import android.widget.GridView
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.operator

object HideFolderScrollBar : Feature() {
    override fun onLoadPackage(packageName: String) {
        operator("com.miui.home.launcher.FolderGridView") {
            // modifier: public | signature: <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
            declaredConstructors().forEach {
                it.hookAfter { param ->
                    val view = param.instance<GridView>()
                    view.scrollBarSize = 0
                    view.isVerticalScrollBarEnabled = false
                    view.isHorizontalScrollBarEnabled = false
                }
            }
        }
    }
}