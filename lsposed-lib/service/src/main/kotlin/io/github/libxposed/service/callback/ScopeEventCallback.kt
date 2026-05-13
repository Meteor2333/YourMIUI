package io.github.libxposed.service.callback

import android.os.Binder
import android.os.IInterface
import android.os.Parcel
import io.github.libxposed.service.XposedService

private const val TRANSACTION_PROMPTED100_OR_APPROVED101 = 1 + 1
private const val TRANSACTION_APPROVED100_OR_FAILED101 = 2 + 1
private const val TRANSACTION_DENIED100 = 3 + 1
private const val TRANSACTION_TIMEOUT100 = 4 + 1
private const val TRANSACTION_FAILED100 = 5 + 1

class ScopeEventCallback : IInterface {
    private val binder = object : Binder() {
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code in FIRST_CALL_TRANSACTION..LAST_CALL_TRANSACTION) {
                data.enforceInterface(XposedService.AIDL_SCOPE_CALLBACK_DESCRIPTOR)
            }

            when (code) {
                INTERFACE_TRANSACTION -> {
                    reply?.writeString(XposedService.AIDL_SCOPE_CALLBACK_DESCRIPTOR)
                }
                TRANSACTION_PROMPTED100_OR_APPROVED101 -> {
                    val start = data.dataPosition()
                    val end = data.dataSize()
                    runCatching {
                        // try list
                        val list = data.createStringArrayList()
                        if (data.dataPosition() != end) {
                            throw IllegalStateException("not is list")
                        }
                        list
                    }.onFailure {
                        // rollback
                        data.setDataPosition(start)
                    }.recoverCatching {
                        // try string
                        val str = data.readString()
                        if (data.dataPosition() != end) {
                            throw IllegalStateException("not is string")
                        }
                        listOf(str)
                    }.getOrNull()?.forEach {
                        onApproved(it)
                    }
                }
                TRANSACTION_APPROVED100_OR_FAILED101 -> {
                    val pkgOrMsg = data.readString()
                    if (pkgOrMsg != null) {
                        val packageRegex = Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+$")
                        if (pkgOrMsg.matches(packageRegex)) onApproved(pkgOrMsg)
                        else onFailed(pkgOrMsg)
                    }
                }
                TRANSACTION_DENIED100 -> {
                    onFailed(data.readString())
                }
                TRANSACTION_TIMEOUT100 -> {
                    onFailed(data.readString())
                }
                TRANSACTION_FAILED100 -> {
                    data.readString()
                    onFailed(data.readString())
                }
                else -> {
                    return super.onTransact(code, data, reply, flags)
                }
            }

            return true
        }
    }

    override fun asBinder() = binder

    fun onApproved(packageName: String) {

    }

    fun onFailed(message: String?) {

    }
}