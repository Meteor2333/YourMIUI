package cc.meteormc.yourmiui.common.data

import org.json.JSONObject

data class CategoryDescriptor(
    val id: String,
    val name: String,
    val icon: String
) {
    companion object {
        fun fromJson(json: JSONObject): CategoryDescriptor {
            val id = json.getString("id")
            val name = json.getString("name")
            val icon = json.getString("icon")
            return CategoryDescriptor(
                id,
                name,
                icon
            )
        }
    }
}