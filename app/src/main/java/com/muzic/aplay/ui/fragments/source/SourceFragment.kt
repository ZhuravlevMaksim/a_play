package com.muzic.aplay.ui.fragments.source

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.muzic.aplay.R
import com.muzic.aplay.viewmodels.YoutubeViewModel
import kotlinx.android.synthetic.main.youtube_download_activity.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SourceFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.youtube_download_activity, container, false)

    private val viewModel: YoutubeViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        downloadButton.setOnClickListener {
            viewModel.download(urlInput.text.toString())
        }
    }

}