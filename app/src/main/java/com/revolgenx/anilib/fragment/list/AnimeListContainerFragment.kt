package com.revolgenx.anilib.fragment.list

import androidx.navigation.fragment.navArgs
import com.revolgenx.anilib.meta.MediaListMeta

class AnimeListContainerFragment : MediaListContainerFragment() {
    private val animeListMetaArgs: AnimeListContainerFragmentArgs by navArgs()

    override fun mediaListMetaArgs(): MediaListMeta = animeListMetaArgs.mediaListMeta
}