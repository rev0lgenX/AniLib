package com.revolgenx.anilib.data.field.home

import android.content.Context
import com.revolgenx.anilib.data.field.media.MediaField
import com.revolgenx.anilib.common.preference.getTrendingField
import com.revolgenx.anilib.common.preference.storeTrendingField

class TrendingMediaField : MediaField() {

    override var includeStaff: Boolean = true
    override var includeStudio: Boolean = true

    companion object {
        fun create(context: Context) = getTrendingField(context)
    }

    fun saveTrendingField(context: Context) {
        storeTrendingField(context, this)
    }
}