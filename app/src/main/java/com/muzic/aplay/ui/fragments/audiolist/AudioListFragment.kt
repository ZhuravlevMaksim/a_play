package com.muzic.aplay.ui.fragments.audiolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.muzic.aplay.R
import com.muzic.aplay.databinding.AudioListFragmentBinding
import com.muzic.aplay.db.AudioRepository
import com.muzic.aplay.ui.inflateMenu
import timber.log.Timber


class AudioListFragment : Fragment() {

    private var audioListBinding: AudioListFragmentBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = AudioListFragmentBinding.inflate(inflater, container, false)

        audioListBinding = binding

        val adapter = AudioListAdapter(layoutInflater) {
            Timber.i(it.id)
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
        adapter.submitList(AudioRepository.items)

        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inflateMenu("A Player", R.menu.player_menu)
    }

    override fun onDestroyView() {
        audioListBinding = null
        super.onDestroyView()
    }

}