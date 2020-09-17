package com.muzic.aplay.ui.fragments.source

import com.muzic.aplay.R
import com.muzic.aplay.ui.fragments.BottomNavigationFragmentParent

class SourceFragment: BottomNavigationFragmentParent(R.layout.radio_fragment, "A source") {
    companion object {
        fun newInstance() = SourceFragment()
    }
}