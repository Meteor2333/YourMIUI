package cc.meteormc.yourmiui.api.data

import cc.meteormc.yourmiui.api.OptionType
import cc.meteormc.yourmiui.api.annotation.AppOptionRegister
import cc.meteormc.yourmiui.api.annotation.ListOptionRegister
import cc.meteormc.yourmiui.api.annotation.SwitchOptionRegister
import cc.meteormc.yourmiui.api.annotation.TextOptionRegister
import cc.meteormc.yourmiui.api.util.NamingCaseUtil.toSnakeCase
import java.lang.reflect.Field

data class OptionInfo(
    val key: String,
    val name: Int,
    val description: Int,
    val type: OptionType<*>,
    val source: Field
) {
    companion object {
        fun fromSource(source: Field): OptionInfo? {
            if (source.isAnnotationPresent(AppOptionRegister::class.java)) {
                val annotation = source.getDeclaredAnnotation(AppOptionRegister::class.java)!!
                return OptionInfo(
                    source.name,
                    annotation.name.toInt(),
                    annotation.description.toInt(),
                    OptionType.App(
                        annotation.defaultPackages,
                        annotation.multiSelect
                    ),
                    source
                )
            }

            if (source.isAnnotationPresent(ListOptionRegister::class.java)) {
                val annotation = source.getDeclaredAnnotation(ListOptionRegister::class.java)!!
                val options = annotation.options
                val displayOptions = annotation.displayOptions.map {
                    // todo: toInt只是为了过编译，后续需要改成解析真正的字符串资源id
                    it.toInt()
                }
                if (options.size != displayOptions.size) {
                    throw IllegalArgumentException("Options and display options must have the same length")
                }

                return OptionInfo(
                    source.name.toSnakeCase(),
                    annotation.name.toInt(),
                    annotation.description.toInt(),
                    OptionType.List(
                        options.zip(displayOptions).toMap(),
                        annotation.defaultOptions,
                        annotation.multiSelect
                    ),
                    source
                )
            }

            if (source.isAnnotationPresent(SwitchOptionRegister::class.java)) {
                val annotation = source.getDeclaredAnnotation(SwitchOptionRegister::class.java)!!
                return OptionInfo(
                    source.name,
                    annotation.name.toInt(),
                    annotation.description.toInt(),
                    OptionType.Switch(annotation.defaultValue),
                    source
                )
            }

            if (source.isAnnotationPresent(TextOptionRegister::class.java)) {
                val annotation = source.getDeclaredAnnotation(TextOptionRegister::class.java)!!
                return OptionInfo(
                    source.name,
                    annotation.name.toInt(),
                    annotation.description.toInt(),
                    OptionType.Text(annotation.defaultText),
                    source
                )
            }

            return null
        }
    }
}