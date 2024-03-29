package com.revolgenx.anilib.user.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import com.otaliastudios.elements.Presenter
import com.otaliastudios.elements.Source
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.ui.fragment.BasePresenterToolbarFragment
import com.revolgenx.anilib.list.viewmodel.AnimeListCollectionStoreVM
import com.revolgenx.anilib.media.data.model.MediaModel
import com.revolgenx.anilib.media.presenter.MediaListingPresenter
import com.revolgenx.anilib.media.viewmodel.MediaListingViewModel
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaListingFragment:BasePresenterToolbarFragment<MediaModel>() {
    override val basePresenter: Presenter<MediaModel>
        get() = MediaListingPresenter(requireContext())
    override val baseSource: Source<MediaModel>
        get() = viewModel.source?:createSource()

    private val viewModel by viewModel<MediaListingViewModel>()
    override val titleRes: Int = R.string.media

    private val mediaIds get() = arguments?.getIntArray(MEDIA_LISTING_IDS_KEY)?.toList()
    override var gridMaxSpan: Int = 6
    override var gridMinSpan: Int = 3

    companion object{
        private const val MEDIA_LISTING_IDS_KEY = "MEDIA_LISTING_IDS_KEY"
        fun newInstance(mediaIds:List<Int>) = MediaListingFragment().also {
            it.arguments = bundleOf(MEDIA_LISTING_IDS_KEY to mediaIds.toIntArray())
        }
    }

    override fun createSource(): Source<MediaModel> {
        getSharedViewModel<AnimeListCollectionStoreVM>()
        return viewModel.createSource()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.field.mediaIdsIn = mediaIds?:return
    }

}