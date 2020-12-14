package com.muzic.aplay.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
            (it as NavHostFragment).navController.also { controller -> navController = controller }
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

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                bottomNavigation.selectedItemId = R.id.source_page
                navController.navigate(R.id.sourceFragment, Bundle().apply {
                    this.putString("url", it)
                })
            }
        }
    }
}

fun Fragment.setTopAppBarTitle(title: String) {
    activity?.topAppBar?.title = title
}

fun Fragment.inflateMenu(title: String, menu: Int) {
    activity?.topAppBar?.title = title
    activity?.topAppBar?.inflateMenu(menu)
}

fun Fragment.inflateMenu(menu: Int) {
    activity?.topAppBar?.inflateMenu(menu)
}
