package com.revolgenx.anilib.ui.viewmodel.list

import com.revolgenx.anilib.data.field.list.MediaListField
import com.revolgenx.anilib.entry.data.model.EntryListEditorMediaModel
import com.revolgenx.anilib.infrastructure.repository.util.Resource
import com.revolgenx.anilib.infrastructure.service.list.MediaListService
import com.revolgenx.anilib.entry.service.MediaEntryService
import com.revolgenx.anilib.infrastructure.source.media_list.MediaListSource
import com.revolgenx.anilib.common.viewmodel.SourceViewModel

open class MediaListViewModel(
    private val entryService: MediaEntryService,
    private val service: MediaListService
) : SourceViewModel<MediaListSource, MediaListField>() {
    override var field: MediaListField = MediaListField()

    override fun createSource(): MediaListSource {
        source = MediaListSource(field, service, compositeDisposable)
        return source!!
    }

    fun increaseProgress(
        model: EntryListEditorMediaModel,
        callback: (Resource<EntryListEditorMediaModel>) -> Unit
    ) {
        entryService.increaseProgress(model, compositeDisposable, callback)
    }

}