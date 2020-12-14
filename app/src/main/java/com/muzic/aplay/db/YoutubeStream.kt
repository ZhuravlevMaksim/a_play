package com.muzic.aplay.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "youtube_stream_table")
data class YoutubeStream(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var url: String,
    var title: String,
    var contentLength: String,
    var mimeType: String,
    var fileName: String
)