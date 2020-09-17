package com.muzic.aplay.ui.fragments.radio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.muzic.aplay.databinding.RadioRowBinding

class RadioListAdapter(private val inflater: LayoutInflater, private val onClick: (RadioStation) -> Unit) :
    ListAdapter<RadioStation, RadioStationRowHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RadioStationRowHolder(RadioRowBinding.inflate(inflater, parent, false), onClick)

    override fun onBindViewHolder(holder: RadioStationRowHolder, position: Int) = holder.bind(getItem(position))

}

private object DiffCallback : DiffUtil.ItemCallback<RadioStation>() {
    override fun areItemsTheSame(oldItem: RadioStation, newItem: RadioStation) = oldItem.name == newItem.name
    override fun areContentsTheSame(oldItem: RadioStation, newItem: RadioStation) = oldItem.name == newItem.name
}