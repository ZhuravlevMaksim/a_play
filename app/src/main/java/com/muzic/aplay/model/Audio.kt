package com.muzic.aplay.model

import com.squareup.moshi.JsonClass
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
        return "${formatter.format(Instant.ofEpochSecond(dateAdded.toLong()))}${mimeFormat}${sizeFormat}"
    }

    private val mimeFormat: String get() = mimeType?.let { "::${it.split("/")[1]}" } ?: ""
    private val sizeFormat: String get() = "::${String.format("%.2f Mb", size?.let { it / 1024 / 1024 })}"
}

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yy-MM-dd")
    .withZone(ZoneId.systemDefault())