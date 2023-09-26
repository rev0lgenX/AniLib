package com.revolgenx.anilib.list.ui.viewmodel

import com.revolgenx.anilib.common.data.store.AuthDataStore
import com.revolgenx.anilib.common.data.store.MediaListFilterDataStore
import com.revolgenx.anilib.list.data.field.MediaListCollectionField
import com.revolgenx.anilib.list.data.service.MediaListService
import com.revolgenx.anilib.type.MediaType

class MangaListViewModel(
    mediaListService: MediaListService,
    authDataStore: AuthDataStore,
    mediaListFilterDataStore: MediaListFilterDataStore
) : MediaListViewModel(mediaListService, authDataStore, mediaListFilterDataStore) {
    override val field: MediaListCollectionField = MediaListCollectionField(MediaType.MANGA)
}