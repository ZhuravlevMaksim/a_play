package com.muzic.aplay.db

import android.text.format.DateUtils
import android.widget.TextView
import androidx.databinding.BindingAdapter

data class AudioFile(
    val id: String,
    val date: Long = System.currentTimeMillis()
)

@BindingAdapter("formattedDate")
fun TextView.formattedDate(date: Long?) {
    date?.let {
        text = DateUtils.getRelativeDateTimeString(
            context,
            date,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.WEEK_IN_MILLIS,
            0x02000
        )
    }
}

val toRegex = "\"(.+)\"".toRegex()

@BindingAdapter("streamInfo")
fun TextView.streamInfo(stream: YoutubeStream?) {
    stream?.let {
        text = "${toRegex.find(stream.mimeType)?.destructured?.component1()}::" + String.format("%.2f Mb", stream.contentLength.toDouble() / 1024 / 1024)
    }
}
