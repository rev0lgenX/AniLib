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
import com.revolgenx.anilib.data.model.MediaRelationshipModel
import com.revolgenx.anilib.databinding.BrowserRelationshipPresenterLayoutBinding
import com.revolgenx.anilib.util.naText

class BrowseRelationshipPresenter(context: Context) : BasePresenter<BrowserRelationshipPresenterLayoutBinding, MediaRelationshipModel>(context) {
    override val elementTypes: Collection<Int>
        get() = listOf(0)

    private val statusColors by lazy {
        context.resources.getStringArray(R.array.status_color)
    }

    override fun bindView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        elementType: Int
    ): BrowserRelationshipPresenterLayoutBinding {
        return BrowserRelationshipPresenterLayoutBinding.inflate(inflater, parent, false)
    }

    override fun onBind(page: Page, holder: Holder, element: Element<MediaRelationshipModel>) {
        super.onBind(page, holder, element)
        val item = element.data ?: return
        holder.getBinding()?.apply {
            relationshipTitleTv.text = item.title!!.title(context)
            relationshipCoverImage.setImageURI(item.coverImage?.largeImage)
            relationshipMediaRatingTv.text = item.averageScore?.toString().naText()
            mediaSourceSeasonYearTv.text =
                context.getString(R.string.source_seasonyear_s).format(
                    item.relationshipType?.let { context.resources.getStringArray(R.array.media_relation)[it] }.naText()
                    , item.seasonYear?.toString().naText()
                )

            mediaFormatStatusTv.text = context.getString(R.string.str_dot_str).format(
                item.format?.let { context.resources.getStringArray(R.array.media_format)[it] }.naText(),
                item.status?.let { context.resources.getStringArray(R.array.media_status)[it] }.naText()
            )
            root.setOnClickListener {
                BrowseMediaEvent(
                    MediaBrowserMeta(
                        item.mediaId,
                        item.type!!,
                        item.title!!.romaji!!,
                        item.coverImage!!.image(context),
                        item.coverImage!!.largeImage,
                        item.bannerImage
                    ), relationshipCoverImage
                ).postEvent
            }

            item.status?.let {
                statusDivider.setBackgroundColor(Color.parseColor(statusColors[it]))
            }
        }

    }

}