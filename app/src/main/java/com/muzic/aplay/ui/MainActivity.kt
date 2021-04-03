package com.muzic.aplay.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.muzic.aplay.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
import com.muzic.aplay.R
import com.muzic.aplay.databinding.ActivityMainBinding
import com.muzic.aplay.databinding.PlayerControlsBinding
import com.muzic.aplay.permissions

class MainActivity : AppCompatActivity(), Navigate {

    private var binding: ActivityMainBinding? = null
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = permissions()

        if (permissions) {
            init()
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
//                    R.id.podcast_page -> navController.navigate(R.id.podcastFragment)
//                    R.id.radio_page -> navController.navigate(R.id.radioFragment)
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
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
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