package com.muzic.aplay.model

import com.squareup.moshi.JsonClass
import java.time.Instant

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
    val mimeType: String?,
    val size: Double?,
) {

    fun details(): String {
        val added = Instant.ofEpochSecond(dateAdded.toLong())

        return "${added}${mimeType}::${String.format("%.2f Mb", size?.let { it / 1024 / 1024 })}"
    }

}
