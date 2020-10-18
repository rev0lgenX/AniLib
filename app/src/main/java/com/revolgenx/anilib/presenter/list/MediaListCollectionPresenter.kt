package com.revolgenx.anilib.presenter.list

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.apollographql.apollo.exception.ApolloHttpException
import com.otaliastudios.elements.Element
import com.otaliastudios.elements.Page
import com.otaliastudios.elements.Presenter
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.constant.HTTP_TOO_MANY_REQUEST
import com.revolgenx.anilib.event.BrowseGenreEvent
import com.revolgenx.anilib.event.BrowseMediaEvent
import com.revolgenx.anilib.event.ListEditorEvent
import com.revolgenx.anilib.meta.ListEditorMeta
import com.revolgenx.anilib.meta.MediaBrowserMeta
import com.revolgenx.anilib.meta.MediaListMeta
import com.revolgenx.anilib.model.EntryListEditorMediaModel
import com.revolgenx.anilib.model.list.MediaListModel
import com.revolgenx.anilib.model.search.filter.MediaSearchFilterModel
import com.revolgenx.anilib.preference.getMediaListGridPresenter
import com.revolgenx.anilib.preference.loggedIn
import com.revolgenx.anilib.preference.userId
import com.revolgenx.anilib.preference.userName
import com.revolgenx.anilib.repository.util.Status
import com.revolgenx.anilib.type.MediaType
import com.revolgenx.anilib.type.ScoreFormat
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.naText
import com.revolgenx.anilib.viewmodel.media_list.MediaListCollectionViewModel
import kotlinx.android.synthetic.main.media_list_collection_loose_presenter_layout.view.*
import kotlinx.android.synthetic.main.media_list_collection_presenter_layout.view.mediaListContainer
import kotlinx.android.synthetic.main.media_list_collection_presenter_layout.view.mediaListCoverImageView
import kotlinx.android.synthetic.main.media_list_collection_presenter_layout.view.mediaListFormatTv
import kotlinx.android.synthetic.main.media_list_collection_presenter_layout.view.mediaListProgressIncrease
import kotlinx.android.synthetic.main.media_list_collection_presenter_layout.view.mediaListProgressTv
import kotlinx.android.synthetic.main.media_list_collection_presenter_layout.view.mediaListRatingIv
import kotlinx.android.synthetic.main.media_list_collection_presenter_layout.view.mediaListRatingTv
import kotlinx.android.synthetic.main.media_list_collection_presenter_layout.view.mediaListStatusTv
import kotlinx.android.synthetic.main.media_list_collection_presenter_layout.view.mediaListTitleTv

