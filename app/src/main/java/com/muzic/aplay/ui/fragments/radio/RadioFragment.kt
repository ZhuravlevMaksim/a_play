package com.muzic.aplay.ui.fragments.radio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.muzic.aplay.databinding.RadioFragmentBinding
import com.muzic.aplay.ui.setTopTitle
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


//todo: постоянно стоздается фрагмент и не переиспользуется модель
class RadioFragment : Fragment() {

    private var radioBinding: RadioFragmentBinding? = null
    private val radioViewModel: RadioViewModel by viewModel()

    private val itsAdressWWWLocal = "json/stations/bycountryexact/internet?order=clickcount&reverse=true"
    private val itsAdressWWWTopClick = "json/stations/topclick/100"
    private val itsAdressWWWTopVote = "json/stations/topvote/100"
    private val itsAdressWWWChangedLately = "json/stations/lastchange/100"
    private val itsAdressWWWCurrentlyHeard = "json/stations/lastclick/100"
    private val itsAdressWWWTags = "json/tags"
    private val itsAdressWWWCountries = "json/countrycodes"
    private val itsAdressWWWLanguages = "json/languages"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = RadioFragmentBinding.inflate(inflater, container, false)

        radioBinding = binding

        binding.items.apply {
            adapter = RadioListAdapter(layoutInflater) {
                Timber.i(it.name)
            }
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(
                    activity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        //        radioViewModel.stations.observe(viewLifecycleOwner) {
//            radioAdapter.submitList(it.toMutableList())
//        }
//        radioViewModel.request(itsAdressWWWTopVote)
        activity?.setTopTitle("A Radio")

        return binding.root
    }

    override fun onDestroyView() {
        radioBinding = null
        super.onDestroyView()
    }

}