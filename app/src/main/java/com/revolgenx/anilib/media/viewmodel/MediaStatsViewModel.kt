package com.revolgenx.anilib.media.viewmodel

import androidx.lifecycle.MutableLiveData
import com.revolgenx.anilib.common.viewmodel.BaseViewModel
import com.revolgenx.anilib.media.data.field.MediaStatsField
import com.revolgenx.anilib.user.data.model.stats.MediaStatsModel
import com.revolgenx.anilib.common.repository.util.Resource
import com.revolgenx.anilib.media.service.MediaInfoService

class MediaStatsViewModel(private val browseService: MediaInfoService) : BaseViewModel() {
    val statsLiveData = MutableLiveData<Resource<MediaStatsModel>>()
    fun getStats(field: MediaStatsField) {
        statsLiveData.value = Resource.loading(null)
        browseService.getMediaStats(field, compositeDisposable){
            statsLiveData.value = it
        }
    }
}
