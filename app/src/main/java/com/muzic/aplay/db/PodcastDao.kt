package com.muzic.aplay.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PodcastDao {

    @Query("SELECT * FROM Podcast ORDER BY FeedTitle")
    fun loadPodcasts(): LiveData<List<Podcast>>

    @Query("SELECT * FROM Podcast WHERE feedUrl = :url")
    fun loadPodcast(url: String): Podcast?

    @Query("SELECT * FROM Episode WHERE podcastId = :podcastId ORDER BY releaseDate DESC")
    fun loadEpisodes(podcastId: Long): List<Episode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPodcast(podcast: Podcast): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEpisode(episode: Episode): Long

    @Delete
    fun deletePodcast(podcast: Podcast)

    @Query("SELECT * FROM Podcast ORDER BY FeedTitle")
    fun loadPodcastsStatic(): List<Podcast>

}