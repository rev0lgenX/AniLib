package com.revolgenx.anilib.media.viewmodel

import androidx.lifecycle.MutableLiveData
import com.revolgenx.anilib.common.data.field.ToggleFavouriteField
import com.revolgenx.anilib.common.viewmodel.BaseViewModel
import com.revolgenx.anilib.infrastructure.repository.util.Resource
import com.revolgenx.anilib.media.service.MediaInfoService
import com.revolgenx.anilib.infrastructure.service.favourite.FavouriteService
import com.revolgenx.anilib.infrastructure.service.toggle.ToggleService
import com.revolgenx.anilib.list.data.model.MediaListModel
import com.revolgenx.anilib.media.data.model.MediaModel

class MediaInfoViewModel(
    private val mediaBrowseService: MediaInfoService,
    private val toggleService: ToggleService,
    private val favouriteService: FavouriteService
) : BaseViewModel() {

    val mediaLiveData = MutableLiveData<Resource<MediaModel>>()
    val isFavLiveData = MutableLiveData<Resource<Boolean>>()

    val toggleFavMediaLiveData = MutableLiveData<Resource<Boolean>>()
    val saveMediaListEntryLiveData = MutableLiveData<Resource<MediaListModel>>()

    fun saveMediaListEntry(model: MediaListModel) {
        saveMediaListEntryLiveData.value = Resource.loading(null)

        //TODO update entry list
//        mediaListEntryService.saveMediaListEntry(model,compositeDisposable){
//            saveMediaListEntryLiveData.value = it
//        }
    }

    fun getMediaInfo(mediaId: Int?) {
        mediaBrowseService.getSimpleMedia(mediaId, compositeDisposable) {
            mediaLiveData.value = it
        }
    }

    fun isFavourite(mediaId: Int?){
        favouriteService.isFavourite(mediaId, compositeDisposable){
            isFavLiveData.value = it
        }
    }


    fun toggleMediaFavourite(toggleFavouriteField: ToggleFavouriteField) {
        toggleFavMediaLiveData.value = Resource.loading(null)
        toggleService.toggleFavourite(toggleFavouriteField,compositeDisposable){
            toggleFavMediaLiveData.value = it
        }
    }
}