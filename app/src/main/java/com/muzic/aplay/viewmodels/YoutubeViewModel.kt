package com.muzic.aplay.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chopper.services.YoutubeService
import com.muzic.aplay.PlayDownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class YoutubeViewModel(private val downloadManager: PlayDownloadManager) : ViewModel() {

    fun download(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val playerInfo = YoutubeService().getPlayerInfo(
                if (url.contains("youtu.be")) "https://www.youtube.com/watch?v=" + url.replace("https://youtu.be/", "") else url
            )
            val audioStream = playerInfo!!.audioStreams()[0]
            Timber.d("start downloading $audioStream")
            downloadManager.downloadWithAndroidManager(audioStream.url, playerInfo.title, audioStream.mimeType)
        }
    }

}