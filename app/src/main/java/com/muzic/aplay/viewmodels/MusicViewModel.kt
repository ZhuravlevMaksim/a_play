package com.muzic.aplay.viewmodels

import android.app.Application
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.muzic.aplay.model.Audio

class MusicViewModel(private val application: Application) : ViewModel() {

    private val mutAudio: MutableLiveData<List<Audio>> by lazy {
        MutableLiveData<List<Audio>>()
    }

    val audio: LiveData<List<Audio>> get() = mutAudio

    public fun queryForMusicFromPath(path: String) {
        mutAudio.value = queryForMusic().filter { it.relativePath == path }
    }

    public fun queryForAllMusic() {
        mutAudio.value = queryForMusic()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun queryForMusic(): List<Audio> {
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.YEAR,
            MediaStore.Audio.AudioColumns.TRACK,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.Audio.AudioColumns.MIME_TYPE,
            MediaStore.Audio.AudioColumns.SIZE
        )

        val selection = "${MediaStore.Audio.AudioColumns.IS_MUSIC} = 1"
        val sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER

        val musicCursor = application.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, sortOrder)

        val list = mutableListOf<Audio>()

        musicCursor?.use { cursor ->
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
            val yearIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.YEAR)
            val trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TRACK)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
            val displayNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM)
            val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID)
            val relativePathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME)
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
            val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
            val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.MIME_TYPE)
            val sizeIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE)

            while (cursor.moveToNext()) {

                val audioId = cursor.getLong(idIndex)
                val audioArtist = cursor.getString(artistIndex)
                val audioYear = cursor.getInt(yearIndex)
                val audioTrack = cursor.getInt(trackIndex)
                val audioTitle = cursor.getString(titleIndex)
                val audioDisplayName = cursor.getString(displayNameIndex)
                val audioDuration = cursor.getLong(durationIndex)
                val audioAlbum = cursor.getString(albumIndex)
                val albumId = cursor.getLong(albumIdIndex)
                val audioRelativePath = cursor.getString(relativePathIndex)
                val audioDateAdded = cursor.getInt(dateAddedIndex)
                val audioFolderName = audioRelativePath ?: "/"
                val mimeType = cursor.getString(mimeTypeIndex)
                val size = cursor.getDouble(sizeIndex)

                list.add(
                    Audio(
                        audioArtist,
                        audioYear,
                        audioTrack,
                        audioTitle,
                        audioDisplayName,
                        audioDuration,
                        audioAlbum,
                        albumId,
                        audioFolderName,
                        audioId,
                        audioDateAdded,
                        mimeType,
                        size
                    )
                )
            }
        }

        return list
    }

}