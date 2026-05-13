package io.github.libxposed.service

import android.os.IBinder
import android.os.Parcel

object XposedService {
    const val SEND_BINDER_METHOD = "SendBinder"
    const val AIDL_SERVICE_DESCRIPTOR = "io.github.libxposed.service.IXposedService"

    internal lateinit var binder: IBinder

    val activated
        get() = ::binder.isInitialized

    val apiVersion
        get() = callService(1) { readInt() }

    val frameworkName
        get() = callService(2) { readString() }

    val frameworkVersion
        get() = callService(3) { readString() }

    val frameworkVersionCode
        get() = callService(4) { readLong() }

    val frameworkProperties
        get() = callService(5) {
            // API100及以下为Int类型
            // API101及以上为Long类型
            // 所以先试读Int，如果还有数据再读Long
            val low = readInt()
            if (dataAvail() <= 0) {
                low.toLong()
            } else {
                val high = readInt()
                (high.toLong() shl 32) or (low.toLong() and 0xFFFFFFFFL)
            }
        }

    val scopes
        get() = callService(10) { createStringArrayList() }

    private fun <T : Any> callService(
        vararg transactionCodes: Int,
        flags: Int = 0x00000000,
        writer: Parcel.() -> Unit = { },
        reader: Parcel.() -> T? = { null }
    ): T? {
        if (!activated) return null

        fun tryTransact(code: Int, data: Parcel): Result<T?> {
            return Parcel.obtain().use { reply ->
                runCatching {
                    binder.transact(code + 1, data, reply, flags)
                    reply.readException()
                    reader(reply)
                }
            }
        }

        return Parcel.obtain().use { data ->
            data.writeInterfaceToken(AIDL_SERVICE_DESCRIPTOR)
            writer(data)
            transactionCodes.map { tryTransact(it, data) }
                .firstOrNull { it.isSuccess }
                ?.getOrThrow()
        }
    }

    private inline fun <T> Parcel.use(block: (Parcel) -> T): T {
        return block(this).also { recycle() }
    }
}