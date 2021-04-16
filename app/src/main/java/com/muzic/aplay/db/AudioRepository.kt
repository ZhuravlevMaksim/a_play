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
    val audios: LiveData<List<Audio>> get() = mAudios

    private val mCurrent: MutableLiveData<Audio?> by lazy { MutableLiveData<Audio?>() }
    val current: LiveData<Audio?> get() = mCurrent

    private val currentPathAudios: MutableLiveData<List<Audio>> by lazy { MutableLiveData<List<Audio>>() }
    val pathAudios: LiveData<List<Audio>> get() = currentPathAudios

    fun setCurrentIndex(currentWindowIndex: Int) {
        mCurrent.value = mAudios.value?.getOrNull(currentWindowIndex)
    }

    fun setCurrentPath(path: String) {
        currentPathAudios.value = audios.value?.filter { it.relativePath == path }
    }

    fun setCurrent(audio: Audio) {
        mCurrent.value = audio
    }

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

}