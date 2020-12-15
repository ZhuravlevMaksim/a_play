package com.muzic.aplay.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface YoutubeStreamRepository {

    @Query("SELECT * FROM youtube_stream_table ORDER BY id ASC")
    fun getAllData(): LiveData<List<YoutubeStream>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(stream: YoutubeStream)

    @Delete
    fun delete(stream: YoutubeStream)

}