package com.revolgenx.anilib.home.discover.viewmodel

import android.content.Context
import com.revolgenx.anilib.common.preference.getTrendingField
import com.revolgenx.anilib.home.discover.data.field.TrendingMediaField
import com.revolgenx.anilib.infrastructure.service.media.MediaService
import com.revolgenx.anilib.type.MediaSort

class DiscoverTrendingViewModel(mediaService: MediaService) : BaseDiscoverViewModel<TrendingMediaField>(mediaService) {

    override var field: TrendingMediaField = TrendingMediaField().also {
        it.sort = MediaSort.TRENDING_DESC.ordinal
    }

    fun updateField() {
        getTrendingField().let {
            field.season = it.season
            field.year = it.year
            field.format = it.format
            field.status = it.status
            field.formatsIn = it.formatsIn
        }
    }
}