package com.muzic.aplay.ui.fragments.podcast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.muzic.aplay.db.Episode
import com.muzic.aplay.db.Podcast
import com.muzic.aplay.db.PodcastSummaryViewData
import java.util.*

class PodcastViewModel(application: Application, private val podcastRepo: PodcastRepo?) : AndroidViewModel(application) {

    var activePodcastViewData: PodcastViewData? = null
    private var activePodcast: Podcast? = null

    data class PodcastViewData(
        var subscribed: Boolean = false,
        var feedTitle: String? = "",
        var feedUrl: String? = "",
        var feedDesc: String? = "",
        var imageUrl: String? = "",
        var episodes: List<EpisodeViewData>
    )

    data class EpisodeViewData(
        var guid: String? = "",
        var title: String? = "",
        var description: String? = "",
        var mediaUrl: String? = "",
        var releaseDate: Date? = null,
        var duration: String? = ""
    )

    fun saveActivePodcast() {
        val repo = podcastRepo ?: return
        activePodcast?.let {
            repo.save(it)
        }
    }

    private fun episodesToEpisodesView(episodes: List<Episode>): List<EpisodeViewData> {
        return episodes.map {
            EpisodeViewData(
                it.guid, it.title, it.description,
                it.mediaUrl, it.releaseDate, it.duration
            )
        }
    }

    private fun podcastToPodcastView(podcast: Podcast): PodcastViewData {
        return PodcastViewData(
            false,
            podcast.feedTitle,
            podcast.feedUrl,
            podcast.feedDesc,
            podcast.imageUrl,
            episodesToEpisodesView(podcast.episodes)
        )
    }

    fun getPodcast(podcastSummaryViewData: PodcastSummaryViewData, callback: (PodcastViewData?) -> Unit) {
        val repo = podcastRepo ?: return
        val feedUrl = podcastSummaryViewData.feedUrl ?: return
        repo.getPodcast(feedUrl) {
            it?.let {
                it.feedTitle = podcastSummaryViewData.name ?: ""
                it.imageUrl = podcastSummaryViewData.imageUrl ?: ""
                activePodcastViewData = podcastToPodcastView(it)
                callback(activePodcastViewData)
            }
        }
    }
}