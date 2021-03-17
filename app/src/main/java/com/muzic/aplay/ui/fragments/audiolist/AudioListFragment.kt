package com.muzic.aplay.ui.fragments.audiolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.muzic.aplay.databinding.AudioListFragmentBinding
import com.muzic.aplay.ui.setTopTitle
import com.muzic.aplay.viewmodels.MusicViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class AudioListFragment : Fragment() {

    private var audioListBinding: AudioListFragmentBinding? = null
    private val musicViewModel: MusicViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = AudioListFragmentBinding.inflate(inflater, container, false)

        audioListBinding = binding

        val adapter = AudioListAdapter(layoutInflater) {
            Timber.i(it.id.toString())
        }
        binding.items.apply {
            setAdapter(adapter)
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(
                    activity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        musicViewModel.audio.observe(viewLifecycleOwner) { list -> adapter.submitList(list) }
        activity?.let {
            it.setTopTitle("A player")
            musicViewModel.queryForMusic(it.application)
        }

        return binding.root
    }

    override fun onDestroyView() {
        audioListBinding = null
        super.onDestroyView()
    }

}

