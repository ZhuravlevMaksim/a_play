package com.muzic.aplay.ui.fragments.radio

import androidx.recyclerview.widget.RecyclerView
import com.muzic.aplay.databinding.RadioRowBinding


class RadioStationRowHolder(private val binding: RadioRowBinding, val onClick: (RadioStation) -> Unit) : RecyclerView.ViewHolder(binding.root) {
    fun bind(radio: RadioStation) {
        binding.holder = this
        binding.radio = radio
        binding.executePendingBindings()
    }
}