package com.muzic.aplay.db

import androidx.room.*
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Podcast::class,
            parentColumns = ["id"],
            childColumns = ["podcastId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("podcastId")]
)
data class Episode(
    @PrimaryKey var guid: String = "",
    var podcastId: Long? = null,
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

@Entity
data class Podcast(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    var feedUrl: String = "",
    var feedTitle: String = "",
    var feedDesc: String = "",
    var imageUrl: String = "",
    var lastUpdated: Date = Date(),
    @Ignore
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