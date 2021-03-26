package com.muzic.aplay.ui.fragments.podcast

import android.app.SearchManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.muzic.aplay.R
import org.koin.android.ext.android.inject
import timber.log.Timber

class PodcastFragment : Fragment() {

    private val TAG = javaClass.simpleName
    private val podcastRepo by inject<PodcastRepo>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.podcast_fragment, container, false)

    override fun onStart() {
        super.onStart()
        if (mediaBrowser.isConnected) {
            val fragmentActivity = activity as FragmentActivity
            if (MediaControllerCompat.getMediaController(fragmentActivity) == null) {
                registerMediaController(mediaBrowser.sessionToken)
            }
        } else {
            mediaBrowser.connect()
        }
    }

    private fun startPlaying(episodeViewData: PodcastViewModel.EpisodeViewData) {
        val fragmentActivity = activity as FragmentActivity
        val controller = MediaControllerCompat.getMediaController(fragmentActivity)
        controller.transportControls.playFromUri(Uri.parse(episodeViewData.mediaUrl), null)
    }

    override fun onStop() {
        super.onStop()
        val fragmentActivity = activity as FragmentActivity
        if (MediaControllerCompat.getMediaController(fragmentActivity) != null) {
            mediaControllerCallback?.let {
                MediaControllerCompat.getMediaController(fragmentActivity)
                    .unregisterCallback(it)
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY) ?: return
            performSearch(query)
        }
    }

    private fun performSearch(term: String) {
        val itunesService = ItunesService.instance
        val itunesRepo = ItunesRepo(itunesService)
        itunesRepo.searchByTerm(term) {
            Timber.i(TAG, "Results = $it")

            it?.forEach {
                podcastRepo.getPodcast(it.feedUrl) {
                    Timber.i(TAG, "Podcast = $it")
                }
            }
        }
    }

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaControllerCallback: MediaControllerCallback? = null

    inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            println("metadata changed to ${metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)}")
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            println("state changed to $state")
        }
    }

    inner class MediaBrowserCallBacks : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            registerMediaController(mediaBrowser.sessionToken)
            println("onConnected")
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            println("onConnectionSuspended")
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            println("onConnectionFailed")
        }
    }

    private fun initMediaBrowser() {
        val fragmentActivity = activity as FragmentActivity
        mediaBrowser = MediaBrowserCompat(
            fragmentActivity, ComponentName(fragmentActivity, PodcastMediaService::class.java),
            MediaBrowserCallBacks(),
            null
        )
    }

    private fun registerMediaController(token: MediaSessionCompat.Token) {
        val fragmentActivity = activity as FragmentActivity
        val mediaController = MediaControllerCompat(fragmentActivity, token)
        MediaControllerCompat.setMediaController(fragmentActivity, mediaController)
        mediaControllerCallback = MediaControllerCallback()
        mediaController.registerCallback(mediaControllerCallback!!)
    }

}