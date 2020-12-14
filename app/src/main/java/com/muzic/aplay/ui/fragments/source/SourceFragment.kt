package com.muzic.aplay.ui.fragments.source

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.muzic.aplay.R
import com.muzic.aplay.ui.inflateMenu
import com.muzic.aplay.viewmodels.YoutubeViewModel
import kotlinx.android.synthetic.main.youtube_fragment.*
import kotlinx.android.synthetic.main.youtube_fragment.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SourceFragment : Fragment() {

    private val viewModel: YoutubeViewModel by viewModel()
    private val adapter: StreamListAdapter by lazy {
        StreamListAdapter(layoutInflater) {
            Timber.i(it.toString())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.youtube_fragment, container, false)

        view.recyclerView?.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(requireActivity())
            it.addItemDecoration(
                DividerItemDecoration(
                    activity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        viewModel.getAllData.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        inflateMenu("A Source", R.menu.source_menu)

        arguments?.getString("url")?.let {
            urlInput.setText(it)
            viewModel.getStreamFromUrl(it)
        }
    }

}