package com.muzic.aplay.ui

import android.content.Intent
import android.net.Uri
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
import com.muzic.aplay.PlayerService
import com.muzic.aplay.R
import com.muzic.aplay.databinding.YoutubeFragmentBinding
import com.muzic.aplay.db.AudioRepository
import com.muzic.aplay.db.YoutubeStream
import com.muzic.aplay.model.Audio
import com.muzic.aplay.model.From
import com.muzic.aplay.viewmodels.YoutubeViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class SourceFragment : Fragment() {

    private var sourceBinding: YoutubeFragmentBinding? = null
    private val viewModel: YoutubeViewModel by viewModel()
    private val audioRepository: AudioRepository by inject()

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
                        viewModel.validateUrl(item)
                    }
                    onLongClick {
                        viewModel.download(item)
                    }

                }


            }
        }

        viewModel.getAllData.observe(viewLifecycleOwner, { listYoutubeStreams ->
            source.set(listYoutubeStreams.reversed().map { it })
        })

        viewModel.streamValidation.observe(viewLifecycleOwner, { validatedStream ->
            audioRepository.setPlaylist(
                listOf(
                    Audio(
                        validatedStream.title,
                        0,
                        null,
                        0,
                        validatedStream.update,
                        validatedStream.mimeType,
                        validatedStream.contentLength.toDouble(),
                        Uri.parse(validatedStream.url),
                        From.WEB
                    )
                )
            )
            activity?.startService(Intent(activity, PlayerService::class.java).apply { putExtra("position", 0) })
        })

        return binding.root
    }

    override fun onDestroyView() {
        sourceBinding = null
        super.onDestroyView()
    }

}