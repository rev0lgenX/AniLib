package com.revolgenx.anilib.fragment.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.elements.Adapter
import com.otaliastudios.elements.Presenter
import com.otaliastudios.elements.Source
import com.otaliastudios.elements.pagers.PageSizePager
import com.pranavpandey.android.dynamic.support.widget.DynamicSwipeRefreshLayout
import com.revolgenx.anilib.R
import kotlinx.android.synthetic.main.base_presenter_fragment_layout.view.*
import timber.log.Timber


/**
 * BasePresenter class contains @property baseRecyclerView @property layoutManager
 *
 * */
abstract class BasePresenterFragment<M : Any>() : BaseLayoutFragment() {
    abstract val basePresenter: Presenter<M>
    abstract val baseSource: Source<M>

    var visibleToUser = false

    var adapter: Adapter? = null

    override val layoutRes: Int = R.layout.base_presenter_fragment_layout

    protected open val loadingPresenter: Presenter<Unit>
        get() = Presenter.forLoadingIndicator(
            requireContext(), R.layout.loading_layout
        )

    private val errorPresenter: Presenter<Unit> by lazy {
        Presenter.forErrorIndicator(requireContext(), R.layout.error_layout)
    }

    private val emptyPresenter: Presenter<Unit> by lazy {
        Presenter.forEmptyIndicator(requireContext(), R.layout.empty_layout)
    }

    lateinit var baseRecyclerView: RecyclerView
    lateinit var baseSwipeRefreshLayout: DynamicSwipeRefreshLayout
    lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        baseRecyclerView = v.base_presenter_recycler_view
        baseSwipeRefreshLayout = v.base_swipe_to_refresh
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val span =
            if (requireContext().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1
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

    }

    protected open fun reloadLayoutManager() {
        baseRecyclerView.layoutManager = layoutManager
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        reloadLayoutManager()

        baseSwipeRefreshLayout.setOnRefreshListener {
            createSource()
            baseSwipeRefreshLayout.isRefreshing = false
            invalidateAdapter()
        }
    }


    override fun onResume() {
        super.onResume()
        if (!visibleToUser)
            invalidateAdapter()
        visibleToUser = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        visibleToUser = false
    }

    abstract fun createSource(): Source<M>

    /** call this method to load into recyclerview*/
    protected fun invalidateAdapter() {
        adapter = Adapter.builder(this, 10)
            .setPager(PageSizePager(10))
            .addSource(baseSource)
            .addPresenter(basePresenter)
            .addPresenter(loadingPresenter)
            .addPresenter(errorPresenter)
            .addPresenter(emptyPresenter)
            .into(baseRecyclerView)
    }

}