package com.muzic.aplay

import android.Manifest
import android.content.ContentUris
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.muzic.aplay.ui.MainActivity


const val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 6873

fun MainActivity.permissions(): Boolean {
    if (PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
        return true
    } else {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_READ_EXTERNAL_STORAGE)
    }
    return false
}

fun contentPathFromId(id: Long): String {
    return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()
}