package com.muzic.aplay.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TitleViewModel: ViewModel() {

    val title: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

}