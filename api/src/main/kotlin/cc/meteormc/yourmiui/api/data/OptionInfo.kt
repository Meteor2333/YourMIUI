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
    val name: String,
    val description: String,
    val type: OptionType<*>,
    val source: Field
) {
    companion object {
        fun fromSource(source: Field): OptionInfo? {
            if (source.isAnnotationPresent(AppOptionRegister::class.java)) {
                val annotation = source.getDeclaredAnnotation(AppOptionRegister::class.java)!!
                return OptionInfo(
                    source.name.toSnakeCase(),
                    annotation.name,
                    annotation.description,
                    OptionType.App(
                        annotation.defaultPackages.toSet(),
                        annotation.multiSelect
                    ),
                    source
                )
            }

            if (source.isAnnotationPresent(ListOptionRegister::class.java)) {
                val annotation = source.getDeclaredAnnotation(ListOptionRegister::class.java)!!
                val options = annotation.options
                val displayOptions = annotation.displayOptions
                if (options.size != displayOptions.size) {
                    throw IllegalArgumentException("Options and display options must have the same length")
                }

                return OptionInfo(
                    source.name.toSnakeCase(),
                    annotation.name,
                    annotation.description,
                    OptionType.List(
                        options.zip(displayOptions).toMap(),
                        annotation.defaultOptions.toSet(),
                        annotation.multiSelect
                    ),
                    source
                )
            }

            if (source.isAnnotationPresent(SwitchOptionRegister::class.java)) {
                val annotation = source.getDeclaredAnnotation(SwitchOptionRegister::class.java)!!
                return OptionInfo(
                    source.name.toSnakeCase(),
                    annotation.name,
                    annotation.description,
                    OptionType.Switch(annotation.defaultValue),
                    source
                )
            }

            if (source.isAnnotationPresent(TextOptionRegister::class.java)) {
                val annotation = source.getDeclaredAnnotation(TextOptionRegister::class.java)!!
                return OptionInfo(
                    source.name.toSnakeCase(),
                    annotation.name,
                    annotation.description,
                    OptionType.Text(annotation.defaultText),
                    source
                )
            }

            return null
        }
    }
}