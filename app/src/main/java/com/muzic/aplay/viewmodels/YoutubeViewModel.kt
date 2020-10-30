package com.muzic.aplay.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chopper.services.AudioStreamInfo
import com.chopper.services.YoutubeService
import com.muzic.aplay.PlayDownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class YoutubeViewModel(private val downloadManager: PlayDownloadManager) : ViewModel() {

    fun download(url: String, activity: Activity? = null) {
        viewModelScope.launch(Dispatchers.IO) {

            if (url.contains("playlist")) {
                val streams = YoutubeService().loadAudioStreamsFromPlaylist(url)
                Timber.d("start downloading ${streams.size}")
                streams.forEach {
                    downloadManager(it)
                }
            } else {
                downloadManager(YoutubeService().getAudioStream(fixUrl(url)))
            }

            activity?.finish()
        }
    }

    private fun fixUrl(url: String): String {
        return if (url.contains("youtu.be")) "https://www.youtube.com/watch?v=" + url.replace("https://youtu.be/", "") else url
    }

    private fun downloadManager(stream: AudioStreamInfo) {
        Timber.d("start downloading ${stream.title}")
        downloadManager.downloadWithAndroidManager(stream)

    }

}