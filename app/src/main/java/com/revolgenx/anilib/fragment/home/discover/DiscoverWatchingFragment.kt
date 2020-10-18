package com.revolgenx.anilib.fragment.home.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.pranavpandey.android.dynamic.support.widget.DynamicRecyclerView
import com.revolgenx.anilib.R
import com.revolgenx.anilib.dialog.DiscoverMediaListFilterDialog
import com.revolgenx.anilib.event.BrowseMediaListEvent
import com.revolgenx.anilib.meta.MediaListMeta
import com.revolgenx.anilib.model.home.HomeOrderType
import com.revolgenx.anilib.model.home.OrderedViewModel
import com.revolgenx.anilib.preference.getDiscoverMediaListSort
import com.revolgenx.anilib.preference.getHomeOrderFromType
import com.revolgenx.anilib.preference.loggedIn
import com.revolgenx.anilib.preference.userId
import com.revolgenx.anilib.presenter.list.MediaListPresenter
import com.revolgenx.anilib.source.media_list.MediaListSource
import com.revolgenx.anilib.type.MediaListStatus
import com.revolgenx.anilib.type.MediaType
import com.revolgenx.anilib.viewmodel.home.discover.DiscoverWatchingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

open class DiscoverWatchingFragment : DiscoverAiringFragment() {
    private var watchingRecyclerView: DynamicRecyclerView? = null

    private val presenter
        get() = MediaListPresenter(
            requireContext(),
            MediaListMeta(requireContext().userId()),
            viewModel
        )

    private val source: MediaListSource
        get() = viewModel.source ?: viewModel.createSource()

    private val viewModel by viewModel<DiscoverWatchingViewModel>()

    private val order: Int
        get() = getHomeOrderFromType(requireContext(), HomeOrderType.WATCHING)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (requireContext().loggedIn()) {
            watchingRecyclerView = DynamicRecyclerView(requireContext()).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                it.isNestedScrollingEnabled = false
            }

            orderedViewList.add(OrderedViewModel(
                watchingRecyclerView!!, order,
                getString(R.string.watching) + " >>>"
                , showSetting = true
            ) {
                handleClick(it)
            })
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireContext().loggedIn()) {
            watchingRecyclerView!!.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    override fun reloadAll() {
        super.reloadAll()

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (requireContext().loggedIn()) {

            if (savedInstanceState == null) {
                viewModel.field.also { field ->
                    field.userId = requireContext().userId()
                    field.status = MediaListStatus.CURRENT.ordinal
                    field.type = MediaType.ANIME.ordinal
                    field.sort = getDiscoverMediaListSort(requireContext(), field.type!!)
                }
            }
            super.onActivityCreated(savedInstanceState)

            if (savedInstanceState != null) {
                childFragmentManager.findFragmentByTag(MEDIA_LIST_WATCHING_TAG)?.let {
                    (it as DiscoverMediaListFilterDialog).onDoneListener = {
                        renewAdapter()
                    }
                }
            }
            invalidateAdapter()
        }
        else{
            super.onActivityCreated(savedInstanceState)
        }
    }


    private fun handleClick(which: Int) {
        when (which) {
            0 -> {
                BrowseMediaListEvent(
                    MediaListMeta(
                        requireContext().userId(),
                        null,
                        MediaType.ANIME.ordinal
                    )
                ).postEvent
            }
            1 -> {
                showMediaListFilterDialog(
                    MediaType.ANIME.ordinal,
                    MEDIA_LIST_WATCHING_TAG
                ) {
                    renewAdapter()
                }
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
        if (watchingRecyclerView == null) return
        watchingRecyclerView!!.createAdapter(source, presenter)
    }
}