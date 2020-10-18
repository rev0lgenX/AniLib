package com.revolgenx.anilib.presenter.character

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.otaliastudios.elements.Element
import com.otaliastudios.elements.Page
import com.otaliastudios.elements.Presenter
import com.revolgenx.anilib.R
import com.revolgenx.anilib.event.BrowseMediaEvent
import com.revolgenx.anilib.event.ListEditorEvent
import com.revolgenx.anilib.meta.ListEditorMeta
import com.revolgenx.anilib.meta.MediaBrowserMeta
import com.revolgenx.anilib.model.character.CharacterMediaModel
import com.revolgenx.anilib.preference.loggedIn
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.naText
import kotlinx.android.synthetic.main.character_media_presenter.view.*

class CharacterMediaPresenter(context: Context) : Presenter<CharacterMediaModel>(context) {
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

    override fun onCreate(parent: ViewGroup, elementType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.character_media_presenter,
                parent,
                false
            )
        )
    }

    override fun onBind(page: Page, holder: Holder, element: Element<CharacterMediaModel>) {
        super.onBind(page, holder, element)
        val item = element.data ?: return

        holder.itemView.apply {
            characterMediaImageView.setImageURI(item.coverImage?.image(context))
            characterMediaTitleTv.text = item.title?.title(context)
            characterMediaFormatTv.text =
                context.getString(R.string.media_format_year_s).format(item.format?.let {
                    mediaFormatList[it]
                }.naText(), item.seasonYear?.toString().naText())

            characterMediaStatusTv.text = item.status?.let {
                characterMediaStatusTv.color = Color.parseColor(statusColors[it])
                mediaStatus[it]
            }.naText()

            characterMediaRatingTv.text = item.averageScore?.toString().naText()
            setOnClickListener {
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

            setOnLongClickListener {
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
