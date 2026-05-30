package cc.meteormc.yourmiui.api

sealed class OptionType<T> {
    class App(
        val defaultPackages: Array<String>,
        val multiSelect: Boolean
    ) : OptionType<Set<String>>()

    class List(
        val options: Map<String, Int>,
        val defaultOptions: Array<String>,
        val multiSelect: Boolean
    ) : OptionType<Set<String>>()

    class Switch(
        val defaultValue: Boolean
    ) : OptionType<Boolean>()

    class Text(
        val defaultText: String
    ) : OptionType<String>()
}