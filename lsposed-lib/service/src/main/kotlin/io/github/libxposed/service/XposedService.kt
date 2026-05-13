package io.github.libxposed.service

import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.ParcelFileDescriptor
import io.github.libxposed.service.callback.ScopeEventCallback

@Suppress("unused")
object XposedService {
    const val SEND_BINDER_METHOD = "SendBinder"
    const val AIDL_SERVICE_DESCRIPTOR = "io.github.libxposed.service.IXposedService"
    const val AIDL_SCOPE_CALLBACK_DESCRIPTOR = "io.github.libxposed.service.IXposedScopeCallback"

    internal lateinit var binder: IBinder
    private val mRemotePrefs = mutableMapOf<String, RemotePreferences?>()

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

    val scopes
        get() = callService(10) { createStringArrayList() }

    fun requestScope(vararg packages: String, callback: ScopeEventCallback) {
        callService<Unit>(
            11,
            IBinder.FLAG_ONEWAY,
            writer = {
                writeStringList(packages.toList())
                writeStrongInterface(callback)
            }
        )
    }

    fun removeScope(vararg packages: String) {
        callService<Unit>(
            12,
            writer = { writeStringList(packages.toList()) }
        )
    }

    fun getRemotePreferences(group: String): SharedPreferences? {
        return mRemotePrefs.computeIfAbsent(group) {
            val bundle = callService(
                20,
                writer = { writeString(group) },
                reader = { readTypedObject(Bundle.CREATOR) }
            ) ?: return@computeIfAbsent null

            @Suppress("DEPRECATION", "UNCHECKED_CAST")
            val map = bundle.getSerializable("map") as Map<String, Any?>?
            RemotePreferences(group, map ?: emptyMap())
        }
    }

    fun updateRemotePreferences(group: String, editor: SharedPreferences.Editor) {
        if (editor !is RemotePreferences.Editor) return
        val bundle = Bundle()
        bundle.putSerializable("delete", editor.delete.toHashSet())
        bundle.putSerializable("put", editor.put.toMap(HashMap()))
        callService<Unit>(
            21,
            writer = {
                writeString(group)
                writeTypedObject(bundle, 0)
            }
        )
    }

    fun deleteRemotePreferences(group: String) {
        mRemotePrefs.remove(group)
        callService<Unit>(
            22,
            writer = { writeString(group) }
        )
    }

    fun getRemoteFiles(): Array<String> {
        return callService(30) { createStringArray() } ?: emptyArray()
    }

    fun getRemoteFileDescriptor(name: String): ParcelFileDescriptor? {
        return callService(
            31,
            writer = { writeString(name) },
            reader = { readTypedObject(ParcelFileDescriptor.CREATOR) }
        )
    }

    fun deleteRemoteFile(name: String): Boolean {
        return callService(
            32,
            writer = { writeString(name) },
            reader = { readInt() != 0 }
        ) ?: false
    }

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