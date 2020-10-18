package com.revolgenx.anilib.fragment.home.discover

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.pranavpandey.android.dynamic.support.widget.DynamicRecyclerView
import com.revolgenx.anilib.R
import com.revolgenx.anilib.event.BrowseAllReviewsEvent
import com.revolgenx.anilib.presenter.review.ReviewPresenter
import com.revolgenx.anilib.source.home.discover.AllReviewSource
import com.revolgenx.anilib.util.dp
import com.revolgenx.anilib.viewmodel.home.discover.DiscoverReviewViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

open class DiscoverReviewFragment : DiscoverNewFragment() {
    private lateinit var discoverNewRecyclerView: DynamicRecyclerView
    private val viewModel by viewModel<DiscoverReviewViewModel>()
    private val presenter
        get() = ReviewPresenter(requireContext())

    private val source: AllReviewSource
        get() = viewModel.source ?: viewModel.createSource()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        discoverNewRecyclerView = DynamicRecyclerView(requireContext()).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(600f)
            )
        }

        addView(
            discoverNewRecyclerView,
            getString(R.string.recent_reviews) + " >>>", showSetting = false
        ) {
            handleClick(it)
        }

        return v
    }

    private fun handleClick(which: Int) {
        if (which == 0) {
            BrowseAllReviewsEvent().postEvent
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        discoverNewRecyclerView.layoutManager =
            GridLayoutManager(
                this.context,
                if (requireContext().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1
            )
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        invalidateAdapter()
    }

    /** call this method to load into recyclerview*/
    private fun invalidateAdapter() {
        discoverNewRecyclerView.createAdapter(source, presenter)
    }
}