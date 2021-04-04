package com.muzic.aplay

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

const val CHANNEL_ID = "a_play_foreground_player_service"

class PlayerService() : Service() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private val player: ExoPlayerWrapper by lazy {
        ExoPlayerWrapper(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(this, "PlayerService").apply {
            setSessionActivity(packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this@PlayerService, 0, sessionIntent, 0)
            })
            isActive = true
        }

        notificationManager = NotificationManager(this)
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        notificationManager.setPlayer(player.player)
    }

    fun getMediaSessionToken(): MediaSessionCompat.Token {
        return mediaSession.sessionToken
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("song")?.let {
            player.play(it)
        }

        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        player.stop()
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }
        player.destroy()
    }

}

class ExoPlayerWrapper(val context: Context) {

    private val listener = PlayerEventListener()
    val player = SimpleExoPlayer.Builder(context).build().apply {
        setAudioAttributes(AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true)
        setHandleAudioBecomingNoisy(true)
        addListener(listener)
    }

    fun destroy() {
        player.removeListener(listener)
        player.release()
    }

    fun stop() {
        player.stop(true)
    }

    fun play(urls: List<String>) {
        urls.map { MediaItem.fromUri(it) }.forEach { player.setMediaItem(it) }
        player.prepare()
        player.play()
    }

    fun play(url: String) {
        play(listOf(url))
    }

    private inner class PlayerEventListener : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
//                    notificationManager.showNotificationForPlayer(player)
                    if (playbackState == Player.STATE_READY) {

                        // When playing/paused save the current media item in persistent
                        // storage so that playback can be resumed between device reboots.
                        // Search for "media resumption" for more information.
//                        saveRecentSongToStorage()

                        if (!playWhenReady) {
//                           stopForeground(false)
                        }
                    }
                }
                else -> {
//                    notificationManager.hideNotification()
                }
            }
        }
    }
}
