package com.revolgenx.anilib.ui.presenter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.otaliastudios.elements.Element
import com.otaliastudios.elements.Page
import com.revolgenx.anilib.R
import com.revolgenx.anilib.infrastructure.event.BrowseMediaEvent
import com.revolgenx.anilib.data.meta.MediaBrowserMeta
import com.revolgenx.anilib.data.model.studio.StudioMediaModel
import com.revolgenx.anilib.databinding.StudioMediaPresenterBinding
import com.revolgenx.anilib.util.naText

class StudioMediaPresenter(context: Context) : BasePresenter<StudioMediaPresenterBinding, StudioMediaModel>(context) {
    override val elementTypes: Collection<Int>
        get() = listOf(0)

    private val statusColors by lazy {
        context.resources.getStringArray(R.array.status_color)
    }


    private val mediaFormats by lazy {
        context.resources.getStringArray(R.array.media_format)
    }

    private val mediaStatus by lazy {
        context.resources.getStringArray(R.array.media_status)
    }

    override fun bindView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        elementType: Int
    ): StudioMediaPresenterBinding {
        return StudioMediaPresenterBinding.inflate(inflater, parent, false)
    }

    override fun onBind(page: Page, holder: Holder, element: Element<StudioMediaModel>) {
        super.onBind(page, holder, element)
        val item = element.data ?: return

        holder.getBinding()?.apply {
            studioMediaImageView.setImageURI(item.coverImage?.image(context))
            studioMediaTitleTv.text = item.title?.title(context)
            studioMediaRatingTv.text = item.averageScore?.toString().naText()
            studioMediaFormatYearTv.text = context.getString(R.string.media_format_year_s).format(
                item.format?.let { mediaFormats[it] }.naText(), item.seasonYear?.toString().naText()
            )
            studioMediaStatusTv.text = item.status?.let {
                studioMediaStatusTv.color = Color.parseColor(statusColors[it])
                mediaStatus[it]
            }.naText()
            root.setOnClickListener {
                BrowseMediaEvent(
                    MediaBrowserMeta(
                        item.mediaId,
                        item.type!!,
                        item.title!!.romaji!!,
                        item.coverImage!!.image(context),
                        item.coverImage!!.largeImage,
                        item.bannerImage
                    ), studioMediaImageView
                ).postEvent
            }
        }
    }

}
