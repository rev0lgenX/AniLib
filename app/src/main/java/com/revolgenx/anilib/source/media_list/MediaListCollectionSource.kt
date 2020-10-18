package com.revolgenx.anilib.source.media_list

import com.otaliastudios.elements.Element
import com.otaliastudios.elements.Page
import com.revolgenx.anilib.field.list.MediaListCollectionField
import com.revolgenx.anilib.model.list.MediaListModel
import com.revolgenx.anilib.repository.util.Resource
import com.revolgenx.anilib.repository.util.Status
import com.revolgenx.anilib.service.list.MediaListService
import com.revolgenx.anilib.source.BaseRecyclerSource
import io.reactivex.disposables.CompositeDisposable

class MediaListCollectionSource(
    field: MediaListCollectionField,
    private val listMap: MutableMap<Int, MediaListModel>,
    private val mediaListService: MediaListService,
    private val compositeDisposable: CompositeDisposable
) : BaseRecyclerSource<MediaListModel, MediaListCollectionField>(field) {


    private lateinit var firstPage: Page
    override fun areItemsTheSame(first: MediaListModel, second: MediaListModel): Boolean {
        return first.baseId == second.baseId
    }

    fun filterPage(filterList: MutableList<MediaListModel>) {
        if (::firstPage.isInitialized)
            postResult(firstPage, filterList)
    }

    override fun onPageOpened(page: Page, dependencies: List<Element<*>>) {
        super.onPageOpened(page, dependencies)
        if (page.isFirstPage()) {
            firstPage = page
            if (listMap.isEmpty()) {
                mediaListService.getMediaListCollection(field, compositeDisposable) {
                    postResult(page, it)

                    if (it.status == Status.SUCCESS) {
                        (it.data as List<MediaListModel>).forEach {
                            listMap[it.mediaId!!] = it
                        }
                    }
                }
            } else {
                postResult(page, Resource.success(listMap.toList()))
            }
        } else {
            postResult(page, emptyList<MediaListModel>())
        }
    }
}
