package com.muzic.aplay.ui.fragments.source

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muzic.aplay.R
import com.muzic.aplay.ui.inflateMenu
import com.muzic.aplay.viewmodels.YoutubeViewModel
import kotlinx.android.synthetic.main.youtube_fragment.*
import kotlinx.android.synthetic.main.youtube_fragment.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SourceFragment : Fragment() {

    private val viewModel: YoutubeViewModel by viewModel()

    private val adapter: StreamListAdapter by lazy {
        StreamListAdapter(layoutInflater) {
            viewModel.download(it)
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
            addSwipe(it)
        }

        viewModel.getAllData.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        return view
    }

    private fun addSwipe(recyclerView: RecyclerView){
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.remove(adapter.currentList[viewHolder.adapterPosition])
            }
        }).attachToRecyclerView(recyclerView)
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