class MediaListCollectionPresenter(
    context: Context,
    private val mediaListMeta: MediaListMeta,
    private val viewModel: MediaListCollectionViewModel
) :
    Presenter<MediaListModel>(context) {

    override val elementTypes: Collection<Int> = listOf(0)
    private val mediaFormats =
        context.resources.getStringArray(R.array.media_format)
    private val mediaStatus =
        context.resources.getStringArray(R.array.media_status)
    private val statusColors =
        context.resources.getStringArray(R.array.status_color)
    private val tintSurfaceColor =
        DynamicTheme.getInstance().get().tintSurfaceColor
    private val isLoggedInUser =
        mediaListMeta.userId == context.userId() || mediaListMeta.userName == context.userName()
    private val displayMode = getMediaListGridPresenter()

    override fun onCreate(parent: ViewGroup, elementType: Int): Holder {
        return Holder(
            getLayoutInflater().inflate(
                when (getMediaListGridPresenter()) {
                    0 -> {
                        R.layout.media_list_collection_presenter_layout
                    }
                    else -> {
                        R.layout.media_list_collection_loose_presenter_layout
                    }
                },
                parent,
                false
            )
        )
    }

    override fun onBind(page: Page, holder: Holder, element: Element<MediaListModel>) {
        super.onBind(page, holder, element)
        val item = element.data ?: return
        holder.itemView.apply {
            mediaListTitleTv.text = item.title?.userPreferred
            mediaListCoverImageView.setImageURI(item.coverImage?.large)
            mediaListFormatTv.text = item.format?.let {
                mediaFormats[it]
            }.naText()
            mediaListStatusTv.text = item.status?.let {
                mediaListStatusTv.color = Color.parseColor(statusColors[it])
                mediaStatus[it]
            }.naText()

            mediaListProgressTv.text =  if (displayMode == 0) {
                context.getString(R.string.s_slash_s)
                    .format(
                        item.progress?.toString().naText(),
                        if (item.type == MediaType.ANIME.ordinal) item.episodes.naText() else item.chapters.naText()
                    )
            }else{
                context.getString(R.string.s_slash_s_brackets)
                    .format(
                        item.progress?.toString().naText(),
                        if (item.type == MediaType.ANIME.ordinal) item.episodes.naText() else item.chapters.naText(),
                        context.getString(R.string.startdate_format).format(item.listStartDate?.toString().naText(), item.listCompletedDate?.toString().naText())
                    )
            }

            mediaListProgressTv.compoundDrawablesRelative[0]?.setTint(tintSurfaceColor)

            when (item.scoreFormat) {
                ScoreFormat.POINT_3.ordinal -> {
                    val drawable = when (item.score?.toInt()) {
                        1 -> R.drawable.ic_score_sad
                        2 -> R.drawable.ic_score_neutral
                        3 -> R.drawable.ic_score_smile
                        else -> R.drawable.ic_role
                    }
                    mediaListRatingIv.setImageResource(drawable)
                }
                ScoreFormat.POINT_10_DECIMAL.ordinal -> {
                    mediaListRatingTv.text = item.score?.toString().naText()
                }
                else -> {
                    mediaListRatingTv.text = item.score?.toInt()?.toString().naText()
                }
            }

            if (displayMode == 1) {
                mediaListGenreLayout.addGenre(item.genres?.take(3)) { genre ->
                    BrowseGenreEvent(MediaSearchFilterModel().also {
                        it.genre = listOf(genre.trim())
                    }).postEvent
                }

                mediaListStartDateTv.text = context.getString(R.string.startdate_format)
                    .format(item.startDate?.toString().naText(), item.endDate?.toString().naText())
            }

            if (isLoggedInUser) {
                mediaListProgressIncrease.setOnClickListener {
                    viewModel.increaseProgress(EntryListEditorMediaModel().also {
                        it.mediaId = item.mediaId
                        it.listId = item.mediaListId
                        it.progress = (item.progress ?: 0).plus(1)
                    }) { res ->
                        when (res.status) {
                            Status.SUCCESS -> {
                                if (res.data?.mediaId == item.mediaId) {
                                    item.progress = res.data?.progress
                                    mediaListProgressTv.text =
                                        if (displayMode == 0) {
                                            context.getString(R.string.s_slash_s)
                                                .format(
                                                    item.progress?.toString().naText(),
                                                    if (item.type == MediaType.ANIME.ordinal) item.episodes.naText() else item.chapters.naText()
                                                )
                                        }else{
                                            context.getString(R.string.s_slash_s_brackets)
                                                .format(
                                                    item.progress?.toString().naText(),
                                                    if (item.type == MediaType.ANIME.ordinal) item.episodes.naText() else item.chapters.naText(),
                                                    context.getString(R.string.startdate_format).format(item.listStartDate?.toString().naText(), item.listCompletedDate?.toString().naText())
                                                )
                                        }
                                }
                            }
                            Status.ERROR -> {
                                if (res.exception is ApolloHttpException) {
                                    when (res.exception.code()) {
                                        HTTP_TOO_MANY_REQUEST -> {
                                            context.makeToast(
                                                R.string.too_many_request,
                                                icon = R.drawable.ic_error
                                            )
                                        }
                                        else -> {
                                            context.makeToast(
                                                R.string.operation_failed,
                                                icon = R.drawable.ic_error
                                            )
                                        }
                                    }
                                } else {
                                    context.makeToast(
                                        R.string.operation_failed,
                                        icon = R.drawable.ic_error
                                    )
                                }
                            }
                            Status.LOADING -> {

                            }
                        }

                    }
                }

            } else {
                mediaListProgressIncrease.visibility = View.GONE
            }

            mediaListContainer.setOnClickListener {
                BrowseMediaEvent(
                    MediaBrowserMeta(
                        item.mediaId,
                        item.type!!,
                        item.title!!.userPreferred,
                        item.coverImage!!.image(context),
                        item.coverImage!!.largeImage,
                        item.bannerImage
                    ), mediaListCoverImageView
                ).postEvent
            }

            mediaListContainer.setOnLongClickListener {
                if (context.loggedIn()) {
                    ListEditorEvent(
                        ListEditorMeta(
                            item.mediaId,
                            item.type!!,
                            item.title!!.userPreferred,
                            item.coverImage!!.image(context),
                            item.bannerImage
                        ), mediaListCoverImageView
                    ).postEvent
                } else {
                    context.makeToast(R.string.please_log_in, null, R.drawable.ic_person)
                }
                true
            }
        }
    }

}
