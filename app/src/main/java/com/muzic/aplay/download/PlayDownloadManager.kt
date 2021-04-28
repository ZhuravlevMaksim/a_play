package com.muzic.aplay.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.muzic.aplay.db.YoutubeStream
import com.ystract.YoutubeStreamExtractor
import okhttp3.OkHttpClient
import okhttp3.Request

class PlayDownloadManager(private val context: Context, private val client: OkHttpClient) {

    fun downloadWithAndroidManager(stream: YoutubeStream) {
        fun download() {
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val request = DownloadManager.Request(Uri.parse(stream.url))
                .setTitle(stream.title) // Title of the Download Notification
                .setDescription(stream.title) // Description of the Download Notification
                .setMimeType(stream.mimeType)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
                .setDestinationUri(Uri.fromFile(downloadsDir)) // Uri of the destination file
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, stream.fileName)
                .setAllowedOverMetered(false)
                .setAllowedOverRoaming(false)
            val manager = context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
        }

        //todo: fix rotten link or save stream as file or e.t.c
        if (checkStreamLinkNotExpired(stream)) {
            download()
        } else {
            val freshStream = YoutubeStreamExtractor.streamFromVideo(stream.uid, true)
            freshStream?.let {
                stream.url = it.audioStream.url
                download()
            }
        }
    }

    fun validateUrl(stream: YoutubeStream): YoutubeStream {
        if (!checkStreamLinkNotExpired(stream)) {
            stream.url = YoutubeStreamExtractor.streamFromVideo(stream.uid, true)!!.audioStream.url!!
        }
        return stream
    }

    private fun checkStreamLinkNotExpired(stream: YoutubeStream): Boolean {
        if (stream.url != null) {
            client.newCall(
                Request.Builder()
                    .url(stream.url!!)
                    .head()
                    .build()
            ).execute().use { response -> return response.isSuccessful }
        }
        return false
    }
}
