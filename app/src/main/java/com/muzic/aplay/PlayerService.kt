package com.muzic.aplay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.media.session.MediaButtonReceiver
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.muzic.aplay.db.AudioRepository
import org.koin.android.ext.android.inject


class PlayerService : LifecycleService() {

    private val NOTIFICATION_ID = 0x723
    private val NOTIFICATION_DEFAULT_CHANNEL_ID = "a_play_notify_channel"

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var notificationManager: PlayerNotificationManager
    private val audioRepository by inject<AudioRepository>()

    inner class PlayerServiceBinder : Binder() {
        val mediaSessionToken: MediaSessionCompat.Token
            get() = mediaSession.sessionToken
    }

    override fun onCreate() {
        super.onCreate()

        val nc = NotificationChannel(NOTIFICATION_DEFAULT_CHANNEL_ID, "a play channel", IMPORTANCE_DEFAULT)
        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.createNotificationChannel(nc)

        exoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(DefaultTrackSelector(this))
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setLoadControl(DefaultLoadControl()).build()
            .apply {
                this.addListener(exoPlayerListener)
            }

        notificationManager = PlayerNotificationManager(
            this,
            NOTIFICATION_DEFAULT_CHANNEL_ID,
            NOTIFICATION_ID,
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return audioRepository.current.value?.title ?: ""
                }

                override fun getCurrentContentText(player: Player): CharSequence {
                    return audioRepository.current.value?.details() ?: ""
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    return null
                }

                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap {
                    return BitmapFactory.decodeResource(resources, R.drawable.default_art)
                }
            },
            object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    stopForeground(true)
                    stopSelf()
                }

                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                    ContextCompat.startForegroundService(applicationContext, Intent(applicationContext, this.javaClass))
                    startForeground(notificationId, notification)
                }
            }
        )

        mediaSession = MediaSessionCompat(this, "PlayerService",
            ComponentName(applicationContext, MediaButtonReceiver::class.java),
            packageManager?.getLaunchIntentForPackage(packageName)?.let {
                PendingIntent.getActivity(this@PlayerService, 0, it, 0)
            }).apply { isActive = true }

        notificationManager.setMediaSessionToken(mediaSession.sessionToken)
        notificationManager.setControlDispatcher(DefaultControlDispatcher(30_000, 10_000))
        notificationManager.setPlayer(exoPlayer)

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoPlayer)

        audioRepository.pathAudios.observe(this) { list ->
            exoPlayer.addMediaItems(list.map { MediaItem.fromUri(it.uri) }.toMutableList())
        }
        audioRepository.current.observe(this, {
            it?.let {
                exoPlayer.seekTo(it.position, 0)
                exoPlayer.prepare()
                exoPlayer.play()
            }
        })
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.run {
            isActive = false
            release()
        }
        exoPlayer.release()
    }

    private val exoPlayerListener: Player.EventListener = object : Player.EventListener {
        var prevWindowIndex: Int? = null
        override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {}
        override fun onLoadingChanged(isLoading: Boolean) {}
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    notificationManager.setPlayer(exoPlayer)
                    if (!playWhenReady) {
                        stopForeground(false)
                    }
                }
                else -> notificationManager.setPlayer(null)
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {

        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (prevWindowIndex != exoPlayer.currentWindowIndex) {
                prevWindowIndex = exoPlayer.currentWindowIndex
                audioRepository.setCurrentIndex(exoPlayer.currentWindowIndex)
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {}
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}

    }
}