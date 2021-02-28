package com.muzic.aplay.ui.fragments.podcast

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.muzic.aplay.R
import com.muzic.aplay.ui.setTopAppBarTitle
import org.koin.android.ext.android.inject
import timber.log.Timber

class PodcastFragment : Fragment() {

    private val TAG = javaClass.simpleName
    private val podcastRepo by inject<PodcastRepo>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.podcast_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTopAppBarTitle("A Podcast")

        performSearch("Android Developer")

    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY) ?: return
            performSearch(query)
        }
    }

    private fun performSearch(term: String) {
        val itunesService = ItunesService.instance
        val itunesRepo = ItunesRepo(itunesService)
        itunesRepo.searchByTerm(term) {
            Timber.i(TAG, "Results = $it")

            it?.forEach {
                podcastRepo.getPodcast(it.feedUrl) {
                    Timber.i(TAG, "Podcast = $it")
                }
            }
        }
    }
}