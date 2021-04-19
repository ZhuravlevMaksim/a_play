package com.muzic.aplay.model

import android.content.ContentUris
import android.net.Uri
import com.squareup.moshi.JsonClass
import java.io.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@JsonClass(generateAdapter = true)
data class Audio(
    val title: String?,
    val duration: Long,
    val relativePath: String?,
    val id: Long,
    val dateAdded: Long,
    val mimeType: String?,
    val size: Double?,
    val store: Uri,
    val from: From
) : Serializable {

    val uri: Uri
        get() = when (from) {
            From.LOCAL -> ContentUris.withAppendedId(store, id)
            From.WEB -> store
        }

    fun details(): String = "${formatter.format(Instant.ofEpochSecond(dateAdded))}${mimeFormat}${sizeFormat}"

    private val mimeFormat: String get() = mimeType?.let { "::${it.split("/")[1]}" } ?: ""
    private val sizeFormat: String get() = size?.let { "::${String.format("%.2f Mb", it / 1024 / 1024)}" } ?: ""
}

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yy-MM-dd").withZone(ZoneId.systemDefault())

enum class From {
    LOCAL,
    WEB
}