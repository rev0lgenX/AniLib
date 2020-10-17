package com.revolgenx.anilib.field.search

import com.revolgenx.anilib.MediaSearchQuery
import com.revolgenx.anilib.constant.SearchTypes
import com.revolgenx.anilib.constant.CountryOfOrigins
import com.revolgenx.anilib.type.*

class MediaSearchField : SearchField() {

    override var type: Int = SearchTypes.ANIME.ordinal
    var season: Int? = null
    var minYear: Int? = null
    var maxYear: Int? = null
    var yearEnabled = false
    val isYearSame
        get() = minYear == maxYear
    var sort: Int? = null

    var format: Int? = null
    var status: Int? = null
    var streamingOn: List<String>? = null
    var countryOfOrigin: Int? = null
    var source: Int? = null
    var genre: List<String>? = null
    var tags: List<String>? = null
    var tagsNotIn: List<String>? = null
    var genreNotIn: List<String>? = null
    var hentaiOnly = false

    override fun toQueryOrMutation(): Any {
        return MediaSearchQuery.builder()
            .page(page)
            .perPage(perPage)
            .apply {

                if (!query.isNullOrEmpty()) {
                    search(query)
                }

                season?.let {
                    season(MediaSeason.values()[it])
                }

                if (yearEnabled) {
                    if (isYearSame) {
                        minYear?.let {
                            seasonYear(it)
                        }
                    } else {
                        minYear?.let {
                            yearGreater(it * 10000)
                        }
                        maxYear?.let {
                            yearLesser(it * 10000)
                        }
                    }
                }

                when (type) {
                    SearchTypes.ANIME.ordinal -> {
                        type(MediaType.ANIME)
                    }
                    SearchTypes.MANGA.ordinal -> {
                        type(MediaType.MANGA)
                    }
                }

                format?.let {
                    format(MediaFormat.values()[it])
                }
                season?.let {
                    season(MediaSeason.values()[it])
                }
                status?.let {
                    status(MediaStatus.values()[it])
                }
                source?.let {
                    source(MediaSource.values()[it])
                }

                streamingOn?.takeIf { it.isNotEmpty() }?.let {
                    streamingOn(it)
                }

                genre?.takeIf { it.isNotEmpty() }?.let {
                    genre(it)
                }

                tags?.takeIf { it.isNotEmpty() }?.let {
                    tag(it)
                }

                tagsNotIn?.let {
                    tagNotIn(it)
                }

                genreNotIn?.let {
                    genreNotIn(it)
                }

                sort?.let {
                    sort(listOf(MediaSort.values()[it]))
                }

                countryOfOrigin?.let {
                    country(CountryOfOrigins.values()[it].name)
                }

                if(!canShowAdult){
                    isAdult(canShowAdult)
                }

                if(hentaiOnly && canShowAdult){
                    isAdult(hentaiOnly)
                }

            }
            .build()
    }

}
