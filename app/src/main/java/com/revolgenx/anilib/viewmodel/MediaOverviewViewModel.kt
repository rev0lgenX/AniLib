package com.revolgenx.anilib.viewmodel

import android.os.Handler
import androidx.lifecycle.*
import com.revolgenx.anilib.model.MediaOverviewModel
import com.revolgenx.anilib.field.MediaOverviewField
import com.revolgenx.anilib.field.UpdateRecommendationField
import com.revolgenx.anilib.field.overview.MediaRecommendationField
import com.revolgenx.anilib.model.UpdateRecommendationModel
import com.revolgenx.anilib.repository.util.Resource
import com.revolgenx.anilib.service.MediaBrowseService
import com.revolgenx.anilib.service.RecommendationService
import com.revolgenx.anilib.source.BrowserOverviewRecommendationSource
import com.revolgenx.anilib.util.CommonTimer
import io.reactivex.disposables.CompositeDisposable

class MediaOverviewViewModel(
    private val mediaBrowseService: MediaBrowseService,
    private val recommendationService: RecommendationService
) : ViewModel() {

    private val compositeDisposable by lazy {
        CompositeDisposable()
    }

    private val handler by lazy {
        Handler()
    }

    var browserOverviewRecommendationSource: BrowserOverviewRecommendationSource? = null
    val mediaOverviewLiveData by lazy {
        MediatorLiveData<Resource<MediaOverviewModel>>().apply {
            addSource(mediaBrowseService.mediaOverviewLiveData) {res->
                res.data?.airingTimeModel?.let {
                    it.commonTimer = CommonTimer(handler, it.airingTime!!)
                }
                this.value = res
            }
        }
    }

    fun getOverview(field: MediaOverviewField) {
        mediaOverviewLiveData.value = Resource.loading(null)
        mediaBrowseService.getMediaOverview(field, compositeDisposable)
    }

    fun createRecommendationSource(
        field: MediaRecommendationField,
        lifecycleOwner: LifecycleOwner
    ): BrowserOverviewRecommendationSource {
        browserOverviewRecommendationSource = BrowserOverviewRecommendationSource(
            recommendationService,
            field,
            lifecycleOwner,
            compositeDisposable
        )
        return browserOverviewRecommendationSource!!
    }

    fun removeUpdateRecommendationObserver(observer: Observer<Resource<UpdateRecommendationModel>>) {
        recommendationService.removeUpdateRecommendationObserver(observer)
    }


    fun updateRecommendation(field: UpdateRecommendationField): MutableLiveData<Resource<UpdateRecommendationModel>> {
        return recommendationService.updateRecommendation(field, compositeDisposable)
    }

    override fun onCleared() {
        handler.removeCallbacksAndMessages(null)
        super.onCleared()
    }

}