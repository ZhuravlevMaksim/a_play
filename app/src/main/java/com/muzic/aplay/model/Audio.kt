package com.muzic.aplay.model

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.squareup.moshi.JsonClass
import java.io.Serializable
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
    val id: Long,
    val dateAdded: Int,
    val mimeType: String?,
    val size: Double?,
) : Serializable {

    val uri: Uri
        get() = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

    fun details(): String = "${formatter.format(Instant.ofEpochSecond(dateAdded.toLong()))}${mimeFormat}${sizeFormat}"

    private val mimeFormat: String get() = mimeType?.let { "::${it.split("/")[1]}" } ?: ""
    private val sizeFormat: String get() = size?.let { "::${String.format("%.2f Mb", it / 1024 / 1024)}" } ?: ""
}

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yy-MM-dd").withZone(ZoneId.systemDefault())