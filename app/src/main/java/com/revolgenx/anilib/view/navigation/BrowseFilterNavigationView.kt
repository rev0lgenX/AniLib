package com.revolgenx.anilib.view.navigation

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.otaliastudios.elements.Adapter
import com.otaliastudios.elements.Source
import com.pranavpandey.android.dynamic.support.adapter.DynamicSpinnerImageAdapter
import com.pranavpandey.android.dynamic.support.model.DynamicSpinnerItem
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.pranavpandey.android.dynamic.support.widget.DynamicNavigationView
import com.revolgenx.anilib.R
import com.revolgenx.anilib.constant.MediaTagFilterTypes
import com.revolgenx.anilib.constant.SearchTypes
import com.revolgenx.anilib.controller.ThemeController
import com.revolgenx.anilib.field.TagField
import com.revolgenx.anilib.model.search.filter.*
import com.revolgenx.anilib.preference.getUserGenre
import com.revolgenx.anilib.preference.getUserStream
import com.revolgenx.anilib.preference.getUserTag
import com.revolgenx.anilib.presenter.TagPresenter
import com.revolgenx.anilib.util.hideKeyboard
import com.revolgenx.anilib.util.onItemSelected
import kotlinx.android.synthetic.main.browse_filter_navigation_view.view.*
import java.util.*
import kotlin.math.ceil


