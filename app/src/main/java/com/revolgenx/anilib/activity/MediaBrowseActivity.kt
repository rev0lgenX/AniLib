package com.revolgenx.anilib.activity

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.ViewPager
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.appbar.AppBarLayout
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.field.ToggleFavouriteField
import com.revolgenx.anilib.fragment.EntryListEditorFragment
import com.revolgenx.anilib.fragment.base.BaseFragment
import com.revolgenx.anilib.fragment.base.ParcelableFragment
import com.revolgenx.anilib.fragment.browse.*
import com.revolgenx.anilib.fragment.review.ReviewComposerFragment
import com.revolgenx.anilib.meta.DraweeViewerMeta
import com.revolgenx.anilib.meta.ListEditorMeta
import com.revolgenx.anilib.meta.MediaBrowserMeta
import com.revolgenx.anilib.meta.ReviewComposerMeta
import com.revolgenx.anilib.model.MediaBrowseModel
import com.revolgenx.anilib.preference.loggedIn
import com.revolgenx.anilib.repository.util.Resource
import com.revolgenx.anilib.repository.util.Status.*
import com.revolgenx.anilib.type.MediaType
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.*
import com.revolgenx.anilib.viewmodel.media.MediaBrowserViewModel
import kotlinx.android.synthetic.main.activity_media_browser.*
import kotlinx.android.synthetic.main.custom_bottom_navigation_view.*
import kotlinx.android.synthetic.main.smart_tab_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs

//todo://handle review
class MediaBrowseActivity : BaseDynamicActivity() {
    companion object {
        const val MEDIA_BROWSER_META = "media_browser_meta"
    }

    private var isFavourite = false
    private var toggling = false
    private lateinit var mediaBrowserMeta: MediaBrowserMeta
    private lateinit var tabColorStateList: ColorStateList

    private var circularProgressDrawable: CircularProgressDrawable? = null

    private var browseMediaBrowseModel: MediaBrowseModel? = null

