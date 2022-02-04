package com.revolgenx.anilib.home.season.data.field

import android.content.Context
import com.revolgenx.anilib.MediaQuery
import com.revolgenx.anilib.common.data.field.TagField
import com.revolgenx.anilib.media.data.field.MediaField
import com.revolgenx.anilib.common.preference.getSeasonField
import com.revolgenx.anilib.common.preference.storeSeasonField
import com.revolgenx.anilib.common.preference.storeSeasonGenre
import com.revolgenx.anilib.common.preference.storeSeasonTag
import com.revolgenx.anilib.type.MediaFormat
import com.revolgenx.anilib.type.MediaSeason
import com.revolgenx.anilib.type.MediaSort
import com.revolgenx.anilib.type.MediaStatus


class SeasonField : MediaField() {
    companion object {
        fun create(context: Context) = getSeasonField(context)
    }

    var tagTagFields = mutableMapOf<String, TagField>()
    var genreTagFields = mutableMapOf<String, TagField>()

    var showFormatHeader:Boolean = true

    fun saveSeasonField(context: Context) {
        storeSeasonField(context, this)
    }

    fun saveGenre(context: Context) {
        storeSeasonGenre(context, this)
    }

    fun saveTags(context: Context) {
        storeSeasonTag(context, this)
    }

    fun nextSeason(context: Context) {
        if (season == null) return
        season = season!! + 1
        if (season!! > 3) {
            seasonYear = seasonYear!! + 1
            season = 0
        }
        saveSeasonField(context)
    }

    fun previousSeason(context: Context) {
        if (season == null) return
        season = season!! - 1
        if (season!! < 0) {
            seasonYear = seasonYear!! - 1
            season = 3
        }
        saveSeasonField(context)
    }

    fun updateFields(context: Context) {
        getSeasonField(context).also {
            this.season = it.season
            this.seasonYear = it.seasonYear
            this.tags = it.tags
            this.genres = it.genres
            this.format = it.format
            this.formatsIn = it.formatsIn
            this.sort = it.sort
            this.status = it.status
            this.showFormatHeader = it.showFormatHeader
        }
    }


    override fun toQueryOrMutation(): MediaQuery {

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

        val mediaSort = if (showFormatHeader) {
            if (sort != null) {
                listOf(MediaSort.FORMAT, MediaSort.values()[sort!!])
            } else {
                listOf(MediaSort.FORMAT)
            }
        } else {
            sort?.let {
                listOf(MediaSort.values()[it])
            }
        }

        return MediaQuery(
            page = nn(page),
            perPage = nn(perPage),
            season = nn(mediaSeason),
            seasonYear = nn(seasonYear),
            sort = nn(mediaSort),
            format_in = nn(mediaFormatsIn),
            tag_in = nn(tags),
            idIn = nn(mediaIdsIn),
            isAdult = nn(canShowAdult.takeIf { it.not() }),
            format = nn(mediaFormat),
            status =  nn(mediaStatus),
            includeStaff = includeStaff,
            includeStudio = includeStudio,
            genre_in = nn(genres)
        )
    }


}
