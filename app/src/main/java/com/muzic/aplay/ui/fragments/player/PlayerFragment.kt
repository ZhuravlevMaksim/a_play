package com.muzic.aplay.ui.fragments.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afollestad.recyclical.datasource.emptyDataSource
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.muzic.aplay.R
import com.muzic.aplay.databinding.PlayerListViewBinding
import com.muzic.aplay.ui.fragments.audiolist.AudioViewRow
import com.muzic.aplay.ui.fragments.audiolist.Row
import com.muzic.aplay.viewmodels.MusicViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class PlayerFragment : Fragment() {

    private var playerBinding: PlayerListViewBinding? = null
    private val musicViewModel: MusicViewModel by viewModel()

    private val source = emptyDataSource()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = PlayerListViewBinding.inflate(inflater, container, false)

        binding.songs.let {
            it.setup {
                withDataSource(source)
                withItem<Row, AudioViewRow>(R.layout.audio_list_row) {
                    onBind(::AudioViewRow) { _, item ->
                        title.text = item.title
                        description.text = item.description
                    }
                    onClick {

                    }
                    onLongClick {

                    }
                }
            }
        }

        musicViewModel.audio.observe(viewLifecycleOwner) { list ->
            source.set(list.map { Row(it.title, it.details()) })
        }

        arguments?.getString(FOLDER_INTENT)?.let {
            musicViewModel.queryForMusicFromPath(it)
        }

        playerBinding = binding

        return binding.root
    }

    override fun onDestroyView() {
        playerBinding = null
        super.onDestroyView()
    }

}

public val FOLDER_INTENT: String get() = "player_folder_intent"