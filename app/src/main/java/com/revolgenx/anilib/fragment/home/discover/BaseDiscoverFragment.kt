package com.revolgenx.anilib.fragment.home.discover

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.elements.Adapter
import com.otaliastudios.elements.Presenter
import com.otaliastudios.elements.Source
import com.otaliastudios.elements.pagers.PageSizePager
import com.pranavpandey.android.dynamic.support.widget.DynamicFrameLayout
import com.pranavpandey.android.dynamic.support.widget.DynamicImageView
import com.pranavpandey.android.dynamic.support.widget.DynamicTextView
import com.pranavpandey.android.dynamic.theme.Theme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.dialog.MediaFilterDialog
import com.revolgenx.anilib.dialog.DiscoverMediaListFilterDialog
import com.revolgenx.anilib.fragment.base.BaseFragment
import com.revolgenx.anilib.model.home.OrderedViewModel
import com.revolgenx.anilib.ui.view.DynamicBackgroundView
import com.revolgenx.anilib.util.dp
import kotlinx.android.synthetic.main.discover_fragment_layout.view.*

abstract class BaseDiscoverFragment : BaseFragment(), BaseDiscoverHelper {

    protected lateinit var discoverLayout: ViewGroup
    protected lateinit var garlandLayout: View

    protected val loadingPresenter: Presenter<Unit>
        get() {
            return Presenter.forLoadingIndicator(
                requireContext(), R.layout.loading_layout
            )
        }

    protected val shimmerLoader: Presenter<Unit>
        get() = Presenter.forLoadingIndicator(requireContext(), R.layout.discover_shimmer_layout)


    protected val errorPresenter: Presenter<Unit>
        get() {
            return Presenter.forErrorIndicator(requireContext(), R.layout.error_layout)
        }

    protected val emptyPresenter: Presenter<Unit>
        get() {
            return Presenter.forEmptyIndicator(requireContext(), R.layout.empty_layout)
        }

    companion object {
        const val MEDIA_TRENDING_TAG = "MEDIA_TRENDING_TAG"
        const val MEDIA_POPULAR_TAG = "MEDIA_POPULAR_TAG"
        const val NEWLY_ADDED_TAG = "NEWLY_ADDED_TAG"
        const val MEDIA_LIST_WATCHING_TAG = "MEDIA_LIST_WATCHING_TAG"
        const val MEDIA_LIST_READING_TAG = "MEDIA_LIST_READING_TAG"
    }

