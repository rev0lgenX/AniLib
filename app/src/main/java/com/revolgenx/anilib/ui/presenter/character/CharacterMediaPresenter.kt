package com.revolgenx.anilib.ui.presenter.character

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.otaliastudios.elements.Element
import com.otaliastudios.elements.Page
import com.revolgenx.anilib.R
import com.revolgenx.anilib.infrastructure.event.BrowseMediaEvent
import com.revolgenx.anilib.infrastructure.event.ListEditorEvent
import com.revolgenx.anilib.data.meta.ListEditorMeta
import com.revolgenx.anilib.data.meta.MediaBrowserMeta
import com.revolgenx.anilib.data.model.character.CharacterMediaModel
import com.revolgenx.anilib.common.preference.loggedIn
import com.revolgenx.anilib.databinding.CharacterMediaPresenterBinding
import com.revolgenx.anilib.ui.presenter.BasePresenter
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.naText

class CharacterMediaPresenter(context: Context) : BasePresenter<CharacterMediaPresenterBinding, CharacterMediaModel>(context) {
    override val elementTypes: Collection<Int>
        get() = listOf(0)

    private val mediaFormatList by lazy {
        context.resources.getStringArray(R.array.media_format)
    }

    private val mediaStatus by lazy {
        context.resources.getStringArray(R.array.media_status)
    }
    private val statusColors by lazy {
        context.resources.getStringArray(R.array.status_color)
    }

    override fun bindView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        elementType: Int
    ): CharacterMediaPresenterBinding {
        return CharacterMediaPresenterBinding.inflate(inflater, parent, false)
    }

    override fun onBind(page: Page, holder: Holder, element: Element<CharacterMediaModel>) {
        super.onBind(page, holder, element)
        val item = element.data ?: return

        holder.getBinding()?.apply {
            characterMediaImageView.setImageURI(item.coverImage?.image(context))
            characterMediaTitleTv.text = item.title?.title(context)
            characterMediaFormatTv.text =
                context.getString(R.string.media_format_year_s).format(item.format?.let {
                    mediaFormatList[it]
                }.naText(), item.seasonYear?.toString().naText())
            characterMediaFormatTv.status = item.mediaEntryListModel?.status
            characterMediaStatusTv.text = item.status?.let {
                characterMediaStatusTv.color = Color.parseColor(statusColors[it])
                mediaStatus[it]
            }.naText()

            characterMediaRatingTv.text = item.averageScore?.toString().naText()
            root.setOnClickListener {
                BrowseMediaEvent(
                    MediaBrowserMeta(
                        item.mediaId,
                        item.type!!,
                        item.title!!.romaji!!,
                        item.coverImage!!.image(context),
                        item.coverImage!!.largeImage,
                        item.bannerImage
                    ), characterMediaImageView
                ).postEvent
            }

            root.setOnLongClickListener {
                if (context.loggedIn()) {
                    ListEditorEvent(
                        ListEditorMeta(
                            item.mediaId,
                            item.type!!,
                            item.title!!.title(context)!!,
                            item.coverImage!!.image(context),
                            item.bannerImage
                        ), characterMediaImageView
                    ).postEvent
                } else {
                    context.makeToast(R.string.please_log_in, null, R.drawable.ic_person)
                }
                true
            }
        }

        item.title?.title(context)
    }
}
