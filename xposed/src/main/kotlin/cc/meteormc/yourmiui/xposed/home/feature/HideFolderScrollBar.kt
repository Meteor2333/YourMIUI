package cc.meteormc.yourmiui.xposed.home.feature

import android.widget.GridView
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.DESKTOP,
    "@string/feature_desktop_hide_folder_scroll_bar_name",
    "@string/feature_desktop_hide_folder_scroll_bar_description"
)
@RequiredScope("com.miui.home")
object HideFolderScrollBar : Feature(
    key = "hide_folder_scroll_bar",
    nameRes = R.string.feature_home_hide_folder_scroll_bar_name,
    descriptionRes = R.string.feature_home_hide_folder_scroll_bar_description,
    testEnvironmentRes = R.string.feature_home_hide_folder_scroll_bar_test_environment
), FeatureHooker {
    override fun hook(packageName: String) {
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