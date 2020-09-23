package com.muzic.aplay.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.muzic.aplay.R
import com.muzic.aplay.ui.fragments.BottomNavigationFragmentParent
import com.muzic.aplay.ui.fragments.audiolist.AudioListFragment
import com.muzic.aplay.ui.fragments.podcast.PodcastFragment
import com.muzic.aplay.ui.fragments.radio.RadioFragment
import com.muzic.aplay.ui.fragments.source.SourceFragment
import com.muzic.aplay.viewmodels.FileManagerViewModel
import com.muzic.aplay.viewmodels.TitleViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.player_bottom_sheet.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private val viewModel: FileManagerViewModel by viewModel()
    private val titleViewModel: TitleViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(player_bottom_sheet)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehavior.isHideable = false
        fragmentContainer?.foreground?.alpha = 0
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

//                Log.i("TAG", fragmentContainer?.toString() ?: "")
//                fragmentContainer?.foreground?.alpha = 220
//                fragmentContainer?.background?.alpha = 220
            }
        })

        viewModel.states.observe(this) { state ->
            Log.i("TAG", state.toString())
        }

        titleViewModel.title.observe(this) { title ->
            topAppBar?.title = title
        }

        startFragment(AudioListFragment.newInstance())

//        openDirectory(Uri.EMPTY)
//        viewModel.list(Uri.parse("content://com.android.providers.downloads.documents/tree/downloads/document/downloads/children"))

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.player_page -> startFragment(AudioListFragment.newInstance())
                R.id.podcast_page -> startFragment(PodcastFragment.newInstance())
                R.id.radio_page -> startFragment(RadioFragment.newInstance())
                R.id.source_page -> startFragment(SourceFragment.newInstance())
                else -> false
            }
        }

        val badge = bottomNavigation.getOrCreateBadge(R.id.podcast_page)
        badge.isVisible = true
        badge.number = 99

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == READ_PERM && resultCode == Activity.RESULT_OK) {
            Timber.i("Get read permission on ${resultData?.data?.toString() ?: "null"}")
            viewModel.list(resultData?.data!!)
        }
    }

    fun openDirectory(pickerInitialUri: Uri) {
        // Choose a directory using the system's file picker.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            // Provide read access to files and sub-directories in the user-selected
            // directory.
            this.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(Intent.createChooser(intent, "Choose directory"), READ_PERM)
    }

    private fun startFragment(fragment: BottomNavigationFragmentParent): Boolean {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        return true
    }

}

const val READ_PERM = 45017
const val WRITE_PERM = 45018