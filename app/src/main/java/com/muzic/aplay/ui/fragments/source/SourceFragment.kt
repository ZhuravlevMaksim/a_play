package com.muzic.aplay.ui.fragments.source

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.muzic.aplay.R
import com.muzic.aplay.db.YoutubeStreamDatabase
import com.muzic.aplay.ui.inflateMenu
import com.muzic.aplay.viewmodels.YoutubeViewModel
import kotlinx.android.synthetic.main.youtube_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SourceFragment : Fragment() {

    private val viewModel: YoutubeViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.youtube_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        downloadButton.setOnClickListener {
            viewModel.download(urlInput.text.toString())
        }
        inflateMenu("A Source", R.menu.source_menu)
        context?.let {
            Timber.i("getAllData")

            val dao = YoutubeStreamDatabase.get(requireContext()).dao()

            dao.getAllData().observe(viewLifecycleOwner, { t ->
                t.forEach {
                    Timber.i("${it.id} ${it.url}")
                }
            })
        }
        arguments?.getString("url")?.let {
            urlInput.setText(it)
        }
    }

}