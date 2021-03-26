package com.muzic.aplay.ui.fragments.source

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afollestad.recyclical.datasource.emptyDataSource
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.swipe.SwipeLocation
import com.afollestad.recyclical.swipe.withSwipeAction
import com.afollestad.recyclical.withItem
import com.muzic.aplay.R
import com.muzic.aplay.databinding.YoutubeFragmentBinding
import com.muzic.aplay.db.YoutubeStream
import com.muzic.aplay.ui.fragments.audiolist.AudioViewRow
import com.muzic.aplay.viewmodels.YoutubeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class SourceFragment : Fragment() {

    private var sourceBinding: YoutubeFragmentBinding? = null
    private val viewModel: YoutubeViewModel by viewModel()

    private val source = emptyDataSource()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = YoutubeFragmentBinding.inflate(inflater, container, false)

        sourceBinding = binding

        arguments?.getString("url")?.let {
            viewModel.getStreamFromUrl(it)
        }

        binding.youtubeStreams.let {
            it.setup {

                withDataSource(source)

                withSwipeAction(SwipeLocation.LEFT) {
                    icon(R.drawable.ic_baseline_delete_24)
                    text(R.string.delete)
                    color(R.color.colorPrimary)
                    callback { index, item ->
                        viewModel.remove(item as YoutubeStream)
                        true
                    }
                }

                withItem<YoutubeStream, AudioViewRow>(R.layout.audio_list_row) {
                    onBind(::AudioViewRow) { _, item ->
                        title.text = item.title
                        description.text = item.details()
                    }

                    onClick {
                        viewModel.download(item)
                    }

                    onLongClick {

                    }

                }


            }
        }

        viewModel.getAllData.observe(viewLifecycleOwner, { listYoutubeStreams ->
            source.set(listYoutubeStreams.map { it })
        })

        return binding.root
    }

    override fun onDestroyView() {
        sourceBinding = null
        super.onDestroyView()
    }

}