package cc.meteormc.yourmiui.xposed.home.feature

import android.widget.GridView
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

object HideFolderScrollBar : Feature(
    key = "hide_folder_scroll_bar",
    nameRes = R.string.feature_home_hide_folder_scroll_bar_name,
    descriptionRes = R.string.feature_home_hide_folder_scroll_bar_description,
    testEnvironmentRes = R.string.feature_home_hide_folder_scroll_bar_test_environment
) {
    override fun onLoadPackage() {
        operator("com.miui.home.launcher.FolderGridView") {
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