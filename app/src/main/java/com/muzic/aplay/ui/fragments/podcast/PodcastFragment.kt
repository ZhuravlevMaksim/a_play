package com.muzic.aplay.ui.fragments.podcast

import com.muzic.aplay.R
import com.muzic.aplay.ui.fragments.BottomNavigationFragmentParent

class PodcastFragment: BottomNavigationFragmentParent(R.layout.radio_fragment, "A podcast") {
    companion object {
        fun newInstance() = PodcastFragment()
    }
}