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
import com.muzic.aplay.databinding.YoutubeFragmentBinding
import com.muzic.aplay.ui.setTopTitle
import com.muzic.aplay.viewmodels.YoutubeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class SourceFragment : Fragment() {

    private var sourceBinding: YoutubeFragmentBinding? = null
    private val viewModel: YoutubeViewModel by viewModel()

    private val adapter: StreamListAdapter by lazy {
        StreamListAdapter(layoutInflater) {
            viewModel.download(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = YoutubeFragmentBinding.inflate(inflater, container, false)

        sourceBinding = binding

        binding.recyclerView.let {
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

        activity?.setTopTitle("A Source")

        return binding.root
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

        arguments?.getString("url")?.let {
            sourceBinding?.urlInput?.setText(it)
            viewModel.getStreamFromUrl(it)
        }
    }

    override fun onDestroyView() {
        sourceBinding = null
        super.onDestroyView()
    }

}