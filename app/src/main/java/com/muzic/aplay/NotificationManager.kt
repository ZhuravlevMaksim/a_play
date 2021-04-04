package com.muzic.aplay

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.DefaultControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager


const val CHANNEL = "aplay.NOW_PLAYING"
const val NOTIFICATION_ID = 0xb723

class NotificationManager(val playerService: PlayerService) {

    private var isForegroundService = false
    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(playerService, playerService.getMediaSessionToken())

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            playerService,
            CHANNEL,
            R.string.notification_channel, R.string.notification_channel_description,
            NOTIFICATION_ID, DescriptionAdapter(mediaController), PlayerNotificationListener()
        ).apply {

            setMediaSessionToken(playerService.getMediaSessionToken())
            setSmallIcon(R.drawable.ic_notification)
            setUseNavigationActions(true)
            setUseNavigationActionsInCompactView(false)

            setControlDispatcher(DefaultControlDispatcher(30_000, 10_000))
        }
    }

    fun setPlayer(player: Player) {
        notificationManager.setPlayer(player)
    }

    private inner class PlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    playerService,
                    Intent(playerService, playerService.javaClass)
                )

                playerService.startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            playerService.stopForeground(true)
            isForegroundService = false
            playerService.stopSelf()
        }
    }
}

class DescriptionAdapter(private val controller: MediaControllerCompat) : PlayerNotificationManager.MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): CharSequence {
        return "controller.metadata.description.title.toString()"
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return controller.sessionActivity
    }

    override fun getCurrentContentText(player: Player): CharSequence {
        return "controller.metadata.description.subtitle.toString()"
    }

    override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
        return null
    }

}
