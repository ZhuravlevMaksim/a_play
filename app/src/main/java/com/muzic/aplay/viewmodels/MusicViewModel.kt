package com.muzic.aplay.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.muzic.aplay.db.AudioRepository
import com.muzic.aplay.model.Audio

class MusicViewModel(private val repository: AudioRepository) : ViewModel() {

    private val mutAudios: MutableLiveData<List<Audio>> by lazy {
        MutableLiveData<List<Audio>>()
    }

    val audios: LiveData<List<Audio>> get() = mutAudios
    val current: LiveData<Audio> get() = repository.mCurrent

    fun queryForMusicFromPath(path: String) {
        mutAudios.value = repository.audios.filter { it.relativePath == path }
    }

    fun queryForAllMusic() {
        mutAudios.value = repository.audios
    }

    fun setCurrent(audio: Audio){
        repository.mCurrent.value = audio
    }

}