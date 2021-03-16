package com.muzic.aplay.ui.fragments.audiolist

import androidx.recyclerview.widget.RecyclerView
import com.muzic.aplay.databinding.AudioRowBinding
import com.muzic.aplay.model.Audio

class AudioRowHolder(private val binding: AudioRowBinding, val onClick: (Audio) -> Unit) : RecyclerView.ViewHolder(binding.root) {
    fun bind(audio: Audio) {
        binding.holder = this
        binding.audio = audio
        binding.executePendingBindings()
    }
}