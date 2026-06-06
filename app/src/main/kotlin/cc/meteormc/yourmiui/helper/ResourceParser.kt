package cc.meteormc.yourmiui.helper

import android.content.Context
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.api.util.PrimitiveUtil

object ResourceParser {
    private var resourceMap = R::class.java
        .declaredClasses
        .asSequence()
        .flatMap { clazz ->
            val resType = clazz.simpleName
            clazz.declaredFields.mapNotNull { field ->
                if (PrimitiveUtil.toPrimitiveClass(field.type) != Int::class.javaPrimitiveType) {
                    return@mapNotNull null
                }

                val resName = field.name
                "@$resType/$resName" to field.getInt(null)
            }
        }
        .toMap(LinkedHashMap())

    fun parseResName(context: Context, resName: String): Int {
        if (!resName.startsWith("@")) return R.string.resource_unknown
        var id = resourceMap[resName]
        if (id == null) {
            val split = resName.removePrefix("@").split('/')
            val type = split[0]
            val name = split[1]

            id = context.resources.getIdentifier(name, type, context.packageName)
            if (id <= 0) id = R.string.resource_unknown
            resourceMap[resName] = id
        }

        return id
    }
}