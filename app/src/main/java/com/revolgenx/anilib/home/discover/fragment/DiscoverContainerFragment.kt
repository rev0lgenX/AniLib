package com.revolgenx.anilib.home.discover.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.revolgenx.anilib.R
import com.revolgenx.anilib.app.theme.dynamicAccentColor
import com.revolgenx.anilib.common.preference.*
import com.revolgenx.anilib.common.ui.adapter.makePagerAdapter
import com.revolgenx.anilib.common.ui.fragment.BaseLayoutFragment
import com.revolgenx.anilib.home.season.data.field.SeasonField
import com.revolgenx.anilib.databinding.DiscoverContainerFragmentBinding
import com.revolgenx.anilib.infrastructure.event.*
import com.revolgenx.anilib.home.discover.bottomsheet.MediaFilterBottomSheetFragment
import com.revolgenx.anilib.home.season.dialog.ShowSeasonHeaderDialog
import com.revolgenx.anilib.home.recommendation.fragment.RecommendationFragment
import com.revolgenx.anilib.home.season.fragment.SeasonFragment
import com.revolgenx.anilib.ui.view.setBoundsFor
import com.revolgenx.anilib.home.recommendation.viewmodel.RecommendationViewModel
import com.revolgenx.anilib.home.season.viewmodel.SeasonViewModel
import com.revolgenx.anilib.notification.viewmodel.NotificationStoreViewModel
import com.revolgenx.anilib.util.onItemSelected
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DiscoverContainerFragment : BaseLayoutFragment<DiscoverContainerFragmentBinding>() {

    private lateinit var adapter: FragmentPagerAdapter

    private val discoverFragments by lazy {
        listOf(
            DiscoverFragment(),
            SeasonFragment(),
            RecommendationFragment()
        )
    }

    private val seasonViewModel by viewModel<SeasonViewModel>()
    private val recommendationViewModel by viewModel<RecommendationViewModel>()
    private val notificationStoreVM by sharedViewModel<NotificationStoreViewModel>()

    private val seasons by lazy {
        requireContext().resources.getStringArray(R.array.media_season)
    }

    override fun bindView(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): DiscoverContainerFragmentBinding {
        return DiscoverContainerFragmentBinding.inflate(inflater, parent, false)
    }


    @SuppressLint("UnsafeExperimentalUsageError")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.initListener()
        binding.initSeasonListener()
        binding.initRecommendationListener()

        binding.discoverNotificationIv.visibility =
            if (requireContext().loggedIn()) View.VISIBLE else View.GONE

        binding.discoverNotificationIv.post {
            val badgeDrawable = BadgeDrawable.create(requireContext())

            badgeDrawable.badgeGravity = BadgeDrawable.TOP_END
            badgeDrawable.backgroundColor = dynamicAccentColor
            badgeDrawable.verticalOffset = 4
            badgeDrawable.horizontalOffset = 4
            badgeDrawable.setBoundsFor(binding.discoverNotificationIv, binding.discoverNotifLayout)

            notificationStoreVM.unreadNotificationCount.observe(viewLifecycleOwner, {
                if (it > 0) {
                    BadgeUtils.attachBadgeDrawable(
                        badgeDrawable,
                        binding.discoverNotificationIv,
                        binding.discoverNotifLayout
                    )
                } else {
                    BadgeUtils.detachBadgeDrawable(
                        badgeDrawable,
                        binding.discoverNotificationIv
                    )
                }
            })
        }

        adapter = makePagerAdapter(
            discoverFragments,
            resources.getStringArray(R.array.discover_tab_menu)
        )
        binding.discoverContainerViewPager.adapter = adapter
        binding.discoverContainerViewPager.offscreenPageLimit = 3
        binding.discoverMainTabLayout.setupWithViewPager(binding.discoverContainerViewPager)

        if (savedInstanceState == null) {
            seasonViewModel.field = SeasonField.create(requireContext())

            with(recommendationViewModel.field) {
                sort = getRecommendationSort(requireContext())
                onList = getRecommendationOnList(requireContext())
            }
        }
    }


    private fun DiscoverContainerFragmentBinding.initSeasonListener() {
        seasonFilterFooter.setOnClickListener {

        }
        seasonPreviousIv.setOnClickListener {
            seasonViewModel.previousSeason(requireContext())
            updateSeasonFooterTitle()
            SeasonEvent.SeasonChangeEvent.postEvent
        }

        seasonNextIv.setOnClickListener {
            seasonViewModel.nextSeason(requireContext())
            updateSeasonFooterTitle()
            SeasonEvent.SeasonChangeEvent.postEvent
        }

        seasonFilterIv.onPopupMenuClickListener = { _, position ->
            when (position) {
                0 -> {
                    MediaFilterBottomSheetFragment().show(requireContext()) {
                        arguments =
                            bundleOf(MediaFilterBottomSheetFragment.mediaFilterTypeKey to MediaFilterBottomSheetFragment.MediaFilterType.SEASON.ordinal)
                        onDoneListener = {
                            seasonViewModel.field.updateFields(requireContext())
                            updateSeasonFooterTitle()
                            SeasonEvent.SeasonFilterEvent.postEvent
                        }
                    }
                }
                1 -> {
                    SeasonEvent.SeasonGenreEvent.postEvent
                }
                2 -> {
                    SeasonEvent.SeasonTagEvent.postEvent
                }
                3 -> {
                    ShowSeasonHeaderDialog().also {
                        it.onDoneListener = onDone@{ isChecked, isChanged ->
                            if (context == null) return@onDone
                            if (isChanged) {
                                SeasonEvent.SeasonHeaderEvent(isChecked).postEvent
                            }
                        }
                    }.show(requireContext())
                }
            }
        }
    }

    private fun DiscoverContainerFragmentBinding.initRecommendationListener() {
        recommendationOnListCheckBox.onCheckedChangeListener = null
        recommendationOnListCheckBox.setOnCheckedChangeListener { _, isChecked ->
            val onList =
                if (requireContext().loggedIn()) isChecked else null
            recommendationViewModel.field.onList = onList
            RecommendationEvent.RecommendationFilterEvent(
                onList,
                recommendationViewModel.field.sort!!
            ).postEvent
        }

        recommendationSortSpinner.onItemSelectedListener = null
        recommendationSortSpinner.setSelection(recommendationViewModel.field.sort ?: 0, false)
        recommendationSortSpinner.onItemSelected { position ->
            RecommendationEvent.RecommendationFilterEvent(
                recommendationViewModel.field.onList,
                position
            ).postEvent
        }
    }


    private fun DiscoverContainerFragmentBinding.initListener() {
        discoverSearchIv.setOnClickListener {
            OpenSearchEvent().postEvent
        }
        discoverNotificationIv.setOnClickListener {
            notificationStoreVM.setUnreadNotificationCount(0)
            OpenNotificationCenterEvent().postEvent
        }
        discoverContainerViewPager.addOnPageChangeListener(object :
            ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                when (position) {
                    1 -> {
                        updateSeasonFooterTitle()
                        recommendationFilterFooter.visibility = View.GONE
                        seasonFilterFooter.visibility = View.VISIBLE
                        val behavior =
                            (seasonFilterFooter.layoutParams as CoordinatorLayout.LayoutParams).behavior
                        (behavior as HideBottomViewOnScrollBehavior).slideUp(seasonFilterFooter)
                        discoverContainerAppBar.setExpanded(true, true)
                    }
                    2 -> {
                        seasonFilterFooter.visibility = View.GONE
                        recommendationFilterFooter.visibility = View.VISIBLE
                        val behavior =
                            (recommendationFilterFooter.layoutParams as CoordinatorLayout.LayoutParams).behavior
                        (behavior as HideBottomViewOnScrollBehavior).slideUp(
                            recommendationFilterFooter
                        )
                        discoverContainerAppBar.setExpanded(true, true)
                    }
                    else -> {
                        recommendationFilterFooter.visibility = View.GONE
                        seasonFilterFooter.visibility = View.GONE
                    }
                }
            }
        })

    }


    private fun DiscoverContainerFragmentBinding.updateSeasonFooterTitle() {
        seasonViewModel.field.let { field ->
            var toolbarTitle = ""
            field.season?.let { season ->
                toolbarTitle += seasons[season]
            }
            field.seasonYear?.let {
                toolbarTitle += " $it"
            }
            seasonNameTv.text = toolbarTitle
        }
    }
}