package com.muzic.aplay.ui.fragments.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.muzic.aplay.R
import kotlinx.android.synthetic.main.player_small.view.*

class PlayerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.player_small, container, false)


        var playing = true
        view.buttonPlay.setOnClickListener {
            it as ImageView
            if (playing) {
                it.setImageResource(R.drawable.ic_baseline_pause_24)
            } else {
                it.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
            playing = !playing
        }

        return view
    }
}