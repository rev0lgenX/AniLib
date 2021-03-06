package com.revolgenx.anilib.infrastructure.service.list

import com.revolgenx.anilib.data.field.list.MediaListCollectionField
import com.revolgenx.anilib.data.field.list.MediaListCollectionIdsField
import com.revolgenx.anilib.data.field.list.MediaListCountField
import com.revolgenx.anilib.data.field.list.MediaListField
import com.revolgenx.anilib.data.model.list.MediaListCountTypeModel
import com.revolgenx.anilib.data.model.list.MediaListModel
import com.revolgenx.anilib.infrastructure.repository.util.Resource
import io.reactivex.disposables.CompositeDisposable

interface MediaListService {
    fun getMediaListCollection(
        field: MediaListCollectionField,
        compositeDisposable: CompositeDisposable,
        resourceCallback: (Resource<List<MediaListModel>>) -> Unit
    )

    fun getMediaListCollectionIds(
        field: MediaListCollectionIdsField,
        compositeDisposable: CompositeDisposable,
        resourceCallback: (Resource<List<Int>>) -> Unit
    )

    fun getMediaList(
        field: MediaListField,
        compositeDisposable: CompositeDisposable,
        callback: (Resource<List<MediaListModel>>) -> Unit
    )

    fun getMediaListCount(
        field:MediaListCountField,
        compositeDisposable: CompositeDisposable,
        callback: (Resource<List<MediaListCountTypeModel>>) -> Unit
    )
}
