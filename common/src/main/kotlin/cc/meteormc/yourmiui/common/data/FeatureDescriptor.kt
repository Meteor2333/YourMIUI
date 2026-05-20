package cc.meteormc.yourmiui.common.data

import cc.meteormc.yourmiui.common.Option
import org.json.JSONObject

data class FeatureDescriptor(
    val id: String,
    val name: String,
    val description: String,
    val warning: String,
    val author: String,
    val scopes: List<String>,
    val options: List<Option<*>>,
    val source: String
) {
    companion object {
        fun fromJson(json: JSONObject): FeatureDescriptor {
            val id = json.getString("id")
            val name = json.getString("name")
            val description = json.getString("description")
            val warning = json.optString("warning")
            val author = json.optString("author")
            val scopes = json.optJSONArray("scopes")?.let {
                buildList {
                    for (i in 0 until it.length()) {
                        add(it.getString(i))
                    }
                }
            } ?: emptyList()
            val options = json.optJSONArray("options")?.let {
                buildList {
                    for (i in 0 until it.length()) {
                        add(it.getString(i))
                    }
                }
            } ?: emptyList()
            val source = json.getString("source")
            return FeatureDescriptor(
                id,
                name,
                description,
                warning,
                author,
                scopes,
                options,
                source
            )
        }
    }
}