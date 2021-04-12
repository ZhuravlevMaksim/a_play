package com.muzic.aplay.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Gravity
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.muzic.aplay.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
import com.muzic.aplay.PlayerService
import com.muzic.aplay.R
import com.muzic.aplay.databinding.ActivityMainBinding
import com.muzic.aplay.databinding.PlayerControlsBinding
import com.muzic.aplay.permissions
import timber.log.Timber

class MainActivity : AppCompatActivity(), Navigate {

    private var binding: ActivityMainBinding? = null
    private lateinit var navController: NavController

    private var playerServiceBinder: PlayerService.PlayerServiceBinder? = null
    private var mediaController: MediaControllerCompat? = null
    private var callback: MediaControllerCompat.Callback? = null
    private var serviceConnection: ServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = permissions()

        if (permissions) {
            init()

            callback = object : MediaControllerCompat.Callback() {
                override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                    Timber.i(state?.state.toString())
                    when (state?.state) {
                        PlaybackStateCompat.STATE_NONE,
                        PlaybackStateCompat.STATE_STOPPED,
                        PlaybackStateCompat.STATE_PAUSED -> binding?.playPauseButton?.setImage(this@MainActivity, R.drawable.ic_baseline_play_arrow_24)
                        PlaybackStateCompat.STATE_PLAYING -> binding?.playPauseButton?.setImage(
                            this@MainActivity,
                            R.drawable.ic_baseline_pause_24
                        )
                    }
                }
            }

            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    playerServiceBinder = service as PlayerService.PlayerServiceBinder
                    try {
                        mediaController = MediaControllerCompat(this@MainActivity, playerServiceBinder!!.mediaSessionToken)
                        mediaController?.registerCallback(callback!!)
                        callback?.onPlaybackStateChanged(mediaController?.playbackState)
                    } catch (e: RemoteException) {
                        mediaController = null
                    }
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    playerServiceBinder = null
                    mediaController?.unregisterCallback(callback!!)
                    mediaController = null
                }
            }

            bindService(Intent(this, PlayerService::class.java), serviceConnection!!, BIND_AUTO_CREATE)

            binding?.playPauseButton?.setOnClickListener {
                when (mediaController?.playbackState?.state) {
                    PlaybackStateCompat.STATE_NONE,
                    PlaybackStateCompat.STATE_STOPPED,
                    PlaybackStateCompat.STATE_PAUSED -> mediaController?.transportControls?.play()
                    PlaybackStateCompat.STATE_PLAYING -> mediaController?.transportControls?.pause()
                    else -> mediaController?.transportControls?.play()
                }
            }
        } else {
            setContentView(R.layout.no_permissions)
        }
    }

    private fun init() {
        if (binding == null) {
            binding = ActivityMainBinding.inflate(layoutInflater)
        }

        binding?.let { binding ->
            setContentView(binding.root)

            supportFragmentManager.findFragmentById(R.id.navHostFragment).let {
                (it as NavHostFragment).navController.also { controller -> navController = controller }
            }

            binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.player_page -> navController.navigate(R.id.audioListFragment)
                    R.id.source_page -> navController.navigate(R.id.sourceFragment)
                }
                true
            }

            handleIntent(intent)
        }

        binding?.let {
            with(it.playingSongContainer) {
                setOnClickListener {
                    MaterialDialog(this@MainActivity, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                        customView(R.layout.player_controls)

                        val playerControls = PlayerControlsBinding.bind(getCustomView())

                        playerControls.playbackSpeed.setOnClickListener { view ->
                            PopupMenu(this@MainActivity, view).apply {
                                inflate(R.menu.popup_speed)
                                gravity = Gravity.END

                                setOnMenuItemClickListener { menuItem ->
                                    val speed = when (menuItem.itemId) {
                                        R.id.speed_1 -> 1.00F
                                        R.id.speed_1_5 -> 1.5F
                                        R.id.speed_2 -> 2.0F
                                        else -> 1.0F
                                    }
                                    return@setOnMenuItemClickListener true
                                }
                                show()
                            }
                        }
                    }
                }
                setOnLongClickListener {
                    TODO()
                }
            }
        }
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_READ_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    init()
                }
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            it.getStringExtra(Intent.EXTRA_TEXT)?.let {
                binding!!.bottomNavigation.selectedItemId = R.id.source_page
                navController.navigate(R.id.sourceFragment, Bundle().apply {
                    this.putString("url", it)
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun navigate(fragment: Int, bundle: Bundle) {
        navController.navigate(fragment, bundle)
    }
}

interface Navigate {
    fun navigate(fragment: Int, bundle: Bundle)
}

fun FragmentActivity.navigate(fragment: Int, bundle: Bundle) = (this as MainActivity).navigate(fragment, bundle)

fun ImageButton.setImage(context: Context, icon: Int) = setImageDrawable(ContextCompat.getDrawable(context, icon))