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
import timber.log.Timber

class PodcastFragment : Fragment() {

    val TAG = javaClass.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.podcast_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTopAppBarTitle("A Podcast")

        val itunesService = ItunesService.instance
        val itunesRepo = ItunesRepo(itunesService)
        itunesRepo.searchByTerm("Android Developer") {
            Timber.i(TAG, "Results = $it")
        }
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
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        override fun onCreateOptionsMenu(menu: Menu): Boolean {
//
//            val inflater = menuInflater
//            inflater.inflate(R.menu.menu_search, menu)
//            val searchMenuItem = menu.findItem(R.id.search_item)
//            val searchView = searchMenuItem?.actionView as SearchView
//
//            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
//
//            searchView.setSearchableInfo(
//                searchManager.getSearchableInfo(componentName)
//            )
//            return true
//        }
//    }


}