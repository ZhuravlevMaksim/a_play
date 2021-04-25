package com.muzic.aplay

import android.app.Application
import com.muzic.aplay.db.AudioRepository
import com.muzic.aplay.download.PlayDownloadManager
import com.muzic.aplay.ui.PlayerDetailsUi
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
        single { PlayerDetailsUi(get()) }
        single { OkHttpClient() }
        single { PlayDownloadManager(get(), get()) }
        single { AudioRepository(this@APlayApplication) }
        viewModel { YoutubeViewModel(get(), get()) }
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

