package com.muzic.aplay.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TitleViewModel: ViewModel() {

    private val mutableTitle: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val title: LiveData<String> get() = mutableTitle

    fun setTopAppBarTitle(title: String) {
        this.mutableTitle.value = title
    }

}