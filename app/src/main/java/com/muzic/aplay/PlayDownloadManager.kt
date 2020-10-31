package com.muzic.aplay

import android.annotation.TargetApi
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.chopper.services.AudioStreamInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink

class PlayDownloadManager(private val context: Context) {

    private val client = OkHttpClient()

    suspend fun download(url: String, filename: String, mimeType: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadQ(url, filename, mimeType)
        }
    }

    @TargetApi(29)
    private suspend fun downloadQ(url: String, filename: String, mimeType: String) {
        withContext(Dispatchers.IO) {
            val response = client.newCall(Request.Builder().url(url).build()).execute()

            if (response.isSuccessful) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

                uri?.let {

                    resolver.openOutputStream(uri)?.use { outputStream ->
                        val sink = outputStream.sink().buffer()

                        response.body?.source()?.let { sink.writeAll(it) }
                        sink.close()
                    }

                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)

                } ?: throw RuntimeException("MediaStore failed for some reason")
            } else {
                throw RuntimeException("OkHttp failed for some reason")
            }
        }
    }

    fun downloadWithAndroidManager(stream: AudioStreamInfo) {
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val request = DownloadManager.Request(Uri.parse(stream.audioStream.url))
            .setTitle(stream.title) // Title of the Download Notification
            .setDescription(stream.title) // Description of the Download Notification
            .setMimeType(stream.audioStream.mimeType)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // Visibility of the download Notification
            .setDestinationUri(Uri.fromFile(downloadsDir)) // Uri of the destination file
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, stream.file())
            .setAllowedOverMetered(false)
            .setAllowedOverRoaming(false)
        val manager = context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }
}
