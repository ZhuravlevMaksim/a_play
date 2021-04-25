package com.muzic.aplay.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.view.Gravity
import android.widget.SeekBar
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
import java.util.concurrent.TimeUnit

class PlayerDetailsUi(private val audioRepository: AudioRepository) {

    private var mediaController: MediaControllerCompat? = null

    fun show(fragment: Fragment?) {
        fragment?.let {
            bind(fragment)
            val executor = Executors.newSingleThreadScheduledExecutor()
            MaterialDialog(fragment.requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT))
                .show {
                    customView(R.layout.player_controls)
                    val playerControls = PlayerControlsBinding.bind(getCustomView())
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

    private fun bind(fragment: Fragment) {
        fragment.requireActivity().bindService(Intent(fragment.context, PlayerService::class.java), object : ServiceConnection {
            var binder: PlayerService.PlayerServiceBinder? = null
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                binder = service as PlayerService.PlayerServiceBinder
                mediaController = MediaControllerCompat(fragment.context, binder!!.mediaSessionToken)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                binder = null
            }
        }, Context.BIND_AUTO_CREATE)
    }
}