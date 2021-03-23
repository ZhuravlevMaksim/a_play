package com.muzic.aplay.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "youtube_stream_table")
data class YoutubeStream(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var uid: String,
    var url: String?,
    var title: String,
    var contentLength: String,
    var mimeType: String,
    var fileName: String,
    var update: Long = System.currentTimeMillis()
) {
    fun details(): String {
        return "${toRegex.find(mimeType)?.destructured?.component1()}::" + String.format("%.2f Mb", contentLength.toDouble() / 1024 / 1024)
    }
}

val toRegex = "\"(.+)\"".toRegex()
