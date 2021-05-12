package com.muzic.aplay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector.ALL_PLAYBACK_ACTIONS
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.muzic.aplay.db.AudioRepository
import org.koin.android.ext.android.inject
import java.util.*
import java.util.concurrent.*


class PlayerService : Service() {

    private val NOTIFICATION_ID = 0x723
    private val NOTIFICATION_DEFAULT_CHANNEL_ID = "a_play_notify_channel"

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var notificationManager: PlayerNotificationManager
    private val audioRepository: AudioRepository by inject()

    inner class PlayerServiceBinder : Binder() {
        val mediaSessionToken: MediaSessionCompat.Token
            get() = mediaSession.sessionToken
    }

    override fun onBind(intent: Intent?): IBinder {
        return PlayerServiceBinder()
    }

    override fun onCreate() {
        super.onCreate()

        val nc = NotificationChannel(NOTIFICATION_DEFAULT_CHANNEL_ID, "a play channel", IMPORTANCE_LOW)
        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.createNotificationChannel(nc)

        exoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(DefaultTrackSelector(this))
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
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
                    return audioRepository.currentPlaying.value?.title ?: ""
                }

                override fun getCurrentContentText(player: Player): CharSequence {
                    return audioRepository.currentPlaying.value?.details() ?: ""
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
        mediaSessionConnector.setEnabledPlaybackActions(ALL_PLAYBACK_ACTIONS)
        mediaSessionConnector.setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
                return MediaDescriptionCompat.Builder()
                    .setTitle(player.getMediaItemAt(windowIndex).mediaMetadata.title)
                    .build()
            }
        })
        mediaSessionConnector.setPlayer(exoPlayer)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        audioRepository.currentPathAudios.value?.let { list ->
            exoPlayer.setMediaItems(list.map { MediaItem.fromUri(it.uri) }.toMutableList())
        }
        val position = intent!!.getIntExtra("position", 0)
        exoPlayer.seekTo(position, 0)
        exoPlayer.prepare()
        exoPlayer.play()
        return super.onStartCommand(intent, flags, startId)
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

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    notificationManager.setPlayer(exoPlayer)
                    audioRepository.setCurrentPlaying(exoPlayer.currentWindowIndex)
                    if (!playWhenReady) {
                        stopForeground(false)
                    }
                }
                Player.STATE_ENDED -> audioRepository.setCurrentPlaying(null)
                else -> notificationManager.setPlayer(null)
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED == reason) {
                prevWindowIndex = null
                audioRepository.setCurrentPlaying(null)
                return
            }
            if (mediaItem != null && prevWindowIndex != exoPlayer.currentWindowIndex) {
                prevWindowIndex = exoPlayer.currentWindowIndex
            }
        }

    }
}