package com.muzic.aplay.ui.fragments.audiolist

import android.app.Application
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.muzic.aplay.databinding.AudioListFragmentBinding
import com.muzic.aplay.db.AudioRepository
import com.muzic.aplay.viewmodels.TitleViewModel
import com.squareup.moshi.JsonClass
import timber.log.Timber


class AudioListFragment : Fragment() {

    private var audioListBinding: AudioListFragmentBinding? = null
    private val titleViewModel: TitleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { queryForMusic(it.application) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = AudioListFragmentBinding.inflate(inflater, container, false)

        audioListBinding = binding

        val adapter = AudioListAdapter(layoutInflater) {
            Timber.i(it.id)
        }
        binding.items.apply {
            setAdapter(adapter)
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(
                    activity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
        adapter.submitList(AudioRepository.items)

        titleViewModel.setTopAppBarTitle("A player")

        return binding.root
    }

    fun queryForMusic(application: Application) {
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.ARTIST, // 0
            MediaStore.Audio.AudioColumns.YEAR, // 1
            MediaStore.Audio.AudioColumns.TRACK, // 2
            MediaStore.Audio.AudioColumns.TITLE, // 3
            MediaStore.Audio.AudioColumns.DISPLAY_NAME, // 4,
            MediaStore.Audio.AudioColumns.DURATION, //5,
            MediaStore.Audio.AudioColumns.ALBUM, // 6
            MediaStore.Audio.AudioColumns.ALBUM_ID, // 7
            MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME, // 8
            MediaStore.Audio.AudioColumns._ID, // 9
            MediaStore.MediaColumns.DATE_MODIFIED // 10
        )

        val selection = "${MediaStore.Audio.AudioColumns.IS_MUSIC} = 1"
        val sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER

        val musicCursor = application.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, sortOrder)

        val list = mutableListOf<Music>()

        // Query the storage for music files
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
            while (cursor.moveToNext()) {
                // Now loop through the music files
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


                list.add(
                    Music(
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
                        "0",
                        0,
                        audioDateAdded
                    )
                )

                // Add the current music to the list
//                    mDeviceMusicList.add(
//                        Music(
//                            audioArtist,
//                            audioYear,
//                            audioTrack,
//                            audioTitle,
//                            audioDisplayName,
//                            audioDuration,
//                            audioAlbum,
//                            albumId,
//                            audioFolderName,
//                            audioId,
//                            GoConstants.ARTIST_VIEW,
//                            0,
//                            audioDateAdded
//                        )
//                    )
            }
        }
        list.forEach {
            Timber.i(it.toString())
        }
    }

    override fun onDestroyView() {
        audioListBinding = null
        super.onDestroyView()
    }

}

@JsonClass(generateAdapter = true)
data class Music(
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
    val launchedBy: String,
    val startFrom: Int,
    val dateAdded: Int,
)
