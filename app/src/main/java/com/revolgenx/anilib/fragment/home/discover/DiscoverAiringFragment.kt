package com.revolgenx.anilib.fragment.home.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.pranavpandey.android.dynamic.support.widget.DynamicRecyclerView
import com.revolgenx.anilib.R
import com.revolgenx.anilib.activity.ContainerActivity
import com.revolgenx.anilib.fragment.airing.AiringFragment
import com.revolgenx.anilib.fragment.base.ParcelableFragment
import com.revolgenx.anilib.model.home.HomeOrderType
import com.revolgenx.anilib.model.home.OrderedViewModel
import com.revolgenx.anilib.preference.getHomeOrderFromType
import com.revolgenx.anilib.presenter.home.discover.DiscoverAiringPresenter
import com.revolgenx.anilib.source.home.airing.AiringSource
import org.koin.androidx.viewmodel.ext.android.viewModel


open class DiscoverAiringFragment : BaseDiscoverFragment() {

    private val presenter
        get() = DiscoverAiringPresenter(requireContext())

    private val source: AiringSource
        get() = viewModel.source ?: viewModel.createSource()

    private var discoverAiringRecyclerView: DynamicRecyclerView? = null
    private val viewModel by viewModel<DiscoverAiringViewModel>()

    private val order: Int
        get() = getHomeOrderFromType(requireContext(), HomeOrderType.AIRING)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        discoverAiringRecyclerView = DynamicRecyclerView(requireContext()).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.isNestedScrollingEnabled = false
        }
        orderedViewList.add(OrderedViewModel(
            discoverAiringRecyclerView!!, order,
            getString(R.string.airing_schedules) + " >>>"
            , showSetting = false
        ) {
            this@DiscoverAiringFragment.handleClick(it)
        })

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        discoverAiringRecyclerView!!.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        invalidateAdapter()
    }


    override fun reloadAll() {
        viewModel.createSource()
        invalidateAdapter()
    }

    private fun handleClick(which: Int) {
        if (which == 0) {
            ContainerActivity.openActivity(
                requireContext(),
                ParcelableFragment(
                    AiringFragment::class.java,
                    bundleOf()
                )
            )
        }
    }

    /** call this method to load into recyclerview*/
    private fun invalidateAdapter() {
        if (discoverAiringRecyclerView == null) return
        viewModel.adapter =
            discoverAiringRecyclerView!!.createAdapter(source, presenter, true)
    }
}