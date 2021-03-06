package com.revolgenx.anilib.ui.fragment.list

import android.content.res.Configuration
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.otaliastudios.elements.Presenter
import com.otaliastudios.elements.Source
import com.revolgenx.anilib.activity.MediaListActivity
import com.revolgenx.anilib.infrastructure.event.DisplayModeChangedEvent
import com.revolgenx.anilib.infrastructure.event.DisplayTypes
import com.revolgenx.anilib.data.field.MediaListCollectionFilterField
import com.revolgenx.anilib.common.ui.fragment.BasePresenterFragment
import com.revolgenx.anilib.data.meta.MediaListMeta
import com.revolgenx.anilib.data.model.list.MediaListModel
import com.revolgenx.anilib.common.preference.getMediaListGridPresenter
import com.revolgenx.anilib.constant.MediaListDisplayMode
import com.revolgenx.anilib.ui.presenter.home.discover.MediaListCollectionPresenter
import com.revolgenx.anilib.util.registerForEvent
import com.revolgenx.anilib.util.unRegisterForEvent
import com.revolgenx.anilib.ui.viewmodel.list.MediaListCollectionViewModel
import com.revolgenx.anilib.util.EventBusListener
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class MediaListFragment : BasePresenterFragment<MediaListModel>(), EventBusListener {

    override val basePresenter: Presenter<MediaListModel>
        get() = MediaListCollectionPresenter(requireContext(), mediaListMeta!!, viewModel)
    override val baseSource: Source<MediaListModel>
        get() = viewModel.source ?: createSource()

    abstract val viewModel: MediaListCollectionViewModel
    abstract val mediaListStatus: Int

    protected var mediaListMeta: MediaListMeta? = null


    override fun createSource(): Source<MediaListModel> {
        return viewModel.createSource()
    }

    override fun reloadLayoutManager() {
        var span =
            if (requireContext().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 2

        when (getMediaListGridPresenter()) {
            MediaListDisplayMode.COMPACT -> {

            }

            MediaListDisplayMode.NORMAL, MediaListDisplayMode.MINIMAL_LIST -> {
                span /= 2
            }

            MediaListDisplayMode.CARD -> {

            }

            MediaListDisplayMode.CLASSIC, MediaListDisplayMode.MINIMAL -> {
                span += 1
            }

        }

        layoutManager =
            GridLayoutManager(
                this.context,
                span
            ).also {
                it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (adapter?.getItemViewType(position) == 0) {
                            1
                        } else {
                            span
                        }
                    }
                }
            }

        super.reloadLayoutManager()
    }

    override fun onStart() {
        super.onStart()
        registerForEvent()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        arguments?.classLoader = MediaListMeta::class.java.classLoader
        mediaListMeta = arguments?.getParcelable(MediaListActivity.MEDIA_LIST_META_KEY) ?: return
        viewModel.field.also { field ->
            field.userId = mediaListMeta?.userId
            field.userName = mediaListMeta?.userName
            field.mediaListStatus = mediaListStatus
            field.type = mediaListMeta?.type
        }
        super.onActivityCreated(savedInstanceState)

        baseSwipeRefreshLayout.setOnRefreshListener {
            viewModel.renewSource()
            createSource()
            baseSwipeRefreshLayout.isRefreshing = false
            invalidateAdapter()
        }
    }

    fun filter(mediaListFilterField: MediaListCollectionFilterField) {
        viewModel.updateFilter(mediaListFilterField)
        if (visibleToUser) {
            createSource()
            invalidateAdapter()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDisplayModeEvent(event: DisplayModeChangedEvent) {
        when (event.whichDisplay) {
            DisplayTypes.MEDIA_LIST -> {
                changePresenterLayout()
            }
        }
    }

    private fun changePresenterLayout() {
        reloadLayoutManager()
        if (visibleToUser) {
            invalidateAdapter()
        }
    }

    override fun onStop() {
        super.onStop()
        unRegisterForEvent()
    }
}
