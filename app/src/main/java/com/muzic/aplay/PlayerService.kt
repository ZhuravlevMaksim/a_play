package com.muzic.aplay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.muzic.aplay.db.AudioRepository
import com.muzic.aplay.model.Audio
import org.koin.android.ext.android.inject


class PlayerService : Service() {

    private val NOTIFICATION_ID = 0x723
    private val NOTIFICATION_DEFAULT_CHANNEL_ID = "a_play_notify_channel"

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var notificationManager: PlayerNotificationManager
    private val audioRepository by inject<AudioRepository>()

    override fun onBind(intent: Intent?): IBinder {
        return PlayerServiceBinder()
    }

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
                    return audioRepository.currentAudio.value?.title ?: ""
                }

                override fun getCurrentContentText(player: Player): CharSequence {
                    return audioRepository.currentAudio.value?.details() ?: ""
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
        mediaSessionConnector.setPlaybackPreparer(object : MediaSessionConnector.PlaybackPreparer {
            override fun onCommand(
                player: Player,
                controlDispatcher: ControlDispatcher,
                command: String,
                extras: Bundle?,
                cb: ResultReceiver?
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun getSupportedPrepareActions(): Long {
                return PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_FAST_FORWARD or
                        PlaybackStateCompat.ACTION_REWIND or
                        PlaybackStateCompat.ACTION_STOP
            }

            override fun onPrepare(playWhenReady: Boolean) {
                val audio: Audio = audioRepository.currentAudio.value ?: return
                exoPlayer.addMediaItems(audioRepository.audios.map { MediaItem.fromUri(it.uri) }.toMutableList())
                exoPlayer.seekToDefaultPosition(audio.position)
                exoPlayer.prepare()
            }

            override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
                TODO("Not yet implemented")
            }

            override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
                TODO("Not yet implemented")
            }

            override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }
        exoPlayer.release()
    }

    private val exoPlayerListener: Player.EventListener = object : Player.EventListener {
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
                ExoPlayer.STATE_ENDED -> if (playWhenReady) exoPlayer.next()
                else -> notificationManager.setPlayer(null)

            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {

        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (reason == 1) { // next
                audioRepository.setCurrentIndex(exoPlayer.currentWindowIndex)
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {}
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}

    }
}