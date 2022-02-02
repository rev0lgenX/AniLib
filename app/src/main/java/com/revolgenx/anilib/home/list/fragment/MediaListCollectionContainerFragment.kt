package com.revolgenx.anilib.home.list.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.ui.adapter.makeViewPagerAdapter2
import com.revolgenx.anilib.common.ui.fragment.BaseLayoutFragment
import com.revolgenx.anilib.databinding.MediaListCollectionContainerFragmentBinding
import com.revolgenx.anilib.list.fragment.AnimeListCollectionFragment
import com.revolgenx.anilib.list.fragment.BaseMediaListCollectionFragment
import com.revolgenx.anilib.list.fragment.MangaListCollectionFragment
import com.revolgenx.anilib.list.viewmodel.MediaListCollectionContainerCallback
import com.revolgenx.anilib.list.viewmodel.MediaListContainerSharedVM
import com.revolgenx.anilib.type.MediaType
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaListCollectionContainerFragment :
    BaseLayoutFragment<MediaListCollectionContainerFragmentBinding>() {
    private val fragments: List<BaseMediaListCollectionFragment> by lazy {
        listOf(
            AnimeListCollectionFragment(),
            MangaListCollectionFragment()
        )
    }
    private val sharedViewModel by viewModel<MediaListContainerSharedVM>()

    private var tabLayoutMediator: TabLayoutMediator? = null
    override fun bindView(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): MediaListCollectionContainerFragmentBinding {
        return MediaListCollectionContainerFragmentBinding.inflate(inflater, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            alListViewPager.adapter = makeViewPagerAdapter2(fragments)
            tabLayoutMediator = TabLayoutMediator(listTabLayout, alListViewPager) { tab, position ->
                tab.text = resources.getStringArray(R.array.list_tab_menu)[position]
            }.also { it.attach() }

            alListViewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    sharedViewModel.mediaListContainerCallback.value =
                        MediaListCollectionContainerCallback.CURRENT_TAB to if (position == 0)
                            MediaType.ANIME.ordinal
                        else MediaType.MANGA.ordinal
                }
            })

            sharedViewModel.currentGroupNameWithCount.observe(viewLifecycleOwner) {
                if (it == null) {
                    listExtendedFab.text =
                        "%s %d".format(requireContext().getString(R.string.all), 0)
                } else {
                    listExtendedFab.text = "%s %d".format(it.first, it.second)
                }
            }

            listSearchIv.setOnClickListener {
                sharedViewModel.mediaListContainerCallback.value =
                    MediaListCollectionContainerCallback.SEARCH to alListViewPager.currentItem
            }

            listExtendedFab.setOnClickListener {
                sharedViewModel.mediaListContainerCallback.value =
                    MediaListCollectionContainerCallback.GROUP to alListViewPager.currentItem
            }

            listMoreIv.onPopupMenuClickListener = { _, position ->
                when (position) {
                    0 -> {
                        sharedViewModel.mediaListContainerCallback.value =
                            MediaListCollectionContainerCallback.DISPLAY to alListViewPager.currentItem
                    }
                    1 -> {
                        sharedViewModel.mediaListContainerCallback.value =
                            MediaListCollectionContainerCallback.FILTER to alListViewPager.currentItem
                    }
                }
            }

        }
    }

    override fun onDestroyView() {
        tabLayoutMediator?.detach()
        super.onDestroyView()
    }

}