package com.revolgenx.anilib.media.viewmodel

import androidx.lifecycle.ViewModel
import com.revolgenx.anilib.media.data.field.MediaStaffField
import com.revolgenx.anilib.media.service.MediaInfoService
import com.revolgenx.anilib.infrastructure.source.MediaStaffSource
import io.reactivex.disposables.CompositeDisposable

class MediaStaffViewModel(private val browseService: MediaInfoService) : ViewModel() {
    var staffSource: MediaStaffSource? = null
    private val compositeDisposable by lazy {
        CompositeDisposable()
    }

    fun createSource(field: MediaStaffField): MediaStaffSource {
        staffSource = MediaStaffSource(field, browseService, compositeDisposable)
        return staffSource!!
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}
