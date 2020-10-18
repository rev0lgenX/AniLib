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
import com.revolgenx.anilib.field.home.TrendingMediaField
import com.revolgenx.anilib.model.home.HomeOrderType
import com.revolgenx.anilib.model.home.OrderedViewModel
import com.revolgenx.anilib.model.search.filter.MediaSearchFilterModel
import com.revolgenx.anilib.preference.getHomeOrderFromType
import com.revolgenx.anilib.presenter.home.MediaPresenter
import com.revolgenx.anilib.source.MediaSource
import com.revolgenx.anilib.type.MediaSort
import com.revolgenx.anilib.viewmodel.TrendingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

open class DiscoverTrendingFragment : DiscoverReadingFragment() {

    private var trendingRecyclerView: DynamicRecyclerView? = null

    private val presenter
        get() = MediaPresenter(requireContext())

    private val source: MediaSource
        get() = viewModel.source ?: viewModel.createSource()


    private val viewModel by viewModel<TrendingViewModel>()

    private val order: Int
        get() = getHomeOrderFromType(requireContext(), HomeOrderType.TRENDING)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        trendingRecyclerView = DynamicRecyclerView(requireContext()).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.isNestedScrollingEnabled = false
        }

        orderedViewList.add(OrderedViewModel(
            trendingRecyclerView!!, order,
            getString(R.string.trending) + " >>>"
            , showSetting = true
        ) {
            handleClick(it)
        })

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trendingRecyclerView!!.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    override fun reloadAll() {
        super.reloadAll()

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState == null)
            viewModel.field = TrendingMediaField.create(requireContext()).also {
                it.sort = MediaSort.TRENDING_DESC.ordinal
            }
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            childFragmentManager.findFragmentByTag(MEDIA_TRENDING_TAG)?.let {
                (it as MediaFilterDialog).onDoneListener = {
                    renewAdapter()
                }
            }
        }
        invalidateAdapter()
    }


    private fun handleClick(which: Int) {
        if (which == 0) {
            BrowseTrendingEvent(MediaSearchFilterModel().also {
                it.sort = MediaSort.TRENDING_DESC.ordinal
            }).postEvent
        } else if (which == 1) {
            showMediaFilterDialog(
                MediaFilterDialog.MediaFilterType.TRENDING.ordinal,
                MEDIA_TRENDING_TAG
            ) {
                renewAdapter()
            }
        }
    }

    private fun renewAdapter() {
        viewModel.updateField(requireContext())
        viewModel.createSource()
        invalidateAdapter()
    }

    /** call this method to load into recyclerview*/
    private fun invalidateAdapter() {
        if (trendingRecyclerView == null) return
        trendingRecyclerView!!.createAdapter(source, presenter, true)
    }
}