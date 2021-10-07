package com.muzic.aplay.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Gravity
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.muzic.aplay.PlayerService
import com.muzic.aplay.R
import com.muzic.aplay.databinding.PlayerControlsBinding
import com.muzic.aplay.db.AudioRepository
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class PlayerDetailsUi(private val audioRepository: AudioRepository) {

    private var mediaController: MediaControllerCompat? = null
    private var controls: PlayerControlsBinding? = null
    private var sleepTask: ScheduledFuture<*>? = null
    private val executor by lazy { Executors.newSingleThreadScheduledExecutor() }
    private var repeat: Boolean = false

    fun show(fragment: Fragment?) {
        fragment?.let {
            bind(fragment)
            MaterialDialog(fragment.requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT))
                .show {
                    customView(R.layout.player_controls)
                    val playerControls = PlayerControlsBinding.bind(getCustomView())
                    controls = playerControls
                    audioRepository.currentPlaying.observe(fragment.viewLifecycleOwner) {
                        playerControls.song.text = it?.title ?: ""
                        playerControls.details.text = it?.details() ?: ""
                        playerControls.seekBar.max = it?.duration?.toInt() ?: 0
                        playerControls.seekBar.progress = 0
                    }
                    val scheduleAtFixedRate = executor.scheduleAtFixedRate({
                        val duration = mediaController?.metadata?.getLong("android.media.metadata.DURATION")
                        val position = mediaController?.playbackState?.position ?: 0
                        if (playerControls.seekBar.max == 0) {
                            playerControls.seekBar.max = duration?.toInt() ?: 0
                        }
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
                        PopupMenu(fragment.requireContext(), view).apply {
                            inflate(R.menu.popup_speed)
                            gravity = Gravity.END
                            setOnMenuItemClickListener { menuItem ->
                                val speed = when (menuItem.itemId) {
                                    R.id.speed_1 -> 1f
                                    R.id.speed_1_5 -> 1.5f
                                    R.id.speed_2 -> 2f
                                    else -> 1f
                                }
                                mediaController?.transportControls?.setPlaybackSpeed(speed)
                                return@setOnMenuItemClickListener true
                            }
                            show()
                        }
                    }
                    playerControls.sleepTime.setOnClickListener {
                        Toast.makeText(context, "set sleep in 30 minutes", Toast.LENGTH_SHORT).show()
                        if (sleepTask == null || sleepTask?.isCancelled != false || sleepTask?.isDone != false) {
                            sleepTask = executor.schedule({
                                mediaController?.transportControls?.pause()
                            }, 30, TimeUnit.MINUTES)
                        }
                    }
                    playerControls.skipPrev.setOnClickListener {
                        mediaController?.transportControls?.skipToPrevious()
                    }
                    playerControls.skipNext.setOnClickListener {
                        mediaController?.transportControls?.skipToNext()
                    }
                    playerControls.play.setOnClickListener {
                        if (mediaController?.playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
                            mediaController?.transportControls?.pause()
                        } else {
                            mediaController?.transportControls?.play()
                        }
                    }
                    playerControls.repeat.setOnClickListener {
                        if (repeat) {
                            repeat = false
                            mediaController?.transportControls?.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)
                        } else {
                            repeat = true
                            mediaController?.transportControls?.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)
                        }
                    }
                }
        }
    }

    private fun bind(fragment: Fragment) {
        fragment.requireActivity().bindService(Intent(fragment.context, PlayerService::class.java), object : ServiceConnection {
            var binder: PlayerService.PlayerServiceBinder? = null
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                binder = service as PlayerService.PlayerServiceBinder
                mediaController = MediaControllerCompat(fragment.context, binder!!.mediaSessionToken)
                mediaController!!.registerCallback(object : MediaControllerCompat.Callback() {
                    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                        when (state?.state) {
                            PlaybackStateCompat.STATE_NONE,
                            PlaybackStateCompat.STATE_STOPPED,
                            PlaybackStateCompat.STATE_PAUSED -> controls?.play?.setImage(
                                fragment.requireContext(),
                                R.drawable.ic_baseline_play_arrow_24
                            )
                            PlaybackStateCompat.STATE_PLAYING -> controls?.play?.setImage(
                                fragment.requireContext(),
                                R.drawable.ic_baseline_pause_24
                            )
                        }
                    }
                })
            }

            override fun onServiceDisconnected(name: ComponentName) {
                binder = null
            }
        }, Context.BIND_AUTO_CREATE)
    }
}