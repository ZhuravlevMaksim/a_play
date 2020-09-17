package com.muzic.aplay.ui.fragments.radio

import android.os.Bundle
import android.view.View
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.muzic.aplay.R
import com.muzic.aplay.ui.fragments.BottomNavigationFragmentParent
import kotlinx.android.synthetic.main.audio_list_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

//todo: помоему можно сделать эффективнее, постоянно стоздается фрагмент и не переиспользуется модель
class RadioFragment: BottomNavigationFragmentParent(R.layout.radio_fragment, "A radio") {

    companion object {
        fun newInstance() = RadioFragment()
    }

    private lateinit var radioAdapter: RadioListAdapter
    private val radioViewModel: RadioViewModel by viewModel()

    private val itsAdressWWWLocal = "json/stations/bycountryexact/internet?order=clickcount&reverse=true"
    private val itsAdressWWWTopClick = "json/stations/topclick/100"
    private val itsAdressWWWTopVote = "json/stations/topvote/100"
    private val itsAdressWWWChangedLately = "json/stations/lastchange/100"
    private val itsAdressWWWCurrentlyHeard = "json/stations/lastclick/100"
    private val itsAdressWWWTags = "json/tags"
    private val itsAdressWWWCountries = "json/countrycodes"
    private val itsAdressWWWLanguages = "json/languages"

    override fun afterActivityCreated(savedInstanceState: Bundle?) {
        super.afterActivityCreated(savedInstanceState)
        radioViewModel.stations.observe(this) {
//            it.forEach { radioStation -> Timber.i(radioStation.toString()) }
            radioAdapter.submitList(it.toMutableList())
        }
        radioViewModel.request(itsAdressWWWTopVote)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioAdapter = RadioListAdapter(layoutInflater) {
            Timber.i(it.name)
        }
        items.apply {
            adapter = radioAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(
                    activity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

    }
}