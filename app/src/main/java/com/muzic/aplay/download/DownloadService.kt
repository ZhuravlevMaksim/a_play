package com.muzic.aplay.download

import android.app.Notification
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Scheduler

private const val JOB_ID = 1
private const val FOREGROUND_NOTIFICATION_ID = 1

class PlayDownloadService: DownloadService(FOREGROUND_NOTIFICATION_ID) {
    override fun getDownloadManager(): DownloadManager {
        TODO("Not yet implemented")
    }

    override fun getScheduler(): Scheduler {
        return PlatformScheduler(this, JOB_ID)
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification {
        TODO("Not yet implemented")
    }
}