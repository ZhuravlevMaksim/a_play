package com.muzic.aplay.ui


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.muzic.aplay.R
import com.muzic.aplay.viewmodels.YoutubeViewModel
import kotlinx.android.synthetic.main.youtube_download_activity.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class YoutubeDownloadActivity : AppCompatActivity() {

    private val viewModel: YoutubeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.youtube_download_activity)

        intentHandler(intent)

        downloadButton.setOnClickListener {
            viewModel.download(urlInput.text.toString())
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intentHandler(intent)
    }

    private fun intentHandler(intent: Intent?) {
        intent?.extras?.let {
            Timber.d("handle intent ${it.getString(Intent.EXTRA_TEXT)}")
            urlInput.setText(it.getString(Intent.EXTRA_TEXT))
        }
    }

}