package com.revolgenx.anilib.viewmodel.media_list

import androidx.lifecycle.viewModelScope
import com.revolgenx.anilib.field.MediaListCollectionFilterField
import com.revolgenx.anilib.field.list.MediaListCollectionField
import com.revolgenx.anilib.model.EntryListEditorMediaModel
import com.revolgenx.anilib.model.list.MediaListModel
import com.revolgenx.anilib.repository.util.Resource
import com.revolgenx.anilib.service.list.MediaListService
import com.revolgenx.anilib.service.media.MediaListEntryService
import com.revolgenx.anilib.source.media_list.MediaListCollectionSource
import com.revolgenx.anilib.ui.sorting.MediaListSorting
import com.revolgenx.anilib.ui.sorting.makeMediaListSortingComparator
import com.revolgenx.anilib.viewmodel.SourceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
* todo addmedialist might not update on rotaion rare case
* */
abstract class MediaListCollectionViewModel(
    private val mediaListService: MediaListService,
    private val entryService: MediaListEntryService
) :
    SourceViewModel<MediaListCollectionSource, MediaListCollectionField>() {
    var filteredList: MutableList<MediaListModel>? = null
    val listMap by lazy { mutableMapOf<Int, MediaListModel>() }
    var filter = MediaListCollectionFilterField()

    override var field: MediaListCollectionField = MediaListCollectionField()

    override fun createSource(): MediaListCollectionSource {
        filteredList = null
        if (source == null) {
            source =
                MediaListCollectionSource(
                    field,
                    listMap,
                    mediaListService,
                    compositeDisposable
                )
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                filteredList = if (filter.format == null) listMap.values else {
                    listMap.values.filter { it.format == filter.format }
                }.let {
                    if (filter.status == null) it else it.filter { it.status == filter.status }
                }.let {
                    if (filter.genre == null) it else it.filter { it.genres?.contains(filter.genre!!) == true }
                }.let {
                    if (filter.search.isNullOrEmpty()) it else {
                        it.filter {
                            it.title?.userPreferred?.contains(
                                filter.search!!, true
                            ) == true
                        }
                    }
                }.let {
                    if (filter.listSort == null) it else it.sortedWith(
                        makeMediaListSortingComparator(
                            MediaListSorting.MediaListSortingType.values()[filter.listSort!!]
                        )
                    )
                }.toMutableList()

                launch(Dispatchers.Main) {
                    filteredList?.let {
                        source?.filterPage(it)
                    }
                }
            }
        }
        return source!!
    }


    fun increaseProgress(
        model: EntryListEditorMediaModel,
        callback: (Resource<EntryListEditorMediaModel>) -> Unit
    ) {
        entryService.increaseProgress(model, compositeDisposable, callback)
    }


    override fun onCleared() {
        listMap.clear()
        filteredList = null
        compositeDisposable.clear()
        super.onCleared()
    }

    fun renewSource() {
        source = null
        filteredList = null
        listMap.clear()
    }
}
