package com.muzic.aplay

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val PROJECTION = arrayOf(
    MediaStore.Audio.Media.DISPLAY_NAME,
    MediaStore.Audio.Media.DATE_MODIFIED,
    MediaStore.Audio.Media.DATE_ADDED,
    MediaStore.Audio.Media.DURATION,
    MediaStore.Audio.Media.SIZE,
    MediaStore.Audio.Media.IS_MUSIC,
    MediaStore.Audio.Media.RELATIVE_PATH,
    MediaStore.Audio.Media.CONTENT_TYPE,
    MediaStore.Audio.Media.MIME_TYPE,
    MediaStore.Audio.Media.TITLE
)

class PlayFileManager(private val context: Context) {


    suspend fun listAudio(uri: Uri): List<AudioFile>? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        Log.i("TAG", uri.toString())

        doc(uri)

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
        Log.i("TAG", childrenUri.toString())

        val query = resolver.query(childrenUri, PROJECTION, null, null, null)
        query?.use { cursor ->
            cursor.mapToAudioList { it.extractAudio() }
        }
    }

    private fun <T : Any> Cursor.mapToAudioList(predicate: (Cursor) -> T): List<T> =
        generateSequence {
            while (moveToNext()) {
//                if (this.getString(8).contains("audio")) {
                return@generateSequence predicate(this)
//                } else continue
            }
            null
        }.toList()

    private fun Cursor.extractAudio(): AudioFile = AudioFile(
        this.getString(0),
        this.getString(1),
        this.getString(2),
        this.getString(3),
        this.getString(4),
        this.getString(5),
        this.getString(6),
        this.getString(7),
        this.getString(8),
        this.getString(9)
    )

    data class AudioFile(
        val displayName: String,
        val dateModified: String?,
        val dateAdded: String?,
        val duration: String?,
        val size: String?,
        val isMusic: String?,
        val relativePath: String?,
        val contentType: String?,
        val mimeType: String,
        val title: String?
    )

    private fun doc(uri: Uri) {
        val fromTreeUri = DocumentFile.fromTreeUri(context, uri)

        if (fromTreeUri!!.isDirectory) {
            fromTreeUri.listFiles().forEach { documentFile ->
                Log.i("TAG DIR", documentFile.uri.path!!)
                doc(documentFile.uri)
            }
        }

        Log.i("TAG FILE", "${fromTreeUri.name!!} - ${fromTreeUri.uri.path!!}")

    }
}
