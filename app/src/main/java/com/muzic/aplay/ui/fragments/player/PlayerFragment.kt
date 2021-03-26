package com.muzic.aplay.ui.fragments.player

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
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

        arguments?.getString(PLAYER_FOLDER_INTENT)?.let {
            musicViewModel.queryForMusicFromPath(it)
        }

        playerBinding = binding

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
    }

    private fun setupToolbar() {
        playerBinding?.detailsToolbar?.run {
            overflowIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_baseline_more_vert_24)
            title = arguments?.getString(PLAYER_TITLE_INTENT, "")
            subtitle = arguments?.getString(PLAYER_SUBTITLE_INTENT, "")
            this.makeScrollable()
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        playerBinding = null
        super.onDestroyView()
    }

}

public val PLAYER_FOLDER_INTENT: String get() = "player_folder_intent"
public val PLAYER_TITLE_INTENT: String get() = "player_title_intent"
public val PLAYER_SUBTITLE_INTENT: String get() = "player_subtitle_intent"

fun Toolbar.makeScrollable() = try {
    val toolbarClass = Toolbar::class.java
    val titleTextViewField = toolbarClass.getDeclaredField("mTitleTextView")
    titleTextViewField.isAccessible = true
    val textView = titleTextViewField.get(this) as TextView

    textView.let {
        it.isSelected = true
        it.setHorizontallyScrolling(true)
        it.ellipsize = TextUtils.TruncateAt.MARQUEE
        it.marqueeRepeatLimit = -1
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}