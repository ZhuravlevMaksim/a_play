package com.muzic.aplay.ui.fragments.podcast

data class PodcastResponse(
    val resultCount: Int,
    val results: List<ItunesPodcast>
) {
    data class ItunesPodcast(
        val collectionCensoredName: String,
        val feedUrl: String,
        val artworkUrl30: String,
        val releaseDate: String
    )
}
