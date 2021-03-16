package com.muzic.aplay.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Audio(
    val artist: String?,
    val year: Int,
    val track: Int,
    val title: String?,
    val displayName: String?,
    val duration: Long,
    val album: String?,
    val albumId: Long?,
    val relativePath: String?,
    val id: Long?,
    val dateAdded: Int,
)
