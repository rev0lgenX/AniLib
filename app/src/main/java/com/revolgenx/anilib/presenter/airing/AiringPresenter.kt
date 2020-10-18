package com.revolgenx.anilib.presenter.airing

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.otaliastudios.elements.Element
import com.otaliastudios.elements.Page
import com.otaliastudios.elements.Presenter
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.event.BrowseGenreEvent
import com.revolgenx.anilib.event.BrowseMediaEvent
import com.revolgenx.anilib.event.ListEditorEvent
import com.revolgenx.anilib.meta.ListEditorMeta
import com.revolgenx.anilib.meta.MediaBrowserMeta
import com.revolgenx.anilib.model.airing.AiringMediaModel
import com.revolgenx.anilib.model.search.filter.MediaSearchFilterModel
import com.revolgenx.anilib.preference.loggedIn
import com.revolgenx.anilib.type.MediaType
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.naText
import com.revolgenx.anilib.util.string
import kotlinx.android.synthetic.main.airing_presenter_layout.view.*

class AiringPresenter(context: Context) : Presenter<AiringMediaModel>(context) {
    override val elementTypes: Collection<Int>
        get() = listOf(0)

    override fun onCreate(parent: ViewGroup, elementType: Int): Holder {
        return Holder(getLayoutInflater().inflate(R.layout.airing_presenter_layout, parent, false))
    }

    private val statusColors by lazy {
        context.resources.getStringArray(R.array.status_color)
    }

    private val mediaFormats by lazy {
        context.resources.getStringArray(R.array.media_format)
    }

    private val mediaStatus by lazy {
        context.resources.getStringArray(R.array.media_status)
    }

    private val tintSurfaceColor by lazy {
        DynamicTheme.getInstance().get().tintSurfaceColor
    }


    override fun onBind(page: Page, holder: Holder, element: Element<AiringMediaModel>) {
        super.onBind(page, holder, element)
        val item = element.data ?: return

        holder.itemView.apply {

            mediaTitleTv.naText(item.title!!.title(context))
            coverImageIv.setImageURI(item.coverImage!!.image(context))
            if (item.type == MediaType.ANIME.ordinal) {
                mediaEpisodeTv.text =
                    string(R.string.ep_d_s).format(item.episodes.naText(), item.duration.naText())
            } else {
                mediaEpisodeTv.text =
                    string(R.string.chap_s).format(item.chapters.naText(), item.volumes.naText())
            }
            mediaStartDateTv.text = item.startDate?.date.naText() + " ~ " + item.endDate?.date?.naText()

            mediaGenreLayout.addGenre(
                item.genres?.take(5)
            ) { genre ->
                BrowseGenreEvent(MediaSearchFilterModel().also {
                    it.genre = listOf(genre.trim())
                }).postEvent
            }

            mediaRatingTv.text = item.averageScore?.toString().naText()

            mediaFormatTv.text = item.format?.let {
                mediaFormats[it]
            }.naText()

            mediaStatusTv.naText(item.status?.let {
                mediaStatusTv.color = Color.parseColor(statusColors[it])
                mediaStatus[it]
            })

            airingCardView.setOnClickListener {
                BrowseMediaEvent(
                    MediaBrowserMeta(
                        item.mediaId,
                        item.type!!,
                        item.title!!.romaji!!,
                        item.coverImage!!.image(context),
                        item.coverImage!!.largeImage,
                        item.bannerImage
                    ), holder.itemView.coverImageIv
                ).postEvent
            }

            bookmarkIv.setOnClickListener {
                if (context.loggedIn()) {
                    ListEditorEvent(
                        ListEditorMeta(
                            item.mediaId,
                            item.type!!,
                            item.title!!.title(context)!!,
                            item.coverImage!!.image(context),
                            item.bannerImage
                        ), holder.itemView.coverImageIv
                    ).postEvent
                } else {
                    context.makeToast(R.string.please_log_in, null, R.drawable.ic_person)
                }
            }

            airingTimeTv.setAiringText(item.airingTimeModel)


            if (context.loggedIn()) {
                entryProgressTv.visibility = View.VISIBLE
                entryProgressTv.compoundDrawablesRelative[0]?.setTint(tintSurfaceColor)
                entryProgressTv.text = context.getString(R.string.s_slash_s).format(
                    item.mediaEntryListModel?.progress?.toString().naText(),
                    when (item.type) {
                        MediaType.ANIME.ordinal -> {
                            item.episodes.naText()
                        }
                        else -> {
                            item.chapters.naText()
                        }

                    }
                )

                item.mediaEntryListModel?.let {
                    it.progress?.let {
                        bookmarkIv.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_bookmark_filled
                            )
                        )
                    } ?: let {
                        bookmarkIv.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_bookmark
                            )
                        )
                    }
                }
            } else {
                entryProgressTv.visibility = View.GONE
            }
        }

    }
}
