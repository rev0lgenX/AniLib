package com.revolgenx.anilib.data.model.entry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaEntryListModel(var progress: Int?, var status: Int?) : Parcelable
