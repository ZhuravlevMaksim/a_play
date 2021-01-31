package com.muzic.aplay.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muzic.aplay.PlayDownloadManager
import com.muzic.aplay.db.YoutubeStream
import com.muzic.aplay.db.YoutubeStreamDatabase
import com.ystract.services.AudioStreamInfo
import com.ystract.services.YoutubeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class YoutubeViewModel(private val downloadManager: PlayDownloadManager, context: Context) : ViewModel() {

    private val dao = YoutubeStreamDatabase.get(context).dao()
    public val getAllData = dao.getAllData()

    fun getStreamFromUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val extract = extractUid(url)
            Timber.i("Uid ${extract.first.name} ${extract.second}")
            extract.second?.let { uid ->
                if (extract.first == TYPE.PLAYLIST) {
                    YoutubeService().fromPlaylist(uid) { insert(it) }
                } else {
                    YoutubeService().fromVideo(uid)?.let { insert(it) }
                }
            }
        }
    }

    private fun insert(stream: AudioStreamInfo) = dao.insert(
        YoutubeStream(
            uid = stream.uid,
            url = stream.audioStream.url,
            contentLength = stream.audioStream.contentLength,
            fileName = stream.file(),
            mimeType = stream.audioStream.mimeType,
            title = stream.title
        )
    )

    public fun download(stream: YoutubeStream) {
        downloadManager.downloadWithAndroidManager(stream)
    }

    private fun extractUid(url: String): Pair<TYPE, String?> {
        if (url.contains("list")) {
            return Pair(TYPE.PLAYLIST, Regex("list=(.+)\$").find(url)?.destructured?.component1())
        }
        if (url.contains("https://youtu.be/")) {
            return Pair(TYPE.VIDEO, url.replace("https://youtu.be/", ""))
        }
        return Pair(TYPE.VIDEO, Regex("watch\\?v=(.+)&|watch\\?v=(.+)\$").find(url)?.destructured?.component1())
    }

    fun remove(youtubeStream: YoutubeStream?) {
        youtubeStream?.let {
            viewModelScope.launch(Dispatchers.IO) {
                dao.delete(it)
            }
        }
    }

    enum class TYPE {
        PLAYLIST,
        VIDEO
    }

}