package com.muzic.aplay.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.muzic.aplay.db.AudioRepository
import com.muzic.aplay.model.Audio

class MusicViewModel(private val repository: AudioRepository) : ViewModel() {

    private val mutAudio: MutableLiveData<List<Audio>> by lazy {
        MutableLiveData<List<Audio>>()
    }

    val audio: LiveData<List<Audio>> get() = mutAudio

    public fun queryForMusicFromPath(path: String) {
        mutAudio.value = repository.queryForMusic().filter { it.relativePath == path }
    }

    public fun queryForAllMusic() {
        mutAudio.value = repository.queryForMusic()
    }

}