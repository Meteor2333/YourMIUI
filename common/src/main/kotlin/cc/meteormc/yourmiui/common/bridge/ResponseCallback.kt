package cc.meteormc.yourmiui.common.bridge

interface ResponseCallback<T : Any> {
    fun onSuccess(data: T)

    fun onFailure()
}