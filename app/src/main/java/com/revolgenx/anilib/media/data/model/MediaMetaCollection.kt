package com.revolgenx.anilib.media.data.model

import android.text.SpannableStringBuilder

class MediaMetaCollection() {
    lateinit var header: String
    var subTitle: String? = null
    var subTitleSpannable: SpannableStringBuilder? = null
}