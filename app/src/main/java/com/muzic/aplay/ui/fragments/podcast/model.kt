package com.muzic.aplay.ui.fragments.podcast

import java.util.*

data class Episode(
    var guid: String = "",
    var title: String = "",
    var description: String = "",
    var mediaUrl: String = "",
    var mimeType: String = "",
    var releaseDate: Date = Date(),
    var duration: String = ""
)

data class PodcastResponse(val resultCount: Int, val results: List<ItunesPodcast>) {
    data class ItunesPodcast(
        val collectionCensoredName: String,
        val feedUrl: String,
        val artworkUrl30: String,
        val releaseDate: String
    )
}

data class PodcastSummaryViewData(
    var name: String? = "",
    var lastUpdated: String? = "",
    var imageUrl: String? = "",
    var feedUrl: String? = ""
)

data class Podcast(
    var feedUrl: String = "",
    var feedTitle: String = "",
    var feedDesc: String = "",
    var imageUrl: String = "",
    var lastUpdated: Date = Date(),
    var episodes: List<Episode> = listOf()
)

data class RssFeedResponse(
    var title: String = "",
    var description: String = "",
    var summary: String = "",
    var lastUpdated: Date = Date(),
    var episodes: MutableList<EpisodeResponse>? = null
) {
    data class EpisodeResponse(
        var title: String? = null,
        var link: String? = null,
        var description: String? = null,
        var guid: String? = null,
        var pubDate: String? = null,
        var duration: String? = null,
        var url: String? = null,
        var type: String? = null
    )
}