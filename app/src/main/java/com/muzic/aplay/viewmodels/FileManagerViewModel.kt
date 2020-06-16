package com.muzic.aplay.viewmodels

import android.net.Uri
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muzic.aplay.PlayFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FileManagerViewModel(val playFileManager: PlayFileManager) : ViewModel() {

    private val _states = MutableLiveData<List<PlayFileManager.AudioFile>>()
    val states: LiveData<List<PlayFileManager.AudioFile>> = _states

    fun list(data: Uri) {
        viewModelScope.launch(Dispatchers.Main) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                _states.value = playFileManager.listAudio(data)
            }
        }
    }

}