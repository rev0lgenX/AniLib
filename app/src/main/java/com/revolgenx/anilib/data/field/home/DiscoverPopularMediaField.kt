package com.revolgenx.anilib.data.field.home

import android.content.Context
import com.revolgenx.anilib.data.field.media.MediaField
import com.revolgenx.anilib.common.preference.getPopularField
import com.revolgenx.anilib.common.preference.storePopularField

class PopularMediaField: MediaField() {
    companion object {
        fun create(context: Context) = getPopularField(context)
    }

    override var includeStaff: Boolean = true
    override var includeStudio: Boolean = true

    fun savePopularField(context: Context) {
        storePopularField(context, this)
    }
}