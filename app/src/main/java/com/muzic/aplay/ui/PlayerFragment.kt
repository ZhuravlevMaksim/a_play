package com.muzic.aplay.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.afollestad.recyclical.datasource.emptyDataSource
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.muzic.aplay.PlayerService
import com.muzic.aplay.R
import com.muzic.aplay.databinding.PlayerListViewBinding
import com.muzic.aplay.model.Audio
import com.muzic.aplay.viewmodels.MusicViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class PlayerFragment : Fragment() {

    private var playerBinding: PlayerListViewBinding? = null
    private val musicViewModel: MusicViewModel by viewModel()

    private val source = emptyDataSource()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = PlayerListViewBinding.inflate(inflater, container, false)
        binding.songs.let { songs ->
            songs.setup {
                withDataSource(source)
                withItem<Audio, AudioViewRow>(R.layout.audio_list_row) {
                    onBind(::AudioViewRow) { _, item ->
                        title.text = item.title
                        description.text = item.details()
                    }
                    onClick {
                        activity?.startService(Intent(context, PlayerService::class.java).apply {
                            putExtra("song", item)
                        })
                    }
                    onLongClick { index ->
                        val itemView = songs.findViewHolderForAdapterPosition(index)?.itemView
                        itemView?.let { view ->
                            PopupMenu(requireActivity(), view).apply {
                                inflate(R.menu.popup)
                                menu.findItem(R.id.popup_title).title = item.title
                                gravity = Gravity.END
                                setOnMenuItemClickListener {
                                    return@setOnMenuItemClickListener true
                                }
                                show()
                            }
                        }
                    }
                }
            }
        }
        musicViewModel.audio.observe(viewLifecycleOwner) { source.set(it) }
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

val PLAYER_FOLDER_INTENT: String get() = "player_folder_intent"
val PLAYER_TITLE_INTENT: String get() = "player_title_intent"
val PLAYER_SUBTITLE_INTENT: String get() = "player_subtitle_intent"

fun Toolbar.makeScrollable() = try {
    val toolbarClass = Toolbar::class.java
    val titleTextViewField = toolbarClass.getDeclaredField("mTitleTextView").apply { this.isAccessible = true }
    titleTextViewField.get(this).let {
        it as TextView
        it.isSelected = true
        it.setHorizontallyScrolling(true)
        it.ellipsize = TextUtils.TruncateAt.MARQUEE
        it.marqueeRepeatLimit = -1
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}