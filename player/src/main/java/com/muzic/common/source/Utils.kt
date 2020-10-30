package com.muzic.common.source

data class Track (
    var id: String = "",
    var title: String = "",
    var album: String = "",
    var artist: String = "",
    var genre: String = "",
    var source: String = "",
    var image: String = "",
    var trackNumber: Long = 0,
    var totalTrackCount: Long = 0,
    var duration: Long = -1,
    var site: String = ""
)

const val META_KEY_FLAG = "MediaItem.FLAG"