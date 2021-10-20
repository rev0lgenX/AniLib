package com.revolgenx.anilib.ui.view.navigation

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.flexbox.FlexboxLayoutManager
import com.otaliastudios.elements.Adapter
import com.otaliastudios.elements.Source
import com.pranavpandey.android.dynamic.support.adapter.DynamicSpinnerImageAdapter
import com.pranavpandey.android.dynamic.support.model.DynamicMenu
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.pranavpandey.android.dynamic.support.widget.DynamicNavigationView
import com.revolgenx.anilib.R
import com.revolgenx.anilib.constant.MediaTagFilterTypes
import com.revolgenx.anilib.constant.SearchTypes
import com.revolgenx.anilib.app.theme.ThemeController
import com.revolgenx.anilib.common.preference.*
import com.revolgenx.anilib.data.field.TagField
import com.revolgenx.anilib.data.field.TagState
import com.revolgenx.anilib.data.meta.type.AlMediaSort
import com.revolgenx.anilib.data.model.search.filter.*
import com.revolgenx.anilib.databinding.BrowseFilterNavigationViewBinding
import com.revolgenx.anilib.ui.dialog.sorting.AniLibSortingModel
import com.revolgenx.anilib.ui.dialog.sorting.SortOrder
import com.revolgenx.anilib.ui.presenter.TagPresenter
import com.revolgenx.anilib.ui.view.TriStateCheckState
import com.revolgenx.anilib.ui.view.makeSpinnerAdapter
import com.revolgenx.anilib.util.hideKeyboard
import com.revolgenx.anilib.util.onItemSelected
import java.util.*
import kotlin.math.ceil

class BrowseFilterNavigationView : DynamicNavigationView {

    private var mListener: AdvanceBrowseNavigationCallbackListener? = null
    private val rView get() = rViewBinding.root
    private lateinit var rViewBinding: BrowseFilterNavigationViewBinding


    private val rippleDrawable: RippleDrawable
        get() = RippleDrawable(ColorStateList.valueOf(accentColor), null, null)

    private val accentColor: Int = DynamicTheme.getInstance().get().accentColor

    private lateinit var streamAdapter: Adapter
    private lateinit var genreAdapter: Adapter
    private lateinit var tagAdapter: Adapter


    private val streamPresenter by lazy {
        TagPresenter(context).also {
            it.tagRemoved {
                streamingTagMap!![it]?.tagState = TagState.EMPTY
                mListener?.onTagRemoved(it, MediaTagFilterTypes.STREAMING_ON)
            }
        }
    }
    private val genrePresenter by lazy {
        TagPresenter(context).also {
            it.tagRemoved {
                genreTagMap!![it]?.tagState = TagState.EMPTY
                mListener?.onTagRemoved(it, MediaTagFilterTypes.GENRES)
            }
        }
    }

    private val tagPresenter by lazy {
        TagPresenter(context).also {
            it.tagRemoved {
                tagTagMap!![it]?.tagState = TagState.EMPTY
                mListener?.onTagRemoved(it, MediaTagFilterTypes.TAGS)
            }
        }
    }

    private val alMediaSortList by lazy {
        context.resources.getStringArray(R.array.al_media_sort)
    }


    private val streamingOnList by lazy {
        getUserStream(context)
    }

    private val tagList by lazy {
        getUserTag(context)
    }

    private val excludedTagList by lazy {
        getExcludedTags(context)
    }

    private val genreList by lazy {
        getUserGenre(context)
    }

    private val excludedGenreList by lazy {
        getExcludedGenre(context)
    }


    private var genreTagMap: MutableMap<String, TagField>? = null
        get() {
            field = field ?: mutableMapOf<String, TagField>().also { map ->
                genreList.forEach {
                    val tagState = if(excludedGenreList.contains(it)) TagState.UNTAGGED else TagState.EMPTY
                    map[it] = TagField(it, tagState)
                }
            }
            return field
        }

    private var tagTagMap: MutableMap<String, TagField>? = null
        get() {
            field = field ?: mutableMapOf<String, TagField>().also { map ->
                tagList.forEach {
                    val tagState = if(excludedTagList.contains(it)) TagState.UNTAGGED else TagState.EMPTY
                    map[it] = TagField(it, tagState)
                }
            }
            return field
        }


