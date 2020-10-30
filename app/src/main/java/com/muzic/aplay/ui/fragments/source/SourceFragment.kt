package com.muzic.aplay.ui.fragments.source

import android.os.Bundle
import com.muzic.aplay.R
import com.muzic.aplay.ui.fragments.BottomNavigationFragmentParent
import com.muzic.aplay.viewmodels.YoutubeViewModel
import kotlinx.android.synthetic.main.youtube_download_activity.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SourceFragment: BottomNavigationFragmentParent(R.layout.youtube_download_activity, "A source") {
    companion object {
        fun newInstance() = SourceFragment()
    }

    private val viewModel: YoutubeViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        downloadButton.setOnClickListener {
            viewModel.download(urlInput.text.toString())
        }
    }

}