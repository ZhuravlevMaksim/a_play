package com.muzic.aplay.ui.fragments.audiolist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.muzic.aplay.databinding.AudioRowBinding
import com.muzic.aplay.db.AudioFile

class AudioListAdapter(private val inflater: LayoutInflater, private val onClick: (AudioFile) -> Unit) : ListAdapter<AudioFile, AudioRowHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AudioRowHolder(AudioRowBinding.inflate(inflater, parent, false), onClick)
    override fun onBindViewHolder(holder: AudioRowHolder, position: Int) = holder.bind(getItem(position))

}

private object DiffCallback : DiffUtil.ItemCallback<AudioFile>() {
    override fun areItemsTheSame(oldItem: AudioFile, newItem: AudioFile) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: AudioFile, newItem: AudioFile) = oldItem.id == newItem.id
}