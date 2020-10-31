package com.muzic.common.source

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.annotation.IntDef
import com.chopper.services.AudioStreamInfo
import com.chopper.services.YoutubeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class YoutubeMediaSource : MediaSource() {

    private var catalog: List<String> = emptyList()

    override fun playableList(): List<String> {
        return catalog
    }

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    suspend fun load(url: String) {
        updateCatalog(url)?.let { updatedCatalog ->
            catalog = updatedCatalog
            state = STATE_INITIALIZED
        } ?: run {
            catalog = emptyList()
            state = STATE_ERROR
        }
    }

    private suspend fun updateCatalog(url: String): List<String>? {
        return withContext(Dispatchers.IO) {
            try {
                listOf<String>(YoutubeService().getAudioStream(url).audioStream.url)
            } catch (ioException: IOException) {
                return@withContext emptyList()
            }
        }
    }

}

@IntDef(
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
)
@Retention(AnnotationRetention.SOURCE)
annotation class State

const val STATE_CREATED = 1
const val STATE_INITIALIZING = 2
const val STATE_INITIALIZED = 3
const val STATE_ERROR = 4


fun MediaMetadataCompat.Builder.from(stream: AudioStreamInfo): MediaMetadataCompat.Builder {

    putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, stream.audioStream.url)

//    val durationMs = TimeUnit.SECONDS.toMillis(stream.duration)

//    putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, stream.id)
//    putString(MediaMetadataCompat.METADATA_KEY_TITLE, stream.title)
//    putString(MediaMetadataCompat.METADATA_KEY_ARTIST, stream.artist)
//    putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)
//    putString(MediaMetadataCompat.METADATA_KEY_ALBUM, stream.album)
//    putString(MediaMetadataCompat.METADATA_KEY_GENRE, stream.genre)
//    putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, stream.source)
//    putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, stream.image)

    putLong(META_KEY_FLAG, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE.toLong())

//    putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, stream.trackNumber)
//    putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, stream.totalTrackCount)
//
//    putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, stream.title)
//    putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, stream.artist)
//    putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, stream.album)
//    putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, stream.image)

    return this
}