package com.muzic.common.source

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.muzic.common.PlayerService
import com.muzic.common.R

class NotificationManager(val playerService: PlayerService, val sessionToken: MediaSessionCompat.Token) {

    private var isForegroundService = false
    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(playerService, sessionToken)

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            playerService,
            "0",
            R.string.notification_channel,
            R.string.notification_channel_description,
            0xb339, // some notification unique id
            DescriptionAdapter(mediaController),
            PlayerNotificationListener()
        ).apply {

            setMediaSessionToken(sessionToken)
            setSmallIcon(R.drawable.ic_notification)

            setFastForwardIncrementMs(30)
        }
    }

    private inner class PlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
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
        TODO("Not yet implemented")
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        TODO("Not yet implemented")
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        TODO("Not yet implemented")
    }

    override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
        TODO("Not yet implemented")
    }


}
