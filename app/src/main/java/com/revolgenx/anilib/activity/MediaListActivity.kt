package com.revolgenx.anilib.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.view.*
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.infrastructure.event.DisplayModeChangedEvent
import com.revolgenx.anilib.infrastructure.event.DisplayTypes
import com.revolgenx.anilib.infrastructure.event.MediaListCollectionFilterEvent
import com.revolgenx.anilib.data.field.MediaListCollectionFilterField
import com.revolgenx.anilib.common.ui.fragment.BaseFragment
import com.revolgenx.anilib.ui.fragment.list.*
import com.revolgenx.anilib.data.meta.MediaListMeta
import com.revolgenx.anilib.common.preference.getMediaListGridPresenter
import com.revolgenx.anilib.common.preference.setMediaListGridPresenter
import com.revolgenx.anilib.databinding.MediaListActivityLayoutBinding
import com.revolgenx.anilib.databinding.SmartTabLayoutBinding
import com.revolgenx.anilib.type.MediaType
import com.revolgenx.anilib.ui.bottomsheet.list.MediaListFilterBottomSheetFragment
import com.revolgenx.anilib.ui.view.makeArrayPopupMenu
import com.revolgenx.anilib.util.registerForEvent
import com.revolgenx.anilib.util.unRegisterForEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MediaListActivity : BaseDynamicActivity<MediaListActivityLayoutBinding>() {

    companion object {
        fun openActivity(context: Context, mediaListMeta: MediaListMeta) {
            context.startActivity(Intent(context, MediaListActivity::class.java).also {
                it.putExtra(MEDIA_LIST_META_KEY, mediaListMeta)
            })
        }

        const val MEDIA_LIST_META_KEY = "MEDIA_LIST_INTENT_KEY"
    }

    private var menuItem: MenuItem? = null

    override fun bindView(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): MediaListActivityLayoutBinding {
        return MediaListActivityLayoutBinding.inflate(inflater)
    }

    private val mediaListFragment by lazy {
        listOf(
            WatchingFragment().apply {
                arguments = bundleOf(MEDIA_LIST_META_KEY to mediaListMeta)
            },
            PlanningFragment().apply {
                arguments = bundleOf(MEDIA_LIST_META_KEY to mediaListMeta)
            },
            CompletedFragment().apply {
                arguments = bundleOf(MEDIA_LIST_META_KEY to mediaListMeta)
            },
            DroppedFragment().apply {
                arguments = bundleOf(MEDIA_LIST_META_KEY to mediaListMeta)
            },
            PausedFragment().apply {
                arguments = bundleOf(MEDIA_LIST_META_KEY to mediaListMeta)
            },
            RepeatingFragment().apply {
                arguments = bundleOf(MEDIA_LIST_META_KEY to mediaListMeta)
            }
        )
    }

    private val pageChangeListener by lazy {
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
                binding.mediaListSmartTab.baseDynamicSmartTab.getTabs()
                    .forEach { it.findViewById<View>(R.id.tab_text_tv).visibility = View.GONE }
                binding.mediaListSmartTab.baseDynamicSmartTab.getTabAt(position)
                    .findViewById<View>(R.id.tab_text_tv).visibility = View.VISIBLE
            }
        }
    }


    private val tabColorStateList: ColorStateList
        get() {
            return ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_selected),
                    intArrayOf(android.R.attr.state_enabled)
                ), intArrayOf(
                    DynamicTheme.getInstance().get().accentColor,
                    DynamicTheme.getInstance().get().tintPrimaryColor
                )
            )
        }
    private lateinit var mediaListMeta: MediaListMeta
    private val accentColor by lazy {
        DynamicTheme.getInstance().get().accentColor
    }
    private val tintAccentColor by lazy {
        DynamicTheme.getInstance().get().tintAccentColor
    }
    private var mediaListFilterField = MediaListCollectionFilterField()


    override fun onStart() {
        super.onStart()
        registerForEvent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.mediaListToolbar.dynamicToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.mediaListSmartTab.baseDynamicSmartTab.setBackgroundColor(
            DynamicTheme.getInstance().get().primaryColor
        )
        statusBarColor = statusBarColor

        mediaListMeta = intent.getParcelableExtra(MEDIA_LIST_META_KEY) ?: return
        if ((mediaListMeta.userId == null && mediaListMeta.userName == null)) {
            return
        }
        supportActionBar!!.title = when (mediaListMeta.type) {
            MediaType.ANIME.ordinal -> {
                getString(R.string.anime_list)
            }
            else -> {
                getString(R.string.manga_list)
            }
        }

        val inflater = LayoutInflater.from(this)
        binding.mediaListSmartTab.baseDynamicSmartTab.setCustomTabView { container, position, _ ->
            val binding = SmartTabLayoutBinding.inflate(inflater, container, false)
            when (position) {
                0 -> {
                    createTabView(binding, R.drawable.ic_watching, R.string.watching)
                }
                1 -> {
                    createTabView(binding, R.drawable.ic_planning, R.string.planning)
                }
                2 -> {
                    createTabView(binding, R.drawable.ic_completed, R.string.completed)
                }
                3 -> {
                    createTabView(binding, R.drawable.ic_dropped, R.string.dropped)
                }
                4 -> {
                    createTabView(binding, R.drawable.ic_paused_filled, R.string.paused)
                }
                5 -> {
                    createTabView(binding, R.drawable.ic_rewatching, R.string.repeating)
                }
            }
            binding.root
        }

        binding.mediaListViewPager.addOnPageChangeListener(pageChangeListener)
        binding.mediaListViewPager.adapter = MediaListAdapter(mediaListFragment)
        binding.mediaListViewPager.offscreenPageLimit = 5
        binding.mediaListSmartTab.baseDynamicSmartTab.setViewPager(binding.mediaListViewPager, null)
        binding.mediaListViewPager.setCurrentItem(0, false)
        binding.mediaListViewPager.post {
            pageChangeListener.onPageSelected(binding.mediaListViewPager.currentItem)
        }

        (savedInstanceState?.getParcelable(MediaListFilterBottomSheetFragment.LIST_FILTER_PARCEL_KEY) as? MediaListCollectionFilterField)?.let { field ->
            mediaListFilterField = field
            if (field.search.isNullOrEmpty().not()) {
                menuItem?.expandActionView()
                (menuItem?.actionView as? SearchView)?.let {
                    it.setQuery(field.search!!, true)
                }
            }
        }
    }


    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.list_activity_menu, menu)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        menu?.findItem(R.id.listNotificationMenu)?.isVisible = false
        menu?.findItem(R.id.listSearchMenu)?.let { item ->
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
        return true
    }

    private fun getViewPagerFragment(pos: Int) =
        supportFragmentManager.findFragmentByTag("android:switcher:${R.id.mediaListViewPager}:$pos") as? MediaListFragment


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.listSearchMenu -> {
                true
            }
            R.id.listFilterMenu -> {
                MediaListFilterBottomSheetFragment().show(this, mediaListMeta.type) {
                    arguments =
                        bundleOf(MediaListFilterBottomSheetFragment.LIST_FILTER_PARCEL_KEY to mediaListFilterField)
                }
                true
            }
            R.id.listDisplayModeMenu -> {
                makeArrayPopupMenu(
                    findViewById(R.id.listSearchMenu),
                    resources.getStringArray(R.array.list_display_modes),
                    selectedPosition = getMediaListGridPresenter().ordinal
                ) { _, _, index, _ ->
                    setMediaListGridPresenter(index)
                    DisplayModeChangedEvent(DisplayTypes.MEDIA_LIST).postEvent
                }
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun createTabView(
        view: SmartTabLayoutBinding,
        @DrawableRes src: Int,
        @StringRes str: Int
    ) {
        view.tabImageView.imageTintList = tabColorStateList
        view.tabImageView.setImageResource(src)
        view.tabTextTv.text = getString(str)
        view.root.background = RippleDrawable(ColorStateList.valueOf(tintAccentColor), null, null)
        view.tabTextTv.setTextColor(accentColor)
    }

    inner class MediaListAdapter(private val fragmentList: List<BaseFragment>) :
        FragmentPagerAdapter(
            supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MediaListCollectionFilterEvent) {
        event.meta.let {
            mediaListFilterField.formatsIn = it.formatsIn
            mediaListFilterField.status = it.status
            mediaListFilterField.genre = it.genres
            mediaListFilterField.listSort = it.mediaListSort
        }
        filterMediaList()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(
            MediaListFilterBottomSheetFragment.LIST_FILTER_PARCEL_KEY,
            mediaListFilterField
        )
        super.onSaveInstanceState(outState)
    }

    private fun filterMediaList() {
        binding.mediaListViewPager.let {
            getViewPagerFragment(it.currentItem)?.filter(mediaListFilterField)
        }
    }


    override fun onStop() {
        super.onStop()
        unRegisterForEvent()
    }

}