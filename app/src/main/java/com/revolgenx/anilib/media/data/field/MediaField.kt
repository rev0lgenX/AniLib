package com.revolgenx.anilib.media.data.field

import com.revolgenx.anilib.MediaQuery
import com.revolgenx.anilib.common.data.field.BaseSourceField
import com.revolgenx.anilib.common.preference.getExcludedGenre
import com.revolgenx.anilib.common.preference.getExcludedTags
import com.revolgenx.anilib.common.preference.includeExcludedGenre
import com.revolgenx.anilib.common.preference.includeExcludedTags
import com.revolgenx.anilib.type.MediaFormat
import com.revolgenx.anilib.type.MediaSeason
import com.revolgenx.anilib.type.MediaSort
import com.revolgenx.anilib.type.MediaStatus

open class MediaField : BaseSourceField<MediaQuery>() {
    var genres: List<String>? = null
    var tags: List<String>? = null

    var genreNotIn: List<String>? = null
    var tagNotIn: List<String>? = null

    var format: Int? = null
    var sort: Int? = null
    var seasonYear: Int? = null
    var year: Int? = null
    var season: Int? = null
    var status: Int? = null
    var mediaIdsIn: List<Int>? = null

    var formatsIn: MutableList<Int>? = null
    open var includeStaff = false
    open var includeStudio = false

    override fun toQueryOrMutation(): MediaQuery {
        val genreExcludedList = mutableListOf<String>()
        val tagExcludedList = mutableListOf<String>()

        genreNotIn?.let {
            genreExcludedList.addAll(it)
        }
        tagNotIn?.let {
            tagExcludedList.addAll(it)
        }

        if (includeExcludedGenre) {
            genreExcludedList.addAll(getExcludedGenre())
        }

        if (includeExcludedTags) {
            tagExcludedList.addAll(getExcludedTags())
        }

        val mediaSort = sort?.let {
            listOf(MediaSort.values()[it])
        }

        val mediaSeason = season?.let {
            MediaSeason.values()[it]
        }

        val mediaFormatsIn = formatsIn?.let {
            val formats = MediaFormat.values()
            it.map { formats[it] }
        }

        val mediaFormat = format?.let {
            MediaFormat.values()[it]
        }
        val mediaStatus = status?.let {
            MediaStatus.values()[it]
        }

        return MediaQuery(
            page = nn(page),
            perPage = nn(perPage),
            season = nn(mediaSeason),
            seasonYear = nn(seasonYear),
            year = nn(year?.let { "$it%" }),
            sort = nn(mediaSort),
            format_in = nn(mediaFormatsIn),
            idIn = nn(mediaIdsIn),
            isAdult = nn(canShowAdult.takeIf { it.not() }),
            format = nn(mediaFormat),
            status = nn(mediaStatus),
            includeStaff = includeStaff,
            includeStudio = includeStudio,
            genre_in = nn(genres),
            tag_in = nn(tags),
            genre_not_in = nn(genreExcludedList),
            tag_not_in = nn(tagExcludedList)
        )
    }

}
