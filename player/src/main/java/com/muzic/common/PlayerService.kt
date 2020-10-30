package com.muzic.common

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.muzic.common.source.MediaSource
import com.muzic.common.source.NotificationManager
import com.muzic.common.source.PersistentStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PlayerService() : MediaBrowserServiceCompat() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var mediaSource: MediaSource

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()

    private lateinit var storage: PersistentStorage

    private val tree: List<MediaMetadataCompat> by lazy {
        mediaSource.playableList()
    }

    private val dataSourceFactory: DefaultDataSourceFactory by lazy {
        DefaultDataSourceFactory(this, Util.getUserAgent(this, applicationInfo.name), null)
    }

    private val exoPlayer: SimpleExoPlayer by lazy {
        ExoPlayer(this).create()
    }

    override fun onCreate() {
        super.onCreate()

        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        mediaSession = MediaSessionCompat(this, "PlayerService")
            .apply {
                setSessionActivity(sessionActivityPendingIntent)
                isActive = true
            }

        sessionToken = mediaSession.sessionToken

        notificationManager = NotificationManager(this, mediaSession.sessionToken)

        mediaSource = MediaSource()
//        mediaSource = JsonSource(source = remoteJsonSource)
//        serviceScope.launch {
//            mediaSource.load()
//        }

//        mediaSessionConnector = MediaSessionConnector(mediaSession)
//        mediaSessionConnector.setPlaybackPreparer(UampPlaybackPreparer())
//        mediaSessionConnector.setQueueNavigator(UampQueueNavigator(mediaSession))
//
//        notificationManager.showNotificationForPlayer(exoPlayer)
//
//        storage = PersistentStorage.getInstance(applicationContext)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
//        saveRecentSongToStorage()
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop(/* reset= */true)
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }

        //todo singleton
        ExoPlayer(this).destroy()

        //todo singleton
        ServiceJob().cancel()

    }

    /**
     * Returns the "root" media ID that the client should request to get the list of
     * [MediaItem]s to browse/play.
     */
    /**
     * Returns the "root" media ID that the client should request to get the list of
     * [MediaItem]s to browse/play.
     */
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {

        val rootExtras = Bundle().apply {
            putBoolean(CONTENT_STYLE_SUPPORTED, true)
            putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
            putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
        }

        val isRecentRequest = rootHints?.getBoolean(BrowserRoot.EXTRA_RECENT) ?: false
        val browserRootPath = if (isRecentRequest) UAMP_RECENT_ROOT else UAMP_BROWSABLE_ROOT
        return BrowserRoot(browserRootPath, rootExtras)

    }

    override fun onLoadChildren(parentMediaId: String, result: Result<List<MediaItem>>) {
        if (parentMediaId == UAMP_RECENT_ROOT) {
//            result.sendResult(storage.loadRecentSong()?.let { song -> listOf(song) })
        } else {
            val resultsSent = mediaSource.whenReady { successfullyInitialized ->
                if (successfullyInitialized) {
                    val children = tree.map { item ->
                        MediaItem(item.description, MediaItem.FLAG_PLAYABLE)
                    }
                    result.sendResult(children)
                } else {
                    mediaSession.sendSessionEvent(NETWORK_FAILURE, null)
                    result.sendResult(null)
                }
            }
            if (!resultsSent) {
                result.detach()
            }
        }
    }

}

const val UAMP_BROWSABLE_ROOT = "/"
const val UAMP_EMPTY_ROOT = "@empty@"
const val UAMP_RECOMMENDED_ROOT = "__RECOMMENDED__"
const val UAMP_ALBUMS_ROOT = "__ALBUMS__"
const val UAMP_RECENT_ROOT = "__RECENT__"
const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"

private const val CONTENT_STYLE_BROWSABLE_HINT = "BROWSABLE"
private const val CONTENT_STYLE_PLAYABLE_HINT = "PLAYABLE"
private const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
private const val CONTENT_STYLE_LIST = 1
private const val CONTENT_STYLE_GRID = 2

const val NETWORK_FAILURE = "com.example.android.uamp.media.session.NETWORK_FAILURE"
val MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS = "playback_start_position_ms"

class ServiceJob {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    fun doJob() {

    }

    fun cancel() {
        serviceJob.cancel()
    }
}

class ExoPlayer(val context: Context) {

    private val listener = PlayerEventListener()
    private lateinit var player: SimpleExoPlayer

    fun create(): SimpleExoPlayer {
        player = SimpleExoPlayer.Builder(context).build().apply {
            setAudioAttributes(AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true)
            setHandleAudioBecomingNoisy(true)
            addListener(listener)
        }
        return player
    }

    fun destroy() {
        player.removeListener(listener)
        player.release()
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
