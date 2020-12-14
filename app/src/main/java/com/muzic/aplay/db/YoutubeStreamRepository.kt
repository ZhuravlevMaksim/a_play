package com.muzic.aplay.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface YoutubeStreamRepository {

    @Query("SELECT * FROM youtube_stream_table ORDER BY id ASC")
    fun getAllData(): LiveData<List<YoutubeStream>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(stream: YoutubeStream)

}