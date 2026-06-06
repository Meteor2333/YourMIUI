package cc.meteormc.yourmiui.ui.data

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.data.FeatureInfo

data class SearchResultItem(
    val category: Category,
    val feature: FeatureInfo,
    val title: CharSequence,
    val path: String,
)