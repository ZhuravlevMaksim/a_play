package com.muzic.aplay.ui.fragments.audiolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afollestad.recyclical.datasource.emptyDataSource
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.muzic.aplay.R
import com.muzic.aplay.databinding.AudioListFragmentBinding
import com.muzic.aplay.ui.fragments.player.FOLDER_INTENT
import com.muzic.aplay.ui.navigate
import com.muzic.aplay.ui.setTopTitle
import com.muzic.aplay.viewmodels.MusicViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class AudioListFragment : Fragment() {

    private var audioListBinding: AudioListFragmentBinding? = null
    private val musicViewModel: MusicViewModel by viewModel()

    private val source = emptyDataSource()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = AudioListFragmentBinding.inflate(inflater, container, false)

        audioListBinding = binding

        binding.items.let {
            it.setup {

                withDataSource(source)

                withItem<Row, AudioViewRow>(R.layout.audio_list_row) {
                    onBind(::AudioViewRow) { _, item ->
                        title.text = item.title
                        description.text = item.description
                    }

                    onClick {
                        onRowClick(item)
                    }

                    onLongClick { index ->
                        onRowLongClick(item, it.findViewHolderForAdapterPosition(index)?.itemView)
                    }

                }

            }
        }

        musicViewModel.audio.observe(viewLifecycleOwner) { list ->
            list.groupBy { it.relativePath }.let { groupBy ->
                source.set(groupBy.keys.map { Row(it, "${groupBy[it]?.size} Songs") })
            }
        }

        activity?.let {
            it.setTopTitle("A player")
            musicViewModel.queryForAllMusic()
        }

        return binding.root
    }

    private fun onRowClick(item: Row) {
        activity?.let {
            it.navigate(R.id.playerFragment, Bundle().apply { this.putString(FOLDER_INTENT, item.title) })
        }
    }

    private fun onRowLongClick(item: Row, itemView: View?) {
        TODO("Not yet imlemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioListBinding = null
    }

}

data class Row(val title: String?, val description: String?)