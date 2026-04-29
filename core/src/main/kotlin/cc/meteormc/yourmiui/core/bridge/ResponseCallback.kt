package cc.meteormc.yourmiui.core.bridge

import java.io.Serializable

interface ResponseCallback<T: Serializable> {
    fun onSuccess(data: T)

    fun onFailure()
}