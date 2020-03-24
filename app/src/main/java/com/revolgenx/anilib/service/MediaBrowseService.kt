package com.revolgenx.anilib.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.revolgenx.anilib.model.*
import com.revolgenx.anilib.field.MediaOverviewField
import com.revolgenx.anilib.field.overview.*
import com.revolgenx.anilib.model.stats.MediaStatsModel
import com.revolgenx.anilib.repository.network.BaseGraphRepository
import com.revolgenx.anilib.repository.util.Resource
import io.reactivex.disposables.CompositeDisposable

typealias MutableOverviewType = MutableLiveData<Resource<MediaOverviewModel>>
typealias MutableStatsType = MutableLiveData<Resource<MediaStatsModel>>

abstract class MediaBrowseService(protected val graphRepository: BaseGraphRepository) {
    val mediaOverviewLiveData by lazy {
        MutableOverviewType()
    }

    val mediaStatsLiveData by lazy {
        MutableStatsType()
    }

    val simpleMediaLiveData by lazy {
        MutableLiveData<Resource<MediaBrowseMediaModel>>()
    }

    abstract fun getSimpleMedia(
        mediaId: Int,
        compositeDisposable: CompositeDisposable
    ): LiveData<Resource<MediaBrowseMediaModel>>

    abstract fun getMediaOverview(
        field: MediaOverviewField,
        compositeDisposable: CompositeDisposable? = null
    ): MutableOverviewType

    abstract fun getMediaWatch(
        field: MediaWatchField,
        compositeDisposable: CompositeDisposable? = null,
        resourceCallback: (Resource<List<MediaWatchModel>>) -> Unit
    )

    abstract fun getMediaCharacter(
        field: MediaCharacterField,
        compositeDisposable: CompositeDisposable? = null,
        resourceCallback: (Resource<List<MediaCharacterModel>>) -> Unit
    )

    abstract fun getMediaStaff(
        field: MediaStaffField,
        compositeDisposable: CompositeDisposable? = null,
        resourceCallback: (Resource<List<MediaStaffModel>>) -> Unit
    )


    abstract fun getMediaReview(
        field: MediaReviewField,
        compositeDisposable: CompositeDisposable? = null,
        resourceCallback: (Resource<List<MediaReviewModel>>) -> Unit
    )


    abstract fun getMediaStats(
        field: MediaStatsField,
        compositeDisposable: CompositeDisposable? = null
    ): MutableStatsType

}