package com.muzic.aplay

import android.app.Application
import com.muzic.aplay.viewmodels.MusicViewModel
import com.muzic.aplay.viewmodels.YoutubeViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber
import timber.log.Timber.DebugTree

class APlayApplication : Application() {

    private val module = module {
        single { OkHttpClient() }
        single { PlayDownloadManager(get(), get()) }
        single { PlayerService() }
        viewModel { YoutubeViewModel(get(), get()) }
        viewModel { MusicViewModel(this@APlayApplication) }
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
        startKoin {
            androidLogger()
            androidContext(this@APlayApplication)
            modules(module)
        }
    }
}

