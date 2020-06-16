package com.muzic.aplay.ui.fragments.audiolist

import androidx.recyclerview.widget.RecyclerView
import com.muzic.aplay.databinding.AudioRowBinding
import com.muzic.aplay.db.AudioFile

class AudioRowHolder(private val binding: AudioRowBinding, val onClick: (AudioFile) -> Unit) : RecyclerView.ViewHolder(binding.root) {
    fun bind(audio: AudioFile) {
        binding.holder = this
        binding.audio = audio
        binding.executePendingBindings()
    }
}