package com.muzic.aplay.db

import android.app.Application
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.muzic.aplay.model.Audio
import com.muzic.aplay.model.From

class AudioRepository(private val application: Application) {

    val audios = MutableLiveData(queryForMusic())
    val currentPlaying: MutableLiveData<Audio?> by lazy { MutableLiveData<Audio?>() }
    val currentPathAudios: MutableLiveData<List<Audio>> by lazy { MutableLiveData<List<Audio>>() }

    fun setPlaylist(playlist: List<Audio>) {
        currentPathAudios.value = playlist
    }

    fun setCurrentPlaying(index: Int?) {
        val audio = if (index != null) audios.value?.getOrNull(index) else null
        if (currentPlaying.value != audio) {
            currentPlaying.value = audio
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun queryForMusic(): List<Audio> {
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.Audio.AudioColumns.MIME_TYPE,
            MediaStore.Audio.AudioColumns.SIZE
        )

        val list = mutableListOf<Audio>()

        listOf(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).forEach {
            application.contentResolver.query(it, projection, null, null, null)?.use { cursor ->
                val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
                val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
                val relativePathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME)
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
                val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
                val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.MIME_TYPE)
                val sizeIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE)

                while (cursor.moveToNext()) {

                    val audioId = cursor.getLong(idIndex)
                    val audioTitle = cursor.getString(titleIndex)
                    val audioDuration = cursor.getLong(durationIndex)
                    val audioRelativePath = cursor.getString(relativePathIndex)
                    val audioDateAdded = cursor.getLong(dateAddedIndex)
                    val audioFolderName = audioRelativePath ?: "/"
                    val mimeType = cursor.getString(mimeTypeIndex)
                    val size = cursor.getDouble(sizeIndex)

                    list.add(
                        Audio(
                            audioTitle,
                            audioDuration,
                            audioFolderName,
                            audioId,
                            audioDateAdded,
                            mimeType,
                            size,
                            it,
                            From.LOCAL
                        )
                    )
                }
            }
        }

        return list
    }

}