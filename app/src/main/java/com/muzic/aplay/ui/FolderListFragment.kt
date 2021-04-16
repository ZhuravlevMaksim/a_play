package com.muzic.aplay.ui

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
import com.muzic.aplay.db.AudioRepository
import org.koin.android.ext.android.inject


class AudioListFragment : Fragment() {

    private val audioRepository: AudioRepository by inject()
    private var audioListBinding: AudioListFragmentBinding? = null

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
        audioRepository.audios.observe(viewLifecycleOwner) { list ->
            list.groupBy { it.relativePath }.let { groupBy ->
                source.set(groupBy.keys.map { Row(it, "${groupBy[it]?.size} Songs") })
            }
        }
        return binding.root
    }

    private fun onRowClick(item: Row) {
        activity?.let {
            it.navigate(R.id.playerFragment, Bundle().apply {
                this.putString(PLAYER_FOLDER_INTENT, item.title)
                this.putString(PLAYER_TITLE_INTENT, item.title)
                this.putString(PLAYER_SUBTITLE_INTENT, item.description)
            })
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