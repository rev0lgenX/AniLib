package com.revolgenx.anilib.ui.fragment.browse

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.otaliastudios.elements.Presenter
import com.otaliastudios.elements.Source
import com.revolgenx.anilib.activity.MediaBrowseActivity
import com.revolgenx.anilib.data.field.media.MediaStaffField
import com.revolgenx.anilib.common.ui.fragment.BasePresenterFragment
import com.revolgenx.anilib.data.meta.MediaBrowserMeta
import com.revolgenx.anilib.data.model.MediaStaffModel
import com.revolgenx.anilib.ui.presenter.media.MediaStaffPresenter
import com.revolgenx.anilib.ui.viewmodel.media.MediaStaffViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaStaffFragment : BasePresenterFragment<MediaStaffModel>() {

    override val basePresenter: Presenter<MediaStaffModel>
        get() {
            return MediaStaffPresenter(
                requireContext()
            )
        }

    override val baseSource: Source<MediaStaffModel>
        get() {
            return viewModel.staffSource ?: createSource()
        }

    private val viewModel by viewModel<MediaStaffViewModel>()
    private var mediaBrowserMeta: MediaBrowserMeta? = null
    private val field by lazy {
        MediaStaffField().also {
            it.mediaId = mediaBrowserMeta?.mediaId ?: -1
        }
    }

    override fun createSource(): Source<MediaStaffModel> {
        return viewModel.createSource(field)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val span =
            if (requireContext().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 6 else 3
        layoutManager =
            GridLayoutManager(
                this.context,
                span
            ).also {
                it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (adapter?.getItemViewType(position) == 0) {
                            1
                        } else {
                            span
                        }
                    }
                }
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        mediaBrowserMeta =
            arguments?.getParcelable(MediaBrowseActivity.MEDIA_BROWSER_META) ?: return
        super.onActivityCreated(savedInstanceState)
    }


}
