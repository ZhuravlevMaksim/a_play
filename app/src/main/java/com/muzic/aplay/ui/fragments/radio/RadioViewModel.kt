package com.muzic.aplay.ui.fragments.radio

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.net.InetAddress


class RadioViewModel(private val client: OkHttpClient) : ViewModel() {

    private val host = "all.api.radio-browser.info"
    private val servers by lazy { getServerList() }
    private val gson by lazy { Gson() }

    val stations: MutableLiveData<Array<RadioStation>> by lazy {
        MutableLiveData<Array<RadioStation>>()
    }

    private fun getServerList(): Array<String> {
        val list = mutableSetOf<String>()
        for (item in InetAddress.getAllByName(host)) {
            if (item.canonicalHostName != host && item.canonicalHostName != item.hostAddress) {
                list.add(item.canonicalHostName)
            }
        }
        return list.toTypedArray()
    }

    fun request(path: String) {
        stations.value.isNullOrEmpty().let {
            Timber.tag("TAG").i("station currently: ${stations.value}")
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val request = Request.Builder().url("https://${servers[0]}/$path").build()

                    val response = client.newCall(request).execute()

                    response.use {
                        if (response.isSuccessful) {
                            response.body?.let {
                                stations.postValue(gson.fromJson(it.string(), Array<RadioStation>::class.java))
                            }
                        }
                    }
                }
            }
        }
    }
}

