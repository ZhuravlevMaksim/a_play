package com.muzic.aplay.ui.fragments.source

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.muzic.aplay.databinding.StreamRowBinding
import com.muzic.aplay.db.YoutubeStream

class StreamListAdapter(private val inflater: LayoutInflater, private val onClick: (YoutubeStream) -> Unit) : ListAdapter<YoutubeStream, StreamListAdapter.Holder>(DiffCallback) {

    class Holder(private val binding: StreamRowBinding, val onClick: (YoutubeStream) -> Unit): RecyclerView.ViewHolder(binding.root) {
        fun bind(stream: YoutubeStream) {
            binding.holder = this
            binding.stream = stream
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(StreamRowBinding.inflate(inflater, parent, false), onClick)

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

}

private object DiffCallback : DiffUtil.ItemCallback<YoutubeStream>() {
    override fun areItemsTheSame(oldItem: YoutubeStream, newItem: YoutubeStream) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: YoutubeStream, newItem: YoutubeStream) = oldItem.id == newItem.id
}