package com.muzic.common

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.muzic.common.source.MediaSource
import com.muzic.common.source.YoutubeMediaSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PlayerService : MediaBrowserServiceCompat() {

    private lateinit var notificationManager: NotificationManager
    private  var mediaSource: MediaSource? = null

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()
    private lateinit var storage: PersistentStorage

//    private val tree: List<MediaMetadataCompat> by lazy {
//        mediaSource.playableList()
//    }

    private val player: ExoPlayerWrapper by lazy {
        ExoPlayerWrapper(this)
    }

    override fun onCreate() {
        super.onCreate()

        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        mediaSession = MediaSessionCompat(this, "PlayerService").apply {
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        notificationManager = NotificationManager(this, mediaSession.sessionToken)

//        mediaSource = YoutubeMediaSource()

        this.serviceScope.launch {
            (mediaSource as YoutubeMediaSource).load("https://www.youtube.com/watch?v=GKnQGP67Xd0")
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
//        mediaSessionConnector.setPlaybackPreparer(UampPlaybackPreparer())
//        mediaSessionConnector.setQueueNavigator(UampQueueNavigator(mediaSession))
//
        notificationManager.setPlayer(player.player)
//
//        storage = PersistentStorage.getInstance(applicationContext)

//        mediaSource.whenReady { successfullyInitialized ->
//            if (successfullyInitialized) {
//                player.play(mediaSource.playableList())
//            }
//        }

    }

    override fun onTaskRemoved(rootIntent: Intent) {
//        saveRecentSongToStorage()
        super.onTaskRemoved(rootIntent)
        player.player.stop(true)
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }

        player.destroy()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("/", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
//        if (parentId == "/") {
//            val resultsSent = mediaSource.whenReady { successfullyInitialized ->
//                if (successfullyInitialized) {
//                    player.play(mediaSource.playableList())

//                    val children = tree.map { item ->
//                        MediaBrowserCompat.MediaItem(item.description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
//                    }
//                    result.sendResult(children as MutableList<MediaBrowserCompat.MediaItem>?)
//                } else {
//                    mediaSession.sendSessionEvent("NETWORK_FAILURE", null)
//                    result.sendResult(null)
//                }
//            }
//            if (!resultsSent) {
//                result.detach()
//            }
//        }
    }

}

class ExoPlayerWrapper(val context: Context) {

    private val listener = PlayerEventListener()
    public val player =  SimpleExoPlayer.Builder(context).build().apply {
        setAudioAttributes(AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true)
        setHandleAudioBecomingNoisy(true)
        addListener(listener)
    }

    fun destroy() {
        player.removeListener(listener)
        player.release()
    }

    public fun play(urls: List<String>) {
        urls.map { MediaItem.fromUri(it) }.forEach { player.setMediaItem(it) }
        player.prepare()
        player.play()
    }

    public fun play(url: String) {
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
