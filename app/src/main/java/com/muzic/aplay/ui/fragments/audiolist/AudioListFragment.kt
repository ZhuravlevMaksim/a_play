package com.muzic.aplay.ui.fragments.audiolist

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.muzic.aplay.R
import com.muzic.aplay.db.AudioRepository
import com.muzic.aplay.ui.fragments.BottomNavigationFragmentParent
import kotlinx.android.synthetic.main.audio_list_fragment.*
import timber.log.Timber


class AudioListFragment : BottomNavigationFragmentParent(R.layout.audio_list_fragment, "A play") {

    companion object {
        fun newInstance() = AudioListFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        topAppBar.inflateMenu(R.menu.actions)
//        topAppBar.setOnMenuItemClickListener
        val adapter = AudioListAdapter(layoutInflater) {
            Timber.i(it.id)
        }
        adapter.submitList(AudioRepository.items)
        items.apply {
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
    }

}