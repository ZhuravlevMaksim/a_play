package com.muzic.aplay.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.muzic.aplay.viewmodels.TitleViewModel
import timber.log.Timber

open class BottomNavigationFragmentParent(private val resource: Int, private val title: String) : Fragment() {

    private val titleViewModel: TitleViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(resource, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.run {
            titleViewModel.title.value = this@BottomNavigationFragmentParent.title
        }
        afterActivityCreated(savedInstanceState)
    }

    open fun afterActivityCreated(savedInstanceState: Bundle?) {
        Timber.tag("TAG").i("afterActivityCreated ${this::class.java}")
    }
}