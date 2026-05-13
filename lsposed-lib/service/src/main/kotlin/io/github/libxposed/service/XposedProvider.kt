package io.github.libxposed.service

import android.content.ContentProvider
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle

class XposedProvider : ContentProvider() {
    override fun onCreate() = true

    override fun getType(uri: Uri) = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0

    override fun insert(uri: Uri, values: ContentValues?) = null

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ) = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ) = 0

    override fun call(
        method: String,
        arg: String?,
        extras: Bundle?
    ): Bundle? {
        if (method == XposedService.SEND_BINDER_METHOD && extras != null) {
            val binder = extras.getBinder("binder")
            if (binder != null) {
                XposedService.binder = binder
            }
            return Bundle()
        }

        return null
    }
}