class BrowseFilterNavigationView(context: Context, attributeSet: AttributeSet?, style: Int) :
    DynamicNavigationView(context, attributeSet, style) {

    private var mListener: AdvanceBrowseNavigationCallbackListener? = null
    private val rView by lazy {
        LayoutInflater.from(context).inflate(
            R.layout.browse_filter_navigation_view,
            null,
            false
        )
    }

    private val rippleDrawable: RippleDrawable
        get() = RippleDrawable(ColorStateList.valueOf(accentColor), null, null)

    private val accentColor: Int = DynamicTheme.getInstance().get().accentColor

    private lateinit var streamAdapter: Adapter
    private lateinit var genreAdapter: Adapter
    private lateinit var tagAdapter: Adapter
    private lateinit var tagExcludeAdapter: Adapter
    private lateinit var genreExcludeAdapter: Adapter


    private val streamPresenter by lazy {
        TagPresenter(context).also {
            it.tagRemoved {
                streamingTagMap!![it]?.isTagged = false
                mListener?.onTagRemoved(it, MediaTagFilterTypes.STREAMING_ON)
            }
        }
    }
    private val genrePresenter by lazy {
        TagPresenter(context).also {
            it.tagRemoved {
                genreTagMap!![it]?.isTagged = false
                mListener?.onTagRemoved(it, MediaTagFilterTypes.GENRES)
            }
        }
    }

    private val tagPresenter by lazy {
        TagPresenter(context).also {
            it.tagRemoved {
                tagTagMap!![it]?.isTagged = false
                mListener?.onTagRemoved(it, MediaTagFilterTypes.TAGS)
            }
        }
    }

    private val tagExcludePresenter by lazy {
        TagPresenter(context).also {
            it.tagRemoved {
                tagExcludedTagMap!![it]?.isTagged = false
                mListener?.onTagRemoved(it, MediaTagFilterTypes.TAG_EXCLUDE)
            }
        }
    }

    private val genreExcludePresenter by lazy {
        TagPresenter(context).also {
            it.tagRemoved {
                genreExcludedTagMap!![it]?.isTagged = false
                mListener?.onTagRemoved(it, MediaTagFilterTypes.GENRE_EXCLUDE)
            }
        }
    }

    private val streamingOnList
        get() =
            getUserStream(context)

    private val tagList
        get() = getUserTag(context)

    private val genreList
        get() =
            getUserGenre(context)

    private var genreTagMap: MutableMap<String, TagField>? = null
        get() {
            field = field ?: mutableMapOf<String, TagField>().also { map ->
                genreList.forEach { map[it] = TagField(it, false) }
            }
            return field
        }

    private var tagTagMap: MutableMap<String, TagField>? = null
        get() {
            field = field ?: mutableMapOf<String, TagField>().also { map ->
                tagList.forEach { map[it] = TagField(it, false) }
            }
            return field
        }

    private var genreExcludedTagMap: MutableMap<String, TagField>? = null
        get() {
            field = field ?: mutableMapOf<String, TagField>().also { map ->
                genreList.forEach { map[it] = TagField(it, false) }
            }
            return field
        }

    private var tagExcludedTagMap: MutableMap<String, TagField>? = null
        get() {
            field = field ?: mutableMapOf<String, TagField>().also { map ->
                tagList.forEach { map[it] = TagField(it, false) }
            }
            return field
        }

    private var streamingTagMap: MutableMap<String, TagField>? = null
        get() {
            field = field ?: mutableMapOf<String, TagField>().also { map ->
                streamingOnList.forEach { map[it] = TagField(it, false) }
            }
            return field
        }

    private var searchText: String?
        set(value) {
            if (value != null) {
                browseSearchEt.setText(value)
            }
        }
        get() {
            return browseSearchEt.text.toString()
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
                browseSearchEt.setText(it)
            }
        }
    }

    //region ctor
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0) {
        addView(rView)
        rView.browseSearchInputLayout.apply {
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
        rView.updateTheme()
        rView.updateView()
        rView.updateListener()
    }

    //endregion ctor
    fun buildStreamAdapter(builder: Adapter.Builder, list: List<TagField>) {
        list.forEach {
            streamingTagMap!![it.tag]?.isTagged = it.isTagged
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
            tagTagMap!![it.tag]?.isTagged = it.isTagged
        }
        invalidateTagAdapter(builder)
    }


    fun buildTagExcludeAdapter(builder: Adapter.Builder, list: List<TagField>) {
        list.forEach {
            tagExcludedTagMap!![it.tag]?.isTagged = it.isTagged
        }
        invalidateTagExcludeAdapter(builder)
    }


    fun buildGenreExcludeAdapter(builder: Adapter.Builder, list: List<TagField>) {
        list.forEach {
            genreExcludedTagMap!![it.tag]?.isTagged = it.isTagged
        }
        invalidateGenreExcludeAdapter(builder)
    }


    fun invalidateStreamAdapter(builder: Adapter.Builder) {
        streamAdapter = builder
            .addSource(
                Source.fromList(streamingTagMap!!.values.filter { it.isTagged }.map { it.tag })
            )
            .addPresenter(streamPresenter)
            .into(streamingOnRecyclerView)
    }

    fun invalidateGenreAdapter(builder: Adapter.Builder) {
        genreAdapter = builder
            .addSource(
                Source.fromList(genreTagMap!!.values.filter { it.isTagged }.map { it.tag })
            )
            .addPresenter(genrePresenter)
            .into(tagGenreRecyclerView)
    }


    fun invalidateTagAdapter(builder: Adapter.Builder) {
        tagAdapter = builder
            .addSource(
                Source.fromList(tagTagMap!!.values.filter { it.isTagged }.map { it.tag })
            )
            .addPresenter(tagPresenter)
            .into(tagRecyclerView)
    }

    fun invalidateTagExcludeAdapter(builder: Adapter.Builder) {
        tagExcludeAdapter = builder
            .addSource(
                Source.fromList(tagExcludedTagMap!!.values.filter { it.isTagged }.map { it.tag })
            )
            .addPresenter(tagExcludePresenter)
            .into(tagExcludeRecyclerView)
    }

    fun invalidateGenreExcludeAdapter(builder: Adapter.Builder) {
        genreExcludeAdapter = builder
            .addSource(
                Source.fromList(genreExcludedTagMap!!.values.filter { it.isTagged }.map { it.tag })
            )
            .addPresenter(genreExcludePresenter)
            .into(genreExcludeRecyclerView)
    }


    fun View.updateView() {
        val searchTypeItems: List<DynamicSpinnerItem> =
            context.resources.getStringArray(R.array.advance_search_type).map {
                DynamicSpinnerItem(
                    null, it
                )
            }
        val searchSeasonItems: List<DynamicSpinnerItem> =
            context.resources.getStringArray(R.array.advance_search_season).map {
                DynamicSpinnerItem(
                    null, it
                )
            }
        val searchSortItems: List<DynamicSpinnerItem> =
            context.resources.getStringArray(R.array.advance_search_sort).map {
                DynamicSpinnerItem(
                    null, it
                )
            }

        val searchFormatItems: List<DynamicSpinnerItem> =
            context.resources.getStringArray(R.array.advance_search_format).map {
                DynamicSpinnerItem(
                    null, it
                )
            }

        val searchStatusItems: List<DynamicSpinnerItem> =
            context.resources.getStringArray(R.array.advance_search_status).map {
                DynamicSpinnerItem(
                    null, it
                )
            }

        val searchSourceItems: List<DynamicSpinnerItem> =
            context.resources.getStringArray(R.array.advance_search_source).map {
                DynamicSpinnerItem(
                    null, it
                )
            }
        val searchCountryItems: List<DynamicSpinnerItem> =
            context.resources.getStringArray(R.array.advance_search_country).map {
                DynamicSpinnerItem(
                    null, it
                )
            }

        browseTypeSpinner.adapter = makeSpinnerAdapter(searchTypeItems)
        browseSeasonSpinner.adapter = makeSpinnerAdapter(searchSeasonItems)
        browseSortSpinner.adapter = makeSpinnerAdapter(searchSortItems)
        browseFormatSpinner.adapter = makeSpinnerAdapter(searchFormatItems)
        browseStatusSpinner.adapter = makeSpinnerAdapter(searchStatusItems)
        browseSourceSpinner.adapter = makeSpinnerAdapter(searchSourceItems)
        browseCountrySpinner.adapter = makeSpinnerAdapter(searchCountryItems)

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
        tagGenreRecyclerView.layoutManager  = FlexboxLayoutManager(context)
        streamingOnRecyclerView.layoutManager  = FlexboxLayoutManager(context)
        tagExcludeRecyclerView.layoutManager  = FlexboxLayoutManager(context)
        genreExcludeRecyclerView.layoutManager  = FlexboxLayoutManager(context)
    }

    private fun View.updateTheme() {
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
            tagExcludeFrameLayout.setBackgroundColor(it)
            genreExcludeFrameLayout.setBackgroundColor(it)
            tagAddIv.background = rippleDrawable
            genreAddIv.background = rippleDrawable
            streamAddIv.background = rippleDrawable
            tagExcludeAddIv.background = rippleDrawable
            genreExcludeAddIv.background = rippleDrawable
            browseYearSeekBar.setIndicatorTextDecimalFormat("0")
            browseYearSeekBar.setTypeface(
                ResourcesCompat.getFont(
                    context,
                    R.font.open_sans_light
                )
            )

        }

        DynamicTheme.getInstance().get().accentColor.let {
            browseYearSeekBar.progressColor = it
            browseYearSeekBar.leftSeekBar?.indicatorBackgroundColor = it
            browseYearSeekBar.rightSeekBar?.indicatorBackgroundColor = it
        }
    }

    private fun View.updateListener() {
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

        tagExcludeAddIv.setOnClickListener {
            mListener?.openTagChooserDialog(
                tagExcludedTagMap!!.values.toList(),
                MediaTagFilterTypes.TAG_EXCLUDE
            )
        }

        genreExcludeAddIv.setOnClickListener {
            mListener?.openTagChooserDialog(
                genreExcludedTagMap!!.values.toList(),
                MediaTagFilterTypes.GENRE_EXCLUDE
            )
        }

        applyFilterCardView.setOnClickListener {
            context.hideKeyboard(browseSearchEt)
            applyFilter()
        }
    }

    private fun View.makeSpinnerAdapter(items: List<DynamicSpinnerItem>) =
        DynamicSpinnerImageAdapter(
            context,
            R.layout.ads_layout_spinner_item,
            R.id.ads_spinner_item_icon,
            R.id.ads_spinner_item_text, items
        )


    fun getFilter(): SearchFilterModel {
        return when (browseTypeSpinner.selectedItemPosition) {
            SearchTypes.ANIME.ordinal, SearchTypes.MANGA.ordinal -> {
                MediaSearchFilterModel().apply {
                    query = searchText
                    season = browseSeasonSpinner?.selectedItemPosition?.minus(1)
                        ?.takeIf { it >= 0 }

                    type = browseTypeSpinner.selectedItemPosition
                    yearEnabled = enableYearCheckBox.isChecked
                    if (yearEnabled) {
                        minYear = browseYearSeekBar?.leftSeekBar?.progress?.let {
                            ceil(it).toInt()
                        }
                        maxYear = browseYearSeekBar?.rightSeekBar?.progress?.let {
                            ceil(it).toInt()
                        }
                    }
                    sort = browseSortSpinner?.selectedItemPosition?.minus(1)?.takeIf {
                        it >= 0
                    }
                    format = browseFormatSpinner?.selectedItemPosition?.minus(1)?.takeIf {
                        it >= 0
                    }
                    status = browseStatusSpinner?.selectedItemPosition?.minus(1)?.takeIf {
                        it >= 0
                    }
                    countryOfOrigin = browseCountrySpinner?.selectedItemPosition?.minus(1)?.takeIf {
                        it >= 0
                    }
                    source = browseSourceSpinner?.selectedItemPosition?.minus(1)?.takeIf {
                        it >= 0
                    }
                    streamingOn = streamingTagMap!!.values.filter { it.isTagged }.map { it.tag }
                    genre = genreTagMap!!.values.filter { it.isTagged }.map { it.tag }
                    tags = tagTagMap!!.values.filter { it.isTagged }.map { it.tag }
                    tagsToExclude = tagExcludedTagMap!!.values.filter { it.isTagged }.map { it.tag }
                    genreToExclude =
                        genreExcludedTagMap!!.values.filter { it.isTagged }.map { it.tag }
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
        browseTypeSpinner?.setSelection(value.type)
        when (value) {
            is MediaSearchFilterModel -> {
                value.let {
                    searchText = value.query
                    it.season?.let {
                        browseSeasonSpinner?.setSelection(it + 1)
                    }
                    enableYearCheckBox.isChecked = it.yearEnabled
                    browseYearSeekBar.isEnabled = it.yearEnabled

                    if (it.minYear != null && it.maxYear != null)
                        browseYearSeekBar?.setProgress(
                            it.minYear!!.toFloat(),
                            it.maxYear!!.toFloat()
                        )
                    it.sort?.let {
                        browseSortSpinner?.setSelection(it + 1)
                    }
                    it.format?.let {
                        browseFormatSpinner?.setSelection(it + 1)
                    }
                    it.status?.let {
                        browseStatusSpinner?.setSelection(it + 1)
                    }
                    it.streamingOn?.mapNotNull {
                        streamingTagMap!![it]?.isTagged = true
                        streamingTagMap!![it]
                    }?.let {
                        mListener?.onTagAdd(it, MediaTagFilterTypes.STREAMING_ON)
                    }

                    it.countryOfOrigin?.let {
                        browseCountrySpinner.setSelection(it + 1)
                    }
                    it.source?.let {
                        browseSourceSpinner.setSelection(it + 1)
                    }
                    it.genre?.mapNotNull {
                        genreTagMap!![it]?.isTagged = true
                        genreTagMap!![it]
                    }?.let {
                        mListener?.onTagAdd(it, MediaTagFilterTypes.GENRES)
                    }

                    it.tags?.mapNotNull {
                        tagTagMap!![it]?.isTagged = true
                        tagTagMap!![it]
                    }?.let {
                        mListener?.onTagAdd(it, MediaTagFilterTypes.TAGS)
                    }

                    it.tagsToExclude?.mapNotNull {
                        tagExcludedTagMap!![it]?.isTagged = true
                        tagExcludedTagMap!![it]
                    }?.let {
                        mListener?.onTagAdd(it, MediaTagFilterTypes.TAG_EXCLUDE)
                    }

                    it.genreToExclude?.mapNotNull {
                        genreExcludedTagMap!![it]?.isTagged = true
                        genreExcludedTagMap!![it]
                    }?.let {
                        mListener?.onTagAdd(it, MediaTagFilterTypes.GENRE_EXCLUDE)
                    }

                    mListener?.updateTags(MediaTagFilterTypes.TAGS)
                    mListener?.updateTags(MediaTagFilterTypes.GENRES)
                    mListener?.updateTags(MediaTagFilterTypes.STREAMING_ON)
                    mListener?.updateTags(MediaTagFilterTypes.TAG_EXCLUDE)
                    mListener?.updateTags(MediaTagFilterTypes.GENRE_EXCLUDE)
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