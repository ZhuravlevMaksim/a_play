package com.muzic.aplay.ui.fragments.audiolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.muzic.aplay.R
import com.muzic.aplay.db.AudioRepository
import com.muzic.aplay.ui.inflateMenu
import kotlinx.android.synthetic.main.audio_list_fragment.*
import timber.log.Timber


class AudioListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.audio_list_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        topAppBar.inflateMenu(R.menu.actions)
//        topAppBar.setOnMenuItemClickListener
        val adapter = AudioListAdapter(layoutInflater) {
            Timber.i(it.id)
        }
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inflateMenu("A Player", R.menu.player_menu)
    }

}