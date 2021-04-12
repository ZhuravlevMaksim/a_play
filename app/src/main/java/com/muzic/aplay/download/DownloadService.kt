package com.muzic.aplay.download

import android.app.Notification
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File


private const val JOB_ID = 1
private const val FOREGROUND_NOTIFICATION_ID = 1

class PlayDownloadService : DownloadService(FOREGROUND_NOTIFICATION_ID) {

    private lateinit var databaseProvider: ExoDatabaseProvider
    private lateinit var simpleCache: SimpleCache
    private lateinit var dataSourceFactory: DefaultHttpDataSourceFactory
    private lateinit var downloadManager: DownloadManager

    override fun getDownloadManager(): DownloadManager {
        databaseProvider = ExoDatabaseProvider(this)
        simpleCache = SimpleCache(File(this.filesDir, "cache"), NoOpCacheEvictor(), databaseProvider)
        dataSourceFactory = DefaultHttpDataSourceFactory()
        val downloadExecutor = Runnable::run
        downloadManager = DownloadManager(this, databaseProvider, simpleCache, dataSourceFactory, downloadExecutor)
        downloadManager.maxParallelDownloads = 3
        return downloadManager
    }

    override fun getScheduler(): Scheduler {
        return PlatformScheduler(this, JOB_ID)
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification {
        TODO("Not yet implemented")
    }
}