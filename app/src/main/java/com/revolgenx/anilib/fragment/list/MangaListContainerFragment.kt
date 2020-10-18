package com.revolgenx.anilib.fragment.list

import androidx.navigation.fragment.navArgs
import com.revolgenx.anilib.meta.MediaListMeta

class MangaListContainerFragment : MediaListContainerFragment() {
    private val mangaListMetaArgs: AnimeListContainerFragmentArgs by navArgs()
    override fun mediaListMetaArgs(): MediaListMeta = mangaListMetaArgs.mediaListMeta
}