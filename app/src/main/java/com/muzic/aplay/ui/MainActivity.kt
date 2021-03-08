package com.muzic.aplay.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.muzic.aplay.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
import com.muzic.aplay.R
import com.muzic.aplay.databinding.ActivityMainBinding
import com.muzic.aplay.permissions
import com.muzic.aplay.viewmodels.FileManagerViewModel
import com.muzic.aplay.viewmodels.TitleViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val fileViewModel: FileManagerViewModel by viewModel()
    private val titleViewModel: TitleViewModel by viewModel()
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val permissions = permissions()

        if (permissions) {
            initFragments()
        } else {
            setContentView(R.layout.no_permissions)
        }
    }

    private fun initFragments() {
        setContentView(binding.root)
        titleViewModel.title.observe(this) {
            binding.topAppBar.title = it
        }

        supportFragmentManager.findFragmentById(R.id.navHostFragment).let {
            (it as NavHostFragment).navController.also { controller -> navController = controller }
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.player_page -> navController.navigate(R.id.audioListFragment)
                R.id.podcast_page -> navController.navigate(R.id.podcastFragment)
                R.id.radio_page -> navController.navigate(R.id.radioFragment)
                R.id.source_page -> navController.navigate(R.id.sourceFragment)
            }
            true
        }

        handleIntent(intent)
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
                   initFragments()
                }
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                binding.bottomNavigation.selectedItemId = R.id.source_page
                navController.navigate(R.id.sourceFragment, Bundle().apply {
                    this.putString("url", it)
                })
            }
        }
    }
}