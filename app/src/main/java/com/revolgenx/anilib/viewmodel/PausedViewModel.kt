package com.revolgenx.anilib.viewmodel

import com.revolgenx.anilib.service.media.MediaListEntryService
import com.revolgenx.anilib.service.list.MediaListService

class PausedViewModel(mediaListService: MediaListService, entryService: MediaListEntryService) :
    MediaListViewModel(mediaListService, entryService) {

}