    private val viewModel by viewModel<MediaBrowserViewModel>()
    private val pageChangeListener by lazy {
        object :
            ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                dynamicSmartTab.getTabs().forEach { it.tabTextTv.visibility = View.GONE }
                dynamicSmartTab.getTabAt(position).tabTextTv.visibility = View.VISIBLE
                if (position == 0) return
                appbarLayout.setExpanded(false)
            }
        }
    }

    private val accentColor by lazy {
        DynamicTheme.getInstance().get().accentColor
    }
    private val tintAccentColor by lazy {
        DynamicTheme.getInstance().get().tintAccentColor
    }

    private var state = COLLAPSED

    private val offSetChangeListener =
        AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (verticalOffset == 0) {
                state = EXPANDED
                invalidateOptionsMenu()
            } else if (abs(verticalOffset) >= (appBarLayout.totalScrollRange - mediaBrowserToolbar.height)) {
                state = COLLAPSED
                invalidateOptionsMenu()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        browserRootLayout.setBackgroundColor(DynamicTheme.getInstance().get().backgroundColor)
        mediaAddButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            ContextCompat.getDrawable(this, R.drawable.ic_arrow_down)?.also {
                it.setTint(DynamicTheme.getInstance().get().tintSurfaceColor)
            },
            null
        )
        //todo:initialize with manifest
        mediaBrowserMeta = intent.getParcelableExtra(MEDIA_BROWSER_META) ?: return

        //initialize or do nth


        viewModel.mediaLiveData.observe(this) {
            when (it.status) {
                SUCCESS -> {
                    browseMediaBrowseModel = it.data

                    browseMediaBrowseModel?.mediaListStatus?.let {
                        mediaAddButton.text =
                            resources.getStringArray(R.array.media_list_status)[it]
                    }

                    if (mediaBrowserMeta.coverImage == null || mediaBrowserMeta.bannerImage == null) {
                        mediaBrowserMeta.coverImage = it.data?.coverImage?.large ?: ""
                        mediaBrowserMeta.coverImageLarge = it.data?.coverImage?.largeImage
                        mediaBrowserMeta.bannerImage =
                            it.data?.bannerImage ?: it.data?.coverImage?.largeImage
                        mediaBrowserMeta.title = it.data?.title?.romaji ?: ""
                        updateView()
                    }
                }
            }
        }


        if (savedInstanceState == null)
            viewModel.getMediaInfo(mediaBrowserMeta.mediaId)

        val colors = intArrayOf(
            DynamicTheme.getInstance().get().accentColor,
            DynamicTheme.getInstance().get().tintPrimaryColor
        )
        setSupportActionBar(mediaBrowserToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        updateView()

        tabColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf(android.R.attr.state_enabled)
            ), colors
        )
        dynamicSmartTab.setBackgroundColor(DynamicTheme.getInstance().get().primaryColor)
        statusBarColor = statusBarColor
        setToolbarTheme()


        initListener()

        val inflater = LayoutInflater.from(this)
        dynamicSmartTab.setCustomTabView { container, position, adapter ->
            val view = inflater.inflate(R.layout.smart_tab_layout, container, false)
            when (position) {
                0 -> {
                    createTabView(view, R.drawable.ic_overview, R.string.overview)
                }
                1 -> {
                    createTabView(view, R.drawable.ic_watch, R.string.watch)
                }
                2 -> {
                    createTabView(view, R.drawable.ic_character, R.string.character)
                }
                3 -> {
                    createTabView(view, R.drawable.ic_staff, R.string.staff)
                }
                4 -> {
                    createTabView(view, R.drawable.ic_review, R.string.review)
                }
                5 -> {
                    createTabView(view, R.drawable.ic_chart, R.string.stats)
                }
                else -> {
                    null
                }
            }
        }


        val animeBrowserList = listOf(
            BaseFragment.newInstance(MediaOverviewFragment::class.java).apply {
                arguments = bundleOf(MEDIA_BROWSER_META to mediaBrowserMeta)
            },
            BaseFragment.newInstance(MediaWatchFragment::class.java).apply {
                arguments = bundleOf(MEDIA_BROWSER_META to mediaBrowserMeta)
            },
            BaseFragment.newInstance(MediaCharacterFragment::class.java).apply {
                arguments = bundleOf(MEDIA_BROWSER_META to mediaBrowserMeta)
            },
            BaseFragment.newInstance(MediaStaffFragment::class.java).apply {
                arguments = bundleOf(MEDIA_BROWSER_META to mediaBrowserMeta)
            },
            BaseFragment.newInstance(MediaReviewFragment::class.java).apply {
                arguments = bundleOf(MEDIA_BROWSER_META to mediaBrowserMeta)
            },
            BaseFragment.newInstance(MediaStatsFragment::class.java).apply {
                arguments = bundleOf(MEDIA_BROWSER_META to mediaBrowserMeta)
            }
        )

        browseMediaViewPager.addOnPageChangeListener(pageChangeListener)


        viewModel.isFavourite(mediaBrowserMeta.mediaId).observe(this, Observer {
            when (it.status) {
                SUCCESS -> {
                    isFavourite = it.data!!
                    mediaFavButton.setImageResource(if (isFavourite) R.drawable.ic_favorite else R.drawable.ic_not_favourite)
                    invalidateOptionsMenu()
                }
                ERROR -> {

                }
            }
        })

        viewModel.toggleFavMediaLiveData.observe(this, Observer {
            toggling = when (it.status) {
                SUCCESS -> {
                    isFavourite = !isFavourite
                    mediaFavButton.showLoading(false)
                    viewModel.isFavourite(mediaBrowserMeta.mediaId).value =
                        Resource.success(isFavourite)
                    mediaFavButton.setImageResource(if (isFavourite) R.drawable.ic_favorite else R.drawable.ic_not_favourite)
                    invalidateOptionsMenu()
                    false
                }
                ERROR -> {
                    mediaFavButton.showLoading(false)
                    makeToast(R.string.failed_to_toggle, icon = R.drawable.ic_error)
                    false
                }
                LOADING -> {
                    mediaFavButton.showLoading(true)
                    true
                }
            }
        })


        browseMediaViewPager.adapter = MediaBrowserAdapter(animeBrowserList)
        browseMediaViewPager.offscreenPageLimit = 5
        dynamicSmartTab.setViewPager(browseMediaViewPager) {
            if (it == 0) return@setViewPager
            appbarLayout.setExpanded(false)
        }

        browseMediaViewPager.setCurrentItem(0, false)
        browseMediaViewPager.post {
            pageChangeListener.onPageSelected(browseMediaViewPager.currentItem)
        }

    }

    private fun updateView() {
        supportActionBar!!.title = mediaBrowserMeta.title
        mediaBrowserCoverImage.setImageURI(mediaBrowserMeta.coverImage)
        mediaBrowserBannerImage.setImageURI(mediaBrowserMeta.bannerImage)
    }


    private fun initListener() {
        appbarLayout.addOnOffsetChangedListener(offSetChangeListener)

        mediaAddButton.setOnClickListener {
            openListEditor()
        }

        mediaReviewButton.setOnClickListener {
            openReviewWriter()
        }

        mediaFavButton.setOnClickListener {
            toggleFav()
        }

        mediaBrowserBannerImage.setOnClickListener {
            SimpleDraweeViewerActivity.openActivity(
                this,
                DraweeViewerMeta(mediaBrowserMeta.bannerImage)
            )
        }

        mediaBrowserCoverImage.setOnClickListener {
            SimpleDraweeViewerActivity.openActivity(
                this,
                DraweeViewerMeta(mediaBrowserMeta.coverImageLarge)
            )
        }

        /**problem with transition
         * {@link https://github.com/facebook/fresco/issues/1445}*/
        ActivityCompat.setExitSharedElementCallback(this, object : SharedElementCallback() {
            override fun onSharedElementEnd(
                sharedElementNames: List<String?>?,
                sharedElements: List<View>,
                sharedElementSnapshots: List<View?>?
            ) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
                if (sharedElements.isEmpty()) {
                    return
                }
                for (view in sharedElements) {
                    if (view is SimpleDraweeView) {
                        view.drawable.setVisible(true, true)
                    }
                }
            }
        })
    }

    private fun createTabView(view: View, @DrawableRes src: Int, @StringRes str: Int): View {
        view.tabImageView.imageTintList = tabColorStateList
        view.tabImageView.setImageResource(src)
        view.tabTextTv.text = getString(str)
        view.background = RippleDrawable(ColorStateList.valueOf(tintAccentColor), null, null)
        view.tabTextTv.setTextColor(accentColor)
        return view
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (state == EXPANDED) return false

        menuInflater.inflate(R.menu.media_browser_menu, menu)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        if (isFavourite) {
            val drawable =
                ContextCompat.getDrawable(context, R.drawable.ic_favorite)
            menu?.findItem(R.id.toggleFavMenu)?.icon = drawable
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.addToListMenu -> {
                openListEditor()
                true
            }
            R.id.writeReviewMenu -> {
                openReviewWriter()
                true
            }
            R.id.toggleFavMenu -> {
                if (loggedIn())
                    makeToast(R.string.please_wait, icon = R.drawable.ic_hour_glass)

                toggleFav()
                true
            }
            else -> false
        }
    }


    override fun setStatusBarColor(color: Int) {
        super.setStatusBarColor(color)
        setWindowStatusBarColor(statusBarColor);
    }

    private fun openListEditor() {
        if (!::mediaBrowserMeta.isInitialized) return

        if (loggedIn()) {
            val meta = ListEditorMeta(
                mediaBrowserMeta.mediaId,
                mediaBrowserMeta.type,
                mediaBrowserMeta.title,
                mediaBrowserMeta.coverImage,
                mediaBrowserMeta.bannerImage
            )

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                mediaBrowserCoverImage,
                ViewCompat.getTransitionName(mediaBrowserCoverImage) ?: ""
            )

            ContainerActivity.openActivity(
                this,
                ParcelableFragment(
                    EntryListEditorFragment::class.java,
                    bundleOf(
                        EntryListEditorFragment.LIST_EDITOR_META_KEY to meta
                    )
                ), options
            )
        } else {
            makeToast(R.string.please_log_in, null, R.drawable.ic_person)
        }
    }

    private fun toggleFav() {
        if (toggling || !::mediaBrowserMeta.isInitialized) return

        if (!loggedIn()) {
            makeToast(R.string.please_log_in, null, R.drawable.ic_person)
            return
        }

        viewModel.toggleMediaFavourite(ToggleFavouriteField().also {
            when (mediaBrowserMeta.type) {
                MediaType.ANIME.ordinal -> {
                    it.animeId = mediaBrowserMeta.mediaId
                }
                MediaType.MANGA.ordinal -> {
                    it.mangaId = mediaBrowserMeta.mediaId
                }
            }
        })
    }

    private fun openReviewWriter() {
        if (!::mediaBrowserMeta.isInitialized) return
        if (loggedIn()) {
            ContainerActivity.openActivity(
                this,
                ParcelableFragment(
                    ReviewComposerFragment::class.java,
                    bundleOf(
                        ReviewComposerFragment.reviewComposerMetaKey to ReviewComposerMeta(
                            mediaBrowserMeta.mediaId
                        )
                    )
                )
            )
        } else {
            makeToast(R.string.please_log_in, null, R.drawable.ic_person)
        }
    }

    override val layoutRes: Int = R.layout.activity_media_browser


    private fun setToolbarTheme() {
        mediaBrowserCollapsingToolbar.setStatusBarScrimColor(
            DynamicTheme.getInstance().get().primaryColorDark
        )
        mediaBrowserCollapsingToolbar.setContentScrimColor(
            DynamicTheme.getInstance().get().primaryColor
        )
        mediaBrowserCollapsingToolbar.setCollapsedTitleTextColor(
            DynamicTheme.getInstance().get().tintPrimaryColor
        )
        mediaBrowserCollapsingToolbar.setBackgroundColor(
            DynamicTheme.getInstance().get().backgroundColor
        )
    }


    inner class MediaBrowserAdapter(private val fragmentList: List<BaseFragment>) :
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


    override fun finishAfterTransition() {
        finish()
//        this.overridePendingTransition(0,android.R.anim.slide_out_right);
    }


    override fun onDestroy() {
        circularProgressDrawable?.stop()
        super.onDestroy()
    }

}

