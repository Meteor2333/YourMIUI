package cc.meteormc.yourmiui.common.data

import cc.meteormc.yourmiui.common.util.Unsafe.cast
import java.lang.reflect.Member

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

    fun <T> instance() = this.instance.cast<T>()

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

    fun booleanArg(value: Boolean, index: Int = 0) = argByGenerics<Boolean>(value, index)
    fun byteArg(value: Byte, index: Int = 0) = argByGenerics<Byte>(value, index)
    fun charArg(value: Char, index: Int = 0) = argByGenerics<Char>(value, index)
    fun doubleArg(value: Double, index: Int = 0) = argByGenerics<Double>(value, index)
    fun floatArg(value: Float, index: Int = 0) = argByGenerics<Float>(value, index)
    fun intArg(value: Int, index: Int = 0) = argByGenerics<Int>(value, index)
    fun longArg(value: Long, index: Int = 0) = argByGenerics<Long>(value, index)
    fun shortArg(value: Short, index: Int = 0) = argByGenerics<Short>(value, index)
    fun stringArg(value: String, index: Int = 0) = argByGenerics<String>(value, index)
    fun <T> argByClass(value: Any, type: Class<T>, index: Int = 0) = this.args.withIndex()
        .filter { type.isInstance(it.value) }
        .getOrNull(index)
        ?.index
        ?.let { args[it] = value }
    inline fun <reified T> argByGenerics(value: Any, index: Int = 0) = this.args.withIndex()
        .filter { it.value is T }
        .getOrNull(index)
        ?.index
        ?.let { args[it] = value }

    val booleanResult
        get() = this.result.cast<Boolean>()
    val byteResult
        get() = this.result.cast<Byte>()
    val charResult
        get() = this.result.cast<Char>()
    val doubleResult
        get() = this.result.cast<Double>()
    val floatResult
        get() = this.result.cast<Float>()
    val intResult
        get() = this.result.cast<Int>()
    val longResult
        get() = this.result.cast<Long>()
    val shortResult
        get() = this.result.cast<Short>()
    val stringResult
        get() = this.result.cast<String>()
    fun <T> result() = this.result.cast<T>()

    fun <T : Throwable> throwable() = this.throwable.cast<T>()

    fun callSuper() = this.onCallSuper(this)

    override fun equals(other: Any?) = other is HookParam && this.member == other.member

    override fun hashCode() = this.member.hashCode()
}