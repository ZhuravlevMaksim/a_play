package com.muzic.aplay.ui.fragments.audiolist

import android.view.View
import android.widget.TextView
import com.afollestad.recyclical.ViewHolder
import com.muzic.aplay.R


class AudioViewRow(itemView: View): ViewHolder(itemView) {
    val title: TextView = itemView.findViewById(R.id.title)
    val description: TextView = itemView.findViewById(R.id.description)
}
