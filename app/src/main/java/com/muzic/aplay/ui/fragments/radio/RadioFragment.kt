package com.muzic.aplay.ui.fragments.radio

import com.muzic.aplay.R
import com.muzic.aplay.ui.fragments.BottomNavigationFragmentParent

class RadioFragment: BottomNavigationFragmentParent(R.layout.radio_fragment, "A radio") {
    companion object {
        fun newInstance() = RadioFragment()
    }
}