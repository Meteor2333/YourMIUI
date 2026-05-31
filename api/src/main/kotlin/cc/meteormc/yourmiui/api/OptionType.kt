package cc.meteormc.yourmiui.api

sealed class OptionType<T> {
    class App(
        val defaultPackages: Set<String>,
        val multiSelect: Boolean
    ) : OptionType<Set<String>>()

    class List(
        val options: Map<String, String>,
        val defaultOptions: Set<String>,
        val multiSelect: Boolean
    ) : OptionType<Set<String>>()

    class Switch(
        val defaultValue: Boolean
    ) : OptionType<Boolean>()

    class Text(
        val defaultText: String
    ) : OptionType<String>()
}