package com.muzic.aplay.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.muzic.aplay.R
import com.muzic.aplay.viewmodels.FileManagerViewModel
import com.muzic.aplay.viewmodels.TitleViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private val viewModel: FileManagerViewModel by viewModel()
    private val titleViewModel: TitleViewModel by viewModel()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.findFragmentById(R.id.navHostFragment).let {
            it as NavHostFragment
            navController = it.navController
        }

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.player_page -> navController.navigate(R.id.audioListFragment)
                R.id.podcast_page -> navController.navigate(R.id.podcastFragment)
                R.id.radio_page -> navController.navigate(R.id.radioFragment)
                R.id.source_page -> navController.navigate(R.id.sourceFragment)
            }
            true
        }

    }

}
