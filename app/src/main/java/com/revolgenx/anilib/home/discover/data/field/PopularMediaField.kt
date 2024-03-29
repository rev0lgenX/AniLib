package com.revolgenx.anilib.home.discover.data.field

import com.revolgenx.anilib.media.data.field.MediaField
import com.revolgenx.anilib.common.preference.getPopularField
import com.revolgenx.anilib.common.preference.storePopularField

class PopularMediaField: MediaField() {
    companion object {
        fun create() = getPopularField()
    }

    override var includeStaff: Boolean = true
    override var includeStudio: Boolean = true

    fun savePopularField() {
        storePopularField(this)
    }
}