package cc.meteormc.yourmiui.store

class DataField<T>(private val defaultValue: T) {
    var value: T = defaultValue
        private set
    private var initialized = false

    private val listeners = mutableMapOf<ObserveType, MutableList<DataField<T>.() -> Unit>>()

    fun updateValue(newValue: T) {
        value = newValue
        if (!initialized) {
            this.notifyChange(ObserveType.INIT)
        }

        initialized = true
        this.notifyChange(ObserveType.UPDATE)
    }

    fun removeValue() {
        value = defaultValue
        initialized = false
        this.notifyChange(ObserveType.REMOVE)
    }

    fun observe(type: ObserveType, listener: DataField<T>.() -> Unit) {
        listeners.computeIfAbsent(type) { mutableListOf() }.add(listener)
        if (initialized) {
            this.notifyChange(ObserveType.INIT)
        }
    }

    private fun notifyChange(type: ObserveType) {
        listeners[type]?.forEach { it() }
    }

    override fun toString() = value.toString()

    enum class ObserveType {
        INIT, UPDATE, REMOVE
    }
}