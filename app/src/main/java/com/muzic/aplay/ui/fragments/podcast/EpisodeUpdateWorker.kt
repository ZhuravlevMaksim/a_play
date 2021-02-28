package com.muzic.aplay.ui.fragments.podcast

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.muzic.aplay.db.YoutubeStreamDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

class EpisodeUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val EPISODE_CHANNEL_ID = "aplay_podcast_episodes_channel"
    }

    override suspend fun doWork(): Result = coroutineScope {
        val job = async {
            val db = YoutubeStreamDatabase.get(applicationContext)
            val repo = PodcastRepo(FeedService.instance, db.podcastDao())
            repo.updatePodcastEpisodes { podcastUpdates ->
                for (podcastUpdate in podcastUpdates) {
                    Timber.i("display notification")
                }
            }
        }
        job.await()
        Result.success()
    }

}