package com.revolgenx.anilib.fragment.home.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.pranavpandey.android.dynamic.support.widget.DynamicRecyclerView
import com.revolgenx.anilib.R
import com.revolgenx.anilib.dialog.MediaFilterDialog
import com.revolgenx.anilib.event.BrowseTrendingEvent
import com.revolgenx.anilib.field.home.NewlyAddedMediaField
import com.revolgenx.anilib.model.home.HomeOrderType
import com.revolgenx.anilib.model.home.OrderedViewModel
import com.revolgenx.anilib.model.search.filter.MediaSearchFilterModel
import com.revolgenx.anilib.preference.getHomeOrderFromType
import com.revolgenx.anilib.presenter.home.MediaPresenter
import com.revolgenx.anilib.source.MediaSource
import com.revolgenx.anilib.type.MediaSort
import com.revolgenx.anilib.viewmodel.home.discover.DiscoverNewViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

open class DiscoverNewFragment : DiscoverPopularFragment() {

    private lateinit var discoverNewRecyclerView: DynamicRecyclerView
    private val viewModel by viewModel<DiscoverNewViewModel>()

    private val presenter
        get() = MediaPresenter(requireContext())

    private val source: MediaSource
        get() = viewModel.source ?: viewModel.createSource()

    private val order: Int
        get() = getHomeOrderFromType(requireContext(), HomeOrderType.NEWLY_ADDED)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        discoverNewRecyclerView = DynamicRecyclerView(requireContext()).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.isNestedScrollingEnabled = false
        }

        orderedViewList.add(OrderedViewModel(
            discoverNewRecyclerView, order,
            getString(R.string.newly_added) + " >>>", showSetting = true
        ) {
            handleClick(it)
        })
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        discoverNewRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState == null)
            viewModel.field = NewlyAddedMediaField.create(requireContext()).also {
                it.sort = MediaSort.ID_DESC.ordinal
            }
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            childFragmentManager.findFragmentByTag(NEWLY_ADDED_TAG)?.let {
                (it as MediaFilterDialog).onDoneListener = {
                    renewAdapter()
                }
            }
        }

        invalidateAdapter()
    }


    private fun renewAdapter() {
        viewModel.updateField(requireContext())
        viewModel.createSource()
        invalidateAdapter()
    }

    /** call this method to load into recyclerview*/
    private fun invalidateAdapter() {
        viewModel.adapter = discoverNewRecyclerView.createAdapter(source, presenter, true)
    }

    private fun handleClick(which: Int) {
        if (which == 0) {
            BrowseTrendingEvent(MediaSearchFilterModel().also {
                it.sort = MediaSort.ID_DESC.ordinal
            }).postEvent
        } else if (which == 1) {
            showMediaFilterDialog(
                MediaFilterDialog.MediaFilterType.NEWLY_ADDED.ordinal,
                NEWLY_ADDED_TAG
            ) {
                renewAdapter()
            }
        }
    }

}