    protected val orderedViewList = mutableListOf<OrderedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.discover_fragment_layout, container, false)
        discoverLayout = v.discoverLinearLayout

        orderedViewList.sortBy { it.order }
        orderedViewList.forEach {
            addView(it.oView, it.title, it.showSetting, it.onClick)
        }
        orderedViewList.clear()
        return v
    }

    override fun addView(
        view: View,
        title: String,
        showSetting: Boolean,
        onClick: ((which: Int) -> Unit)?
    ) {
        val constraintLayout = ConstraintLayout(requireContext()).also {
            it.id = R.id.garlandConstraintLayout
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val garlandStickView = DynamicBackgroundView(requireContext()).also {
            it.id = R.id.garlandStickView
            it.layoutParams =
                ConstraintLayout.LayoutParams(
                    dp(4f),
                    0
                )
            it.setPadding(0,dp(2f),0,dp(2f))
        }

        val garlandTextView = DynamicTextView(requireContext()).also {
            it.id = R.id.garlandTitleTv
            it.layoutParams =
                ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            it.textSize = 16f
            it.typeface = ResourcesCompat.getFont(requireContext(), R.font.berlinrounded_extra_bold)
            it.setPadding(dp(10f))
            it.gravity = Gravity.CENTER_VERTICAL
        }

        val garlandSettingIv = DynamicImageView(requireContext()).also {
            it.id = R.id.garlandSettingIv
            it.layoutParams = ViewGroup.LayoutParams(0, 0)
            it.setImageResource(R.drawable.ic_button_setting)
            it.colorType = Theme.ColorType.TINT_SURFACE
            it.setPadding(dp(8f))
            it.visibility = View.GONE
        }

        val garlandContainer = DynamicFrameLayout(requireContext()).also {
            it.id = R.id.gralandFrameContainer
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        garlandTextView.text = title
        if (showSetting) {
            garlandSettingIv.visibility = View.VISIBLE
            garlandSettingIv.setOnClickListener {
                onClick?.invoke(1)
            }
        }

        garlandTextView.setOnClickListener {
            onClick?.invoke(0)
        }

        constraintLayout.addView(garlandStickView)
        constraintLayout.addView(garlandTextView)
        constraintLayout.addView(garlandSettingIv)
        constraintLayout.addView(garlandContainer)
        garlandContainer.addView(view)

        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        constraintSet.constrainPercentHeight(garlandStickView.id, 1f)

        constraintSet.connect(
            garlandStickView.id,
            ConstraintSet.START,
            constraintLayout.id,
            ConstraintSet.START,
            dp(10f)
        )

        constraintSet.connect(
            garlandStickView.id,
            ConstraintSet.TOP,
            garlandTextView.id,
            ConstraintSet.TOP,
            dp(2f)
        )

        constraintSet.connect(
            garlandStickView.id,
            ConstraintSet.BOTTOM,
            garlandTextView.id,
            ConstraintSet.BOTTOM,
            dp(2f)
        )

        constraintSet.connect(
            garlandTextView.id,
            ConstraintSet.START,
            garlandStickView.id,
            ConstraintSet.END,
            0
        )

        constraintSet.connect(
            garlandTextView.id,
            ConstraintSet.TOP,
            constraintLayout.id,
            ConstraintSet.TOP,
            0
        )

        constraintSet.connect(
            garlandSettingIv.id,
            ConstraintSet.END,
            constraintLayout.id,
            ConstraintSet.END,
            0
        )
        constraintSet.connect(
            garlandSettingIv.id,
            ConstraintSet.BOTTOM,
            garlandTextView.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            garlandSettingIv.id,
            ConstraintSet.TOP,
            garlandTextView.id,
            ConstraintSet.TOP,
            0
        )

        constraintSet.connect(
            garlandContainer.id,
            ConstraintSet.START,
            constraintLayout.id,
            ConstraintSet.START
        )
        constraintSet.connect(
            garlandContainer.id,
            ConstraintSet.TOP,
            garlandTextView.id,
            ConstraintSet.BOTTOM
        )

//        constraintSet.setMargin(garlandTextView.id, ConstraintSet.TOP, dp(10f))
        constraintSet.setDimensionRatio(garlandSettingIv.id, "1:1")
        constraintSet.applyTo(constraintLayout)

        discoverLayout.addView(constraintLayout)
    }


    protected fun showMediaFilterDialog(type: Int, tag: String, callback: (() -> Unit)) {
        MediaFilterDialog.newInstance(type).also {
            it.onDoneListener = callback
            it.show(childFragmentManager, tag)
        }
    }


    protected fun showMediaListFilterDialog(type: Int, tag: String, callback: (() -> Unit)) {
        DiscoverMediaListFilterDialog.newInstance(type).also {
            it.onDoneListener = callback
            it.show(childFragmentManager, tag)
        }
    }

    protected fun RecyclerView.createAdapter(
        source: Source<*>,
        presenter: Presenter<*>,
        useShimmerLoader: Boolean = false,
        loader: Presenter<*>? = null
    ): Adapter {
        return Adapter.builder(viewLifecycleOwner, 10)
            .setPager(PageSizePager(10))
            .addSource(source)
            .addPresenter(presenter)
            .addPresenter(loader ?: if (useShimmerLoader) shimmerLoader else loadingPresenter)
            .addPresenter(errorPresenter)
            .addPresenter(emptyPresenter)
            .into(this)
    }

}