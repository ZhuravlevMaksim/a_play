package com.muzic.aplay.db

object AudioRepository {
    var items: List<AudioFile> = generateSequence(AudioFile("0"), {
        AudioFile((it.id.toInt() + 1).toString())
    }).take(100).toList()
}