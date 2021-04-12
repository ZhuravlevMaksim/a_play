package com.muzic.aplay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.muzic.aplay.db.AudioRepository
import com.muzic.aplay.model.Audio
import org.koin.android.ext.android.inject


class PlayerService : Service() {

    private val NOTIFICATION_ID = 0x723
    private val NOTIFICATION_DEFAULT_CHANNEL_ID = "a_play_notify_channel"

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioFocusRequest: AudioFocusRequest
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var audioManager: AudioManager
    private val audioRepository by inject<AudioRepository>()

    private var audioFocusRequested = false

    val metadataBuilder = MediaMetadataCompat.Builder()

    private val stateBuilder = PlaybackStateCompat.Builder().setActions(
        PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    )

    override fun onBind(intent: Intent?): IBinder {
        return PlayerServiceBinder()
    }

    inner class PlayerServiceBinder : Binder() {
        val mediaSessionToken: MediaSessionCompat.Token
            get() = mediaSession.sessionToken
    }

    override fun onCreate() {
        super.onCreate()

        val notificationChannel = NotificationChannel(NOTIFICATION_DEFAULT_CHANNEL_ID, "player con", IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        val audioAttributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> mediaSessionCallback.onPlay()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mediaSessionCallback.onPause()
                    else -> mediaSessionCallback.onPause()
                }
            }
            .setAcceptsDelayedFocusGain(false)
            .setWillPauseWhenDucked(true)
            .setAudioAttributes(audioAttributes)
            .build()


        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        mediaSession = MediaSessionCompat(this, "PlayerService").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setCallback(mediaSessionCallback)
            packageManager?.getLaunchIntentForPackage(packageName)?.let {
                PendingIntent.getActivity(this@PlayerService, 0, it, 0)
            }.run {
                setSessionActivity(this)
            }
            setMediaButtonReceiver(
                PendingIntent.getBroadcast(
                    applicationContext, 0,
                    Intent(Intent.ACTION_MEDIA_BUTTON, null, applicationContext, MediaButtonReceiver::class.java),
                    0
                )
            )
            isActive = true
        }

        exoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(DefaultTrackSelector(this))
            .setLoadControl(DefaultLoadControl()).build()
            .apply {
                this.addListener(exoPlayerListener)
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }
        exoPlayer.release()
    }

    private val mediaSessionCallback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {
        private var currentUri: Uri? = null
        var currentState = PlaybackStateCompat.STATE_STOPPED
        override fun onPlay() {
            if (!exoPlayer.playWhenReady) {
                startService(Intent(applicationContext, PlayerService::class.java))
                if (audioRepository.currentAudio == null) return
                val audio: Audio = audioRepository.currentAudio!!
                updateMetadata(audio)
                prepareToPlay(audio.uri)
                if (!audioFocusRequested) {
                    audioFocusRequested = true
                    val audioFocusResult: Int = audioManager.requestAudioFocus(audioFocusRequest)
                    if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
                }
                mediaSession.isActive = true
                registerReceiver(becomingNoisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
                exoPlayer.playWhenReady = true
            }
            mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f).build()
            )
            currentState = PlaybackStateCompat.STATE_PLAYING
            refreshNotificationAndForegroundStatus(currentState)
        }

        override fun onPause() {
            if (exoPlayer.playWhenReady) {
                exoPlayer.playWhenReady = false
                unregisterReceiver(becomingNoisyReceiver)
            }
            mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f).build()
            )
            currentState = PlaybackStateCompat.STATE_PAUSED
            refreshNotificationAndForegroundStatus(currentState)
        }

        override fun onStop() {
            if (exoPlayer.playWhenReady) {
                exoPlayer.playWhenReady = false
                unregisterReceiver(becomingNoisyReceiver)
            }
            if (audioFocusRequested) {
                audioFocusRequested = false
                audioManager.abandonAudioFocusRequest(audioFocusRequest)
            }
            mediaSession.isActive = false
            mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f).build()
            )
            currentState = PlaybackStateCompat.STATE_STOPPED
            refreshNotificationAndForegroundStatus(currentState)
            stopSelf()
        }

        override fun onSkipToNext() {
            updateMetadata(audioRepository.currentAudio!!)
            refreshNotificationAndForegroundStatus(currentState)
            prepareToPlay(audioRepository.currentAudio!!.uri)
        }

        override fun onSkipToPrevious() {
            updateMetadata(audioRepository.currentAudio!!)
            refreshNotificationAndForegroundStatus(currentState)
            prepareToPlay(audioRepository.currentAudio!!.uri)
        }

        private fun prepareToPlay(uri: Uri) {
            if (uri != currentUri) {
                currentUri = uri
                exoPlayer.addMediaItem(MediaItem.fromUri(uri))
                exoPlayer.prepare()
            }
        }

        private fun updateMetadata(music: Audio) {
//        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, )
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, music.title)
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, music.album)
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, music.artist)
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, music.duration)
            mediaSession.setMetadata(metadataBuilder.build())
        }

    }

    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                mediaSessionCallback.onPause()
            }
        }
    }

    private fun refreshNotificationAndForegroundStatus(playbackState: Int) {
        when (playbackState) {
            PlaybackStateCompat.STATE_PLAYING -> {
                startForeground(NOTIFICATION_ID, getNotification(playbackState))
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                NotificationManagerCompat.from(this@PlayerService).notify(NOTIFICATION_ID, getNotification(playbackState))
                stopForeground(false)
            }
            else -> {
                stopForeground(true)
            }
        }
    }

    private val exoPlayerListener: Player.EventListener = object : Player.EventListener {
        override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {}
        override fun onLoadingChanged(isLoading: Boolean) {}
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playWhenReady && playbackState == ExoPlayer.STATE_ENDED) {
                mediaSessionCallback.onSkipToNext()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
//            updateUiForPlayingMediaItem(mediaItem)
        }

        override fun onPlayerError(error: ExoPlaybackException) {}
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
    }

    private fun getNotification(playbackState: Int): Notification {
        val builder: NotificationCompat.Builder = from(this, mediaSession)
        builder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_previous,
                getString(R.string.previous),
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            )
        )
        if (playbackState == PlaybackStateCompat.STATE_PLAYING) builder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                getString(R.string.pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)
            )
        ) else builder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                getString(R.string.play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)
            )
        )
        builder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_next,
                getString(R.string.next),
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
            )
        )
        builder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
                .setMediaSession(mediaSession.sessionToken)
        )
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.color = ContextCompat.getColor(this, R.color.colorPrimaryDark) // The whole background (in MediaStyle), not just icon background
        builder.setShowWhen(false)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setOnlyAlertOnce(true)
        builder.setChannelId(NOTIFICATION_DEFAULT_CHANNEL_ID)
        return builder.build()
    }
}

fun from(context: Context?, mediaSession: MediaSessionCompat): NotificationCompat.Builder {
    val controller = mediaSession.controller
    val mediaMetadata = controller.metadata
    val description = mediaMetadata.description
    val builder = NotificationCompat.Builder(context)
    builder
        .setContentTitle(description.title)
        .setContentText(description.subtitle)
        .setSubText(description.description)
        .setLargeIcon(description.iconBitmap)
        .setContentIntent(controller.sessionActivity)
        .setDeleteIntent(
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)
        )
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    return builder
}