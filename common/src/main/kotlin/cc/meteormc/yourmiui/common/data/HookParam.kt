package cc.meteormc.yourmiui.common.data

import java.lang.reflect.Member

@Suppress("unused", "UNCHECKED_CAST")
data class HookParam(
    val member: Member,
    val instance: Any?,
    val args: Array<Any?>,
    private val initialResult: Any?,
    private val initialThrowable: Throwable?,
    private var onCallSuper: (param: HookParam) -> Any?
) {
    var resultChanged = false
    var throwableChanged = false
    var result = initialResult
        set(value) {
            field = value
            resultChanged = true
        }
    var throwable = initialThrowable
        set(value) {
            field = value
            throwableChanged = true
        }

    fun <T> instance() = this.instance as T

    fun booleanArg(index: Int = 0) = argByGenerics<Boolean>(index)
    fun byteArg(index: Int = 0) = argByGenerics<Byte>(index)
    fun charArg(index: Int = 0) = argByGenerics<Char>(index)
    fun doubleArg(index: Int = 0) = argByGenerics<Double>(index)
    fun floatArg(index: Int = 0) = argByGenerics<Float>(index)
    fun intArg(index: Int = 0) = argByGenerics<Int>(index)
    fun longArg(index: Int = 0) = argByGenerics<Long>(index)
    fun shortArg(index: Int = 0) = argByGenerics<Short>(index)
    fun stringArg(index: Int = 0) = argByGenerics<String>(index)
    fun <T> argByClass(type: Class<T>, index: Int = 0) = this.args.filterIsInstance(type).getOrNull(index)
    inline fun <reified T> argByGenerics(index: Int = 0) = this.args.filterIsInstance<T>().getOrNull(index)

    val booleanResult
        get() = this.result as Boolean
    val byteResult
        get() = this.result as Byte
    val charResult
        get() = this.result as Char
    val doubleResult
        get() = this.result as Double
    val floatResult
        get() = this.result as Float
    val intResult
        get() = this.result as Int
    val longResult
        get() = this.result as Long
    val shortResult
        get() = this.result as Short
    val stringResult
        get() = this.result as String
    fun <T> result() = this.result as T?

    fun <T : Throwable> throwable() = this.throwable as T?

    fun callSuper() = this.onCallSuper(this)
}