    private var streamingTagMap: MutableMap<String, TagField>? = null
        get() {
            field = field ?: mutableMapOf<String, TagField>().also { map ->
                streamingOnList.forEach { map[it] = TagField(it, TagState.EMPTY) }
            }
            return field
        }

    private var searchText: String?
        set(value) {
            if (value != null) {
                rViewBinding.browseSearchEt.setText(value)
            }
        }
        get() {
            return rViewBinding.browseSearchEt.text.toString()
        }

    val drawerListener = object : DrawerLayout.DrawerListener {
        override fun onDrawerStateChanged(newState: Int) {
        }

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        }

        override fun onDrawerClosed(drawerView: View) {
        }

        override fun onDrawerOpened(drawerView: View) {
            mListener?.getQuery()?.let {
                rViewBinding.browseSearchEt.setText(it)
            }
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0) {
        rViewBinding =
            BrowseFilterNavigationViewBinding.inflate(LayoutInflater.from(context), null, false)
        addView(rView)
        rViewBinding.browseSearchInputLayout.apply {
            this.setEndIconTintList(
                ColorStateList.valueOf(
                    DynamicTheme.getInstance().get().tintAccentColor
                )
            )
            this.setStartIconTintList(
                ColorStateList.valueOf(
                    DynamicTheme.getInstance().get().accentColor
                )
            )
        }
        rViewBinding.updateTheme()
        rViewBinding.updateView()
        rViewBinding.updateListener()
    }

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
    }


    fun buildStreamAdapter(builder: Adapter.Builder, list: List<TagField>) {
        list.forEach {
            streamingTagMap!![it.tag]?.tagState = it.tagState
        }
        invalidateStreamAdapter(builder)
    }

    fun buildGenreAdapter(builder: Adapter.Builder, list: List<TagField>) {
        list.forEach {
            genreTagMap!![it.tag] = it
        }
        invalidateGenreAdapter(builder)
    }


    fun buildTagAdapter(builder: Adapter.Builder, list: List<TagField>) {
        list.forEach {
            tagTagMap!![it.tag]?.tagState = it.tagState
        }
        invalidateTagAdapter(builder)
    }


    fun invalidateStreamAdapter(builder: Adapter.Builder) {
        streamAdapter = builder
            .addSource(
                Source.fromList(streamingTagMap!!.values.filter { it.tagState == TagState.TAGGED || it.tagState == TagState.UNTAGGED })
            )
            .addPresenter(streamPresenter)
            .into(rViewBinding.streamingOnRecyclerView)
    }

    fun invalidateGenreAdapter(builder: Adapter.Builder) {
        genreAdapter = builder
            .addSource(
                Source.fromList(genreTagMap!!.values.filter { it.tagState == TagState.TAGGED || it.tagState == TagState.UNTAGGED })
            )
            .addPresenter(genrePresenter)
            .into(rViewBinding.tagGenreRecyclerView)
    }


    fun invalidateTagAdapter(builder: Adapter.Builder) {
        tagAdapter = builder
            .addSource(
                Source.fromList(tagTagMap!!.values.filter { it.tagState == TagState.TAGGED || it.tagState == TagState.UNTAGGED })
            )
            .addPresenter(tagPresenter)
            .into(rViewBinding.tagRecyclerView)
    }


    fun BrowseFilterNavigationViewBinding.updateView() {
        hentaiSwitchContainerLayout.visibility =
            if (canShowAdult(context)) View.VISIBLE else View.GONE

        val searchTypeItems: List<DynamicMenu> =
            context.resources.getStringArray(R.array.advance_search_type).map {
                DynamicMenu(
                    null, it
                )
            }
        val searchSeasonItems: List<DynamicMenu> =
            context.resources.getStringArray(R.array.advance_search_season).map {
                DynamicMenu(
                    null, it
                )
            }


        val searchFormatItems: List<DynamicMenu> =
            context.resources.getStringArray(R.array.advance_search_format).map {
                DynamicMenu(
                    null, it
                )
            }

        val searchStatusItems: List<DynamicMenu> =
            context.resources.getStringArray(R.array.advance_search_status).map {
                DynamicMenu(
                    null, it
                )
            }

        val searchSourceItems: List<DynamicMenu> =
            context.resources.getStringArray(R.array.advance_search_source).map {
                DynamicMenu(
                    null, it
                )
            }
        val searchCountryItems: List<DynamicMenu> =
            context.resources.getStringArray(R.array.advance_search_country).map {
                DynamicMenu(
                    null, it
                )
            }

        browseTypeSpinner.adapter = makeSpinnerAdapter(context, searchTypeItems)
        browseSeasonSpinner.adapter = makeSpinnerAdapter(context, searchSeasonItems)
        browseFormatSpinner.adapter = makeSpinnerAdapter(context, searchFormatItems)
        browseStatusSpinner.adapter = makeSpinnerAdapter(context, searchStatusItems)
        browseSourceSpinner.adapter = makeSpinnerAdapter(context, searchSourceItems)
        browseCountrySpinner.adapter = makeSpinnerAdapter(context, searchCountryItems)

        val alMediaSorts = AlMediaSort.values()
        alBrowseSort.setSortItems(alMediaSortList.mapIndexed { index, s ->
            AniLibSortingModel(alMediaSorts[index], s, SortOrder.NONE)
        })

        browseYearSeekBar.isEnabled = enableYearCheckBox.isChecked
        val currentYear = Calendar.getInstance().get(Calendar.YEAR) + 1f
        browseYearSeekBar.setRange(1950f, currentYear)
        browseYearSeekBar.setProgress(1950f, currentYear)
        yearTv.text =
            if (enableYearCheckBox.isChecked)
                context.getString(R.string.year)
            else
                context.getString(R.string.year_disabled)
        browseYearSeekBar.progressLeft = 1950

        tagRecyclerView.layoutManager = FlexboxLayoutManager(context)
        tagGenreRecyclerView.layoutManager = FlexboxLayoutManager(context)
        streamingOnRecyclerView.layoutManager = FlexboxLayoutManager(context)

    }

    private fun BrowseFilterNavigationViewBinding.updateTheme() {
        ThemeController.lightSurfaceColor().let {
            searchTypeFrameLayout.setBackgroundColor(it)
            browseSeasonFrameLayout.setBackgroundColor(it)
            browseSortFrameLayout.setBackgroundColor(it)
            browseFormatFrameLayout.setBackgroundColor(it)
            browseStatusFrameLayout.setBackgroundColor(it)
            browseSourceFrameLayout.setBackgroundColor(it)
            browseCountryFrameLayout.setBackgroundColor(it)
            browseStreamingFrameLayout.setBackgroundColor(it)
            genreFrameLayout.setBackgroundColor(it)
            tagFrameLayout.setBackgroundColor(it)
            tagAddIv.background = rippleDrawable
            genreAddIv.background = rippleDrawable
            streamAddIv.background = rippleDrawable
            browseYearSeekBar.setIndicatorTextDecimalFormat("0")
            browseYearSeekBar.setTypeface(
                ResourcesCompat.getFont(
                    context,
                    R.font.cabincondensed_regular
                )
            )

        }

        DynamicTheme.getInstance().get().accentColor.let {
            browseYearSeekBar.progressColor = it
            browseYearSeekBar.leftSeekBar?.indicatorBackgroundColor = it
            browseYearSeekBar.rightSeekBar?.indicatorBackgroundColor = it
        }
    }

    private fun BrowseFilterNavigationViewBinding.updateListener() {
        hentaiSwitchContainerLayout.setOnClickListener {
            hentaiOnlySwtich.toggleTriState()
        }

        yearSwitchContainerLayout.setOnClickListener {
            enableYearCheckBox.isChecked = !enableYearCheckBox.isChecked
        }

        enableYearCheckBox.setOnCheckedChangeListener { _, isChecked ->
            browseYearSeekBar.isEnabled = isChecked
            yearTv.text =
                if (isChecked)
                    context.getString(R.string.year)
                else
                    context.getString(R.string.year_disabled)
        }


        browseTypeSpinner.onItemSelected {
            if (it == 0 || it == 1) {
                browseMediaFilterContainer.visibility = View.VISIBLE

            } else {
                browseMediaFilterContainer.visibility = View.GONE
            }
        }

        streamAddIv.setOnClickListener {
            mListener?.openTagChooserDialog(
                streamingTagMap!!.values.toList(),
                MediaTagFilterTypes.STREAMING_ON
            )
        }

        genreAddIv.setOnClickListener {
            mListener?.openTagChooserDialog(
                genreTagMap!!.values.toList(),
                MediaTagFilterTypes.GENRES
            )
        }

        tagAddIv.setOnClickListener {
            mListener?.openTagChooserDialog(tagTagMap!!.values.toList(), MediaTagFilterTypes.TAGS)
        }

        applyFilterCardView.setOnClickListener {
            context.hideKeyboard(browseSearchEt)
            applyFilter()
        }
    }


    fun getFilter(): SearchFilterModel {
        return when (rViewBinding.browseTypeSpinner.selectedItemPosition) {
            SearchTypes.ANIME.ordinal, SearchTypes.MANGA.ordinal -> {
                MediaSearchFilterModel().apply {
                    query = searchText
                    season = rViewBinding.browseSeasonSpinner.selectedItemPosition.minus(1)
                        .takeIf { it >= 0 }

                    type = rViewBinding.browseTypeSpinner.selectedItemPosition
                    yearEnabled = rViewBinding.enableYearCheckBox.isChecked
                    if (yearEnabled) {
                        minYear = rViewBinding.browseYearSeekBar.leftSeekBar.progress.let {
                            ceil(it).toInt()
                        }
                        maxYear = rViewBinding.browseYearSeekBar.rightSeekBar.progress.let {
                            ceil(it).toInt()
                        }
                    }
                    sort = rViewBinding.alBrowseSort.getActiveSortItem()?.let {
                        if (it.order == SortOrder.DESC) {
                            (it.data as AlMediaSort).sort + 1
                        } else {
                            (it.data as AlMediaSort).sort
                        }
                    }
                    format =
                        rViewBinding.browseFormatSpinner.selectedItemPosition.minus(1).takeIf {
                            it >= 0
                        }
                    status =
                        rViewBinding.browseStatusSpinner.selectedItemPosition.minus(1).takeIf {
                            it >= 0
                        }
                    countryOfOrigin =
                        rViewBinding.browseCountrySpinner.selectedItemPosition.minus(1).takeIf {
                            it >= 0
                        }
                    source =
                        rViewBinding.browseSourceSpinner.selectedItemPosition.minus(1).takeIf {
                            it >= 0
                        }
                    streamingOn = streamingTagMap!!.values.filter { it.tagState == TagState.TAGGED }
                        .map { it.tag }
                    genre = genreTagMap!!.values.filter { it.tagState == TagState.TAGGED }
                        .map { it.tag }
                    genreToExclude =
                        genreTagMap!!.values.filter { it.tagState == TagState.UNTAGGED }
                            .map { it.tag }
                    tags =
                        tagTagMap!!.values.filter { it.tagState == TagState.TAGGED }.map { it.tag }
                    tagsToExclude = tagTagMap!!.values.filter { it.tagState == TagState.UNTAGGED }
                        .map { it.tag }

                    hentaiOnly = when(rViewBinding.hentaiOnlySwtich.checkedState){
                        TriStateCheckState.TICK -> true
                        TriStateCheckState.CROSS -> false
                        TriStateCheckState.EMPTY -> null
                    }
                }
            }
            SearchTypes.CHARACTER.ordinal -> {
                CharacterSearchFilterModel().apply {
                    query = searchText
                }
            }
            SearchTypes.STAFF.ordinal -> {
                StaffSearchFilterModel().apply {
                    query = searchText
                }
            }
            SearchTypes.STUDIO.ordinal -> {
                StudioSearchFilterModel().apply {
                    query = searchText
                }
            }
            SearchTypes.USER.ordinal -> {
                UserSearchFilterModel().apply {
                    query = searchText
                }
            }
            else -> {
                MediaSearchFilterModel().apply {
                    query = searchText
                }
            }
        }
    }


    fun setFilter(value: SearchFilterModel, applyFilter: Boolean = true) {
        rViewBinding.browseTypeSpinner.setSelection(value.type)
        when (value) {
            is MediaSearchFilterModel -> {
                value.let {
                    searchText = value.query
                    it.season?.let {
                        rViewBinding.browseSeasonSpinner.setSelection(it + 1)
                    }
                    rViewBinding.enableYearCheckBox.isChecked = it.yearEnabled
                    rViewBinding.browseYearSeekBar.isEnabled = it.yearEnabled

                    if (it.minYear != null && it.maxYear != null)
                        rViewBinding.browseYearSeekBar.setProgress(
                            it.minYear!!.toFloat(),
                            it.maxYear!!.toFloat()
                        )
                    it.sort?.let { sort ->
                        val alMediaSorts = AlMediaSort.values()
                        var sortOrder = SortOrder.NONE
                        val currentSort = if (sort < 34) {
                            if (sort % 2 == 0) {
                                sortOrder = SortOrder.ASC
                                sort
                            } else {
                                sortOrder = SortOrder.DESC
                                sort - 1
                            }
                        } else if (sort > 34) {
                            if (sort % 2 == 0) {
                                sortOrder = SortOrder.DESC
                                sort - 1
                            } else {
                                sortOrder = SortOrder.ASC
                                sort
                            }
                        } else {
                            null
                        }

                        if (currentSort != null) {
                            val currentSortEnum = alMediaSorts.first { it.sort == currentSort }
                            AniLibSortingModel(
                                currentSortEnum,
                                alMediaSortList[currentSortEnum.ordinal],
                                sortOrder
                            ).let { model ->
                                rViewBinding.alBrowseSort.setActiveSortItem(model)
                            }
                        }
                    }
                    it.format?.let {
                        rViewBinding.browseFormatSpinner.setSelection(it + 1)
                    }
                    it.status?.let {
                        rViewBinding.browseStatusSpinner.setSelection(it + 1)
                    }
                    it.streamingOn?.mapNotNull {
                        streamingTagMap!![it]?.tagState = TagState.TAGGED
                        streamingTagMap!![it]
                    }?.let {
                        mListener?.onTagAdd(it, MediaTagFilterTypes.STREAMING_ON)
                    }

                    it.countryOfOrigin?.let {
                        rViewBinding.browseCountrySpinner.setSelection(it + 1)
                    }
                    it.source?.let {
                        rViewBinding.browseSourceSpinner.setSelection(it + 1)
                    }
                    it.genre?.mapNotNull {
                        genreTagMap!![it]?.tagState = TagState.TAGGED
                        genreTagMap!![it]
                    }?.let {
                        mListener?.onTagAdd(it, MediaTagFilterTypes.GENRES)
                    }

                    it.tags?.mapNotNull {
                        tagTagMap!![it]?.tagState = TagState.TAGGED
                        tagTagMap!![it]
                    }?.let {
                        mListener?.onTagAdd(it, MediaTagFilterTypes.TAGS)
                    }

                    it.tagsToExclude?.mapNotNull {
                        tagTagMap!![it]?.tagState = TagState.UNTAGGED
                        tagTagMap!![it]
                    }?.let {
                        mListener?.onTagAdd(it, MediaTagFilterTypes.TAGS)
                    }

                    it.genreToExclude?.mapNotNull {
                        genreTagMap!![it]?.tagState = TagState.UNTAGGED
                        genreTagMap!![it]
                    }?.let {
                        mListener?.onTagAdd(it, MediaTagFilterTypes.GENRES)
                    }

                    rViewBinding.hentaiOnlySwtich.checkedState = when(it.hentaiOnly){
                        true -> TriStateCheckState.TICK
                        false -> TriStateCheckState.CROSS
                        null -> TriStateCheckState.EMPTY
                    }

                    mListener?.updateTags(MediaTagFilterTypes.TAGS)
                    mListener?.updateTags(MediaTagFilterTypes.GENRES)
                    mListener?.updateTags(MediaTagFilterTypes.STREAMING_ON)
                }
            }
            is CharacterSearchFilterModel -> {
                searchText = value.query
            }
            is StaffSearchFilterModel -> {
                searchText = value.query
            }
            is StudioSearchFilterModel -> {
                searchText = value.query
            }
            is UserSearchFilterModel -> {
                searchText = value.query
            }
        }
        if (applyFilter) {
            applyFilter()
        }
    }

    private fun applyFilter() {
        mListener?.applyFilter()
    }


    fun setNavigationCallbackListener(listener: AdvanceBrowseNavigationCallbackListener) {
        mListener = listener
    }

    interface AdvanceBrowseNavigationCallbackListener {
        fun openTagChooserDialog(tags: List<TagField>, tagType: MediaTagFilterTypes)
        fun onTagAdd(tags: List<TagField>, tagType: MediaTagFilterTypes)
        fun onTagRemoved(tag: String, tagType: MediaTagFilterTypes)
        fun updateTags(tagType: MediaTagFilterTypes)
        fun getQuery(): String
        fun applyFilter()
    }

}

