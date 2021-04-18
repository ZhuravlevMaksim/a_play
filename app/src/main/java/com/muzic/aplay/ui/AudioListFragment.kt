package com.muzic.aplay.ui

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.recyclical.datasource.emptySelectableDataSource
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.viewholder.*
import com.afollestad.recyclical.withItem
import com.muzic.aplay.PlayerService
import com.muzic.aplay.R
import com.muzic.aplay.databinding.PlayerControlsBinding
import com.muzic.aplay.databinding.PlayerListViewBinding
import com.muzic.aplay.db.AudioRepository
import com.muzic.aplay.model.Audio
import org.koin.android.ext.android.inject
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class PlayerFragment : Fragment() {

    private var playerListBinding: PlayerListViewBinding? = null
    private val audioRepository: AudioRepository by inject()
    private val source = emptySelectableDataSource()
    private var mediaController: MediaControllerCompat? = null
    private var prev: Audio? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = PlayerListViewBinding.inflate(inflater, container, false)
        binding.songs.let { songs ->
            songs.setup {
                withDataSource(source)
                withItem<Audio, AudioViewRow>(R.layout.audio_list_row) {
                    onBind(::AudioViewRow) { _, item ->
                        title.text = item.title
                        description.text = item.details()
                        if (isSelected()) {
                            this.itemView.setBackgroundColor(resources.getColor(R.color.nowPlayingBackground))
                        } else {
                            this.itemView.setBackgroundColor(resources.getColor(R.color.white))
                        }
                    }
                    onClick { position ->
                        activity?.startService(Intent(activity, PlayerService::class.java).apply { putExtra("position", position) })
                        show()
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
        audioRepository.audios.observe(viewLifecycleOwner) { source.set(it) }
        audioRepository.currentPlaying.observe(viewLifecycleOwner) {
            it?.let { source.select(it) }
            prev?.let { prev -> if (prev != it) source.deselect(prev) }
            prev = it
        }
        arguments?.getString(PLAYER_FOLDER_INTENT)?.let { audioRepository.setCurrentPath(it) }
        playerListBinding = binding


        activity?.bindService(Intent(context, PlayerService::class.java), object : ServiceConnection {
            var binder: PlayerService.PlayerServiceBinder? = null
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                binder = service as PlayerService.PlayerServiceBinder
                mediaController = MediaControllerCompat(context, binder!!.mediaSessionToken)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                binder = null
            }
        }, BIND_AUTO_CREATE)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
    }

    private fun setupToolbar() {
        playerListBinding?.detailsToolbar?.run {
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
        playerListBinding = null
        super.onDestroyView()
    }

    private fun show() {
        activity?.let { activity ->
            val executor = Executors.newSingleThreadScheduledExecutor()
            MaterialDialog(activity, BottomSheet(LayoutMode.WRAP_CONTENT))
                .show {
                    customView(R.layout.player_controls)
                    val playerControls = PlayerControlsBinding.bind(getCustomView())
                    audioRepository.currentPlaying.observe(viewLifecycleOwner) {
                        playerControls.song.text = it?.title ?: ""
                        playerControls.details.text = it?.details() ?: ""
                    }
                    val scheduleAtFixedRate = executor.scheduleAtFixedRate({
                        val duration = mediaController?.metadata?.getLong("android.media.metadata.DURATION")
                        val position = mediaController?.playbackState?.position ?: 0
                        playerControls.seekBar.max = duration?.toInt() ?: 100
                            duration?.let {
                            playerControls.seekBar.progress = position.toInt()
                        }
                    }, 0, 1, TimeUnit.SECONDS)
                    setOnDismissListener {
                        scheduleAtFixedRate.cancel(true)
                    }
                    playerControls.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) {
                                mediaController?.transportControls?.seekTo(progress.toLong())
                            }
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {

                        }
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {

                        }
                    })
                    playerControls.playbackSpeed.setOnClickListener { view ->
                        PopupMenu(activity, view).apply {
                            inflate(R.menu.popup_speed)
                            gravity = Gravity.END
                            setOnMenuItemClickListener { menuItem ->
                                val speed = when (menuItem.itemId) {
                                    R.id.speed_1 -> 1.00F
                                    R.id.speed_1_5 -> 1.5F
                                    R.id.speed_2 -> 2.0F
                                    else -> 1.0F
                                }
                                return@setOnMenuItemClickListener true
                            }
                            show()
                        }
                    }
                }
        }
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