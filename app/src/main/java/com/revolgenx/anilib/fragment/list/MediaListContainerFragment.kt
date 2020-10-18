package com.revolgenx.anilib.fragment.list

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.view.*
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.viewpager.widget.ViewPager
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.activity.MediaListActivity
import com.revolgenx.anilib.adapter.MediaListAdapter
import com.revolgenx.anilib.dialog.MediaListCollectionFilterDialog
import com.revolgenx.anilib.event.DisplayModeChangedEvent
import com.revolgenx.anilib.event.DisplayTypes
import com.revolgenx.anilib.event.MediaListCollectionFilterEvent
import com.revolgenx.anilib.field.MediaListCollectionFilterField
import com.revolgenx.anilib.fragment.base.BaseTransitiveLayoutFragment
import com.revolgenx.anilib.meta.MediaListMeta
import com.revolgenx.anilib.preference.setMediaListGridPresenter
import com.revolgenx.anilib.type.MediaType
import com.revolgenx.anilib.util.registerForEvent
import com.revolgenx.anilib.util.unRegisterForEvent
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_bottom_navigation_view.*
import kotlinx.android.synthetic.main.media_list_activity_layout.*
import kotlinx.android.synthetic.main.smart_tab_layout.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class MediaListContainerFragment : BaseTransitiveLayoutFragment() {
    override val layoutRes: Int = R.layout.media_list_container_fragment_layout

    private val mediaListMeta: MediaListMeta by lazy {
        mediaListMetaArgs()
    }

    private var menuItem: MenuItem? = null

    private val mediaListFragment by lazy {
        listOf(
            newInstance(WatchingFragment::class.java).apply {
                arguments = bundleOf(MediaListActivity.MEDIA_LIST_META_KEY to mediaListMeta)
            },
            newInstance(PlanningFragment::class.java).apply {
                arguments = bundleOf(MediaListActivity.MEDIA_LIST_META_KEY to mediaListMeta)
            },
            newInstance(CompletedFragment::class.java).apply {
                arguments = bundleOf(MediaListActivity.MEDIA_LIST_META_KEY to mediaListMeta)
            },
            newInstance(DroppedFragment::class.java).apply {
                arguments = bundleOf(MediaListActivity.MEDIA_LIST_META_KEY to mediaListMeta)
            },
            newInstance(PausedFragment::class.java).apply {
                arguments = bundleOf(MediaListActivity.MEDIA_LIST_META_KEY to mediaListMeta)
            },
            newInstance(RepeatingFragment::class.java).apply {
                arguments = bundleOf(MediaListActivity.MEDIA_LIST_META_KEY to mediaListMeta)
            }
        )
    }

    private val pageChangeListener
        get() =
            object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    if (menuItem?.isActionViewExpanded == true)
                        menuItem?.collapseActionView()
                }


                override fun onPageSelected(position: Int) {
                    dynamicSmartTab.getTabs().forEach { it.tabTextTv.visibility = View.GONE }
                    dynamicSmartTab.getTabAt(position).tabTextTv.visibility = View.VISIBLE
                }
            }

    private val tabColorStateList: ColorStateList
        get() {
            return ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_selected),
                    intArrayOf(android.R.attr.state_enabled)
                )
                , intArrayOf(
                    DynamicTheme.getInstance().get().accentColor,
                    DynamicTheme.getInstance().get().tintPrimaryColor
                )
            )
        }

    private val accentColor =
        DynamicTheme.getInstance().get().accentColor
    private val tintAccentColor =
        DynamicTheme.getInstance().get().tintAccentColor

    private var mediaListFilterField = MediaListCollectionFilterField()

    override val toolbar: Toolbar
        get() = dynamicToolbar

    protected abstract fun mediaListMetaArgs():MediaListMeta

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dynamicSmartTab.setBackgroundColor(DynamicTheme.getInstance().get().primaryColor)
    }

    override fun onStart() {
        super.onStart()
        registerForEvent()
    }

    override fun onStop() {
        super.onStop()
        unRegisterForEvent()
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initListener()
        if ((mediaListMeta.userId == null && mediaListMeta.userName == null)) {
            return
        }

        toolbarTitle = when (mediaListMeta.type) {
            MediaType.ANIME.ordinal -> {
                getString(R.string.anime_list)
            }
            else -> {
                getString(R.string.manga_list)
            }
        }


        val inflater = LayoutInflater.from(requireContext())
        dynamicSmartTab.setCustomTabView { container, position, _ ->
            val view = inflater.inflate(R.layout.smart_tab_layout, container, false)
            when (position) {
                0 -> {
                    createTabView(view, R.drawable.ic_watching, R.string.watching)
                }
                1 -> {
                    createTabView(view, R.drawable.ic_planning, R.string.planning)
                }
                2 -> {
                    createTabView(view, R.drawable.ic_completed, R.string.completed)
                }
                3 -> {
                    createTabView(view, R.drawable.ic_dropped, R.string.dropped)
                }
                4 -> {
                    createTabView(view, R.drawable.ic_paused_filled, R.string.paused)
                }
                5 -> {
                    createTabView(view, R.drawable.ic_rewatching, R.string.repeating)
                }
                else -> {
                    null
                }
            }
        }

        mediaListViewPager.addOnPageChangeListener(pageChangeListener)
        mediaListViewPager.adapter = MediaListAdapter(childFragmentManager, mediaListFragment)
        mediaListViewPager.offscreenPageLimit = 5
        dynamicSmartTab.setViewPager(mediaListViewPager, null)
        mediaListViewPager.setCurrentItem(0, false)
        mediaListViewPager.post {
            pageChangeListener.onPageSelected(mediaListViewPager.currentItem)
        }

        (savedInstanceState?.getParcelable(MediaListCollectionFilterDialog.LIST_FILTER_PARCEL_KEY) as? MediaListCollectionFilterField)?.let { field ->
            mediaListFilterField = field
            if (field.search.isNullOrEmpty().not()) {
                menuItem?.expandActionView()
                (menuItem?.actionView as? SearchView)?.let {
                    it.setQuery(field.search!!, true)
                }
            }
        }
    }

    private fun initListener() {
        val toggle = ActionBarDrawerToggle(
            requireActivity(),
            requireActivity().drawerLayout,
            dynamicToolbar,
            R.string.nav_open,
            R.string.nav_close
        )

        requireActivity().drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.list_activity_menu, menu)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        menu.findItem(R.id.listSearchMenu)?.let { item ->
            menuItem = item
            (item.actionView as SearchView).also {
                it.setOnQueryTextListener(object :
                    SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        mediaListFilterField.search = newText
                        filterMediaList()
                        return true
                    }
                })
            }
        }
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.listSearchMenu -> {
                true
            }
            R.id.listFilterMenu -> {
                MediaListCollectionFilterDialog.newInstance(mediaListFilterField)
                    .show(childFragmentManager, "media_filter_dialog")
                true
            }
            R.id.listDisplayModeMenu -> {
                setMediaListGridPresenter()
                DisplayModeChangedEvent(DisplayTypes.MEDIA_LIST).postEvent
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun filterMediaList() {
        mediaListViewPager?.let {
            getViewPagerFragment(it.currentItem)?.filter(mediaListFilterField)
        }
    }

    private fun getViewPagerFragment(pos: Int) =
        childFragmentManager.findFragmentByTag("android:switcher:${R.id.mediaListViewPager}:$pos") as? MediaListFragment


    private fun createTabView(view: View, @DrawableRes src: Int, @StringRes str: Int): View {
        view.tabImageView.imageTintList = tabColorStateList
        view.tabImageView.setImageResource(src)
        view.tabTextTv.text = getString(str)
        view.background = RippleDrawable(ColorStateList.valueOf(tintAccentColor), null, null)
        view.tabTextTv.setTextColor(accentColor)
        return view
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MediaListCollectionFilterEvent) {
        event.meta.let {
            mediaListFilterField.format = it.format
            mediaListFilterField.status = it.status
            mediaListFilterField.genre = it.genres
            mediaListFilterField.listSort = it.mediaListSort
        }
        filterMediaList()
    }
}