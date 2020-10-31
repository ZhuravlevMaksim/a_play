package com.muzic.common

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.DefaultControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class NotificationManager(val playerService: PlayerService, private val sessionToken: MediaSessionCompat.Token) {

    private var isForegroundService = false
    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(playerService, sessionToken)

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            playerService,
            "0",
            R.string.notification_channel,
            R.string.notification_channel_description,
            0xb339, DescriptionAdapter(mediaController), PlayerNotificationListener()
        ).apply {

            setMediaSessionToken(sessionToken)
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

class DescriptionAdapter(mediaController: MediaControllerCompat) :
    PlayerNotificationManager.MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): CharSequence {
        return "Title"
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return null
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return "Title"
    }

    override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
        return null
    }

}
