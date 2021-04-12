package com.muzic.aplay.db

import android.app.Application
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.muzic.aplay.model.Audio

class AudioRepository(private val application: Application) {

    private val mAudios = MutableLiveData(queryForMusic())

    val audios: List<Audio> get() = mAudios.value ?: mutableListOf()

    val mCurrent: MutableLiveData<Audio> by lazy {
        MutableLiveData<Audio>()
    }

    val currentAudio: LiveData<Audio?> get() = mCurrent

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun queryForMusic(): List<Audio> {
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.YEAR,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.Audio.AudioColumns.MIME_TYPE,
            MediaStore.Audio.AudioColumns.SIZE
        )

        val list = mutableListOf<Audio>()
        var position = 0

        listOf(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).forEach {
            application.contentResolver.query(it, projection, null, null, null)?.use { cursor ->
                val yearIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.YEAR)
                val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
                val displayNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
                val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
                val relativePathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME)
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
                val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
                val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.MIME_TYPE)
                val sizeIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE)

                while (cursor.moveToNext()) {

                    val audioId = cursor.getLong(idIndex)
                    val audioYear = cursor.getInt(yearIndex)
                    val audioTitle = cursor.getString(titleIndex)
                    val audioDisplayName = cursor.getString(displayNameIndex)
                    val audioDuration = cursor.getLong(durationIndex)
                    val audioRelativePath = cursor.getString(relativePathIndex)
                    val audioDateAdded = cursor.getInt(dateAddedIndex)
                    val audioFolderName = audioRelativePath ?: "/"
                    val mimeType = cursor.getString(mimeTypeIndex)
                    val size = cursor.getDouble(sizeIndex)

                    list.add(
                        Audio(
                            audioYear,
                            audioTitle,
                            audioDisplayName,
                            audioDuration,
                            audioFolderName,
                            audioId,
                            audioDateAdded,
                            mimeType,
                            size,
                            position++,
                            it
                        )
                    )
                }
            }
        }

        return list
    }

    fun setCurrentNext() {
        mCurrent.value = mCurrent.value?.position?.let { mAudios.value?.getOrNull(it + 1) }
    }

    fun setCurrentPrevious() {
        mCurrent.value = mCurrent.value?.position?.let { mAudios.value?.getOrNull(it - 1) }
    }

    fun setCurrentIndex(currentWindowIndex: Int) {
        mCurrent.value = mCurrent.value?.position?.let { mAudios.value?.getOrNull(currentWindowIndex) }
    }

}