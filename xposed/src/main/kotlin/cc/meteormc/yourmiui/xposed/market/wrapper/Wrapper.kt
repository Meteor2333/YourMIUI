package cc.meteormc.yourmiui.xposed.market.wrapper

import cc.meteormc.yourmiui.xposed.operator

abstract class Wrapper(val className: String) {
    lateinit var wrapped: Any

    fun from(instance: Any) {
        wrapped = instance
    }

    fun new(classLoader: ClassLoader) = operator(classLoader, className) outer@{
        val newInstance = constructor()!!.new()
        operator(this@Wrapper.javaClass) {
            declaredFields().map {
                this@outer.field(it.name()) to it.get<Any>(this@Wrapper)
            }.forEach { (field, value) ->
                field?.set(newInstance, value)
            }

            newInstance
        }
    }

    protected fun <T : Any> getField(name: String) = operator(wrapped.javaClass) {
        field(name)?.get<T>(wrapped)
    }

    protected fun <T : Any> updateField(name: String, value: T?) = operator(wrapped.javaClass) {
        field(name)?.set(wrapped, value)
        Unit
    }
}