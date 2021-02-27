package com.muzic.aplay.ui.fragments.podcast

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class SearchViewModel(application: Application) :
    AndroidViewModel(application) {
    var iTunesRepo: ItunesRepo? = null

    fun searchPodcasts(term: String, callback: (List<PodcastSummaryViewData>) -> Unit) {
        iTunesRepo?.searchByTerm(term) { results ->
            if (results == null) {
                callback(emptyList())
            } else {
                val searchViews = results.map { podcast -> itunesPodcastToPodcastSummaryView(podcast) }
                callback(searchViews)
            }
        }
    }
}

internal fun itunesPodcastToPodcastSummaryView(itunesPodcast: PodcastResponse.ItunesPodcast): PodcastSummaryViewData {
    return PodcastSummaryViewData(
        itunesPodcast.collectionCensoredName,
        itunesPodcast.releaseDate,
        itunesPodcast.artworkUrl30,
        itunesPodcast.feedUrl
    )
}

