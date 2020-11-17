package com.revolgenx.anilib.ui.presenter.media

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.apollographql.apollo.exception.ApolloHttpException
import com.otaliastudios.elements.Element
import com.otaliastudios.elements.Page
import com.otaliastudios.elements.Presenter
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.constant.HTTP_TOO_MANY_REQUEST
import com.revolgenx.anilib.infrastructure.event.BrowseMediaEvent
import com.revolgenx.anilib.infrastructure.event.ListEditorEvent
import com.revolgenx.anilib.data.field.recommendation.UpdateRecommendationField
import com.revolgenx.anilib.data.meta.ListEditorMeta
import com.revolgenx.anilib.data.meta.MediaBrowserMeta
import com.revolgenx.anilib.data.model.MediaRecommendationModel
import com.revolgenx.anilib.data.model.UpdateRecommendationModel
import com.revolgenx.anilib.common.preference.loggedIn
import com.revolgenx.anilib.infrastructure.repository.util.Resource
import com.revolgenx.anilib.infrastructure.repository.util.Status
import com.revolgenx.anilib.type.MediaType
import com.revolgenx.anilib.type.RecommendationRating
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.naText
import com.revolgenx.anilib.ui.viewmodel.media.MediaOverviewViewModel
import kotlinx.android.synthetic.main.overview_recommendation_presnter_layout.view.*

class MediaRecommendationPresenter(
    private val lifecycleOwner: LifecycleOwner,
    context: Context,
    private val viewModel: MediaOverviewViewModel
) :
    Presenter<MediaRecommendationModel>(context) {
    override val elementTypes: Collection<Int>
        get() = listOf(0)

    private val statusColors by lazy {
        context.resources.getStringArray(R.array.status_color).map { Color.parseColor(it) }
    }

    override fun onCreate(parent: ViewGroup, elementType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.overview_recommendation_presnter_layout,
                parent,
                false
            )
        )
    }

    override fun onBind(page: Page, holder: Holder, element: Element<MediaRecommendationModel>) {
        super.onBind(page, holder, element)
        val data = element.data ?: return
        viewModel.mediaRecommendedList[data.recommendationId!!] = data
        holder.updateView(data)
    }

    private fun Holder.updateView(data: MediaRecommendationModel) {
        val item = data.recommended ?: return
        itemView.apply {
            mediaRecommendationTitleTv.text = item.title?.title(context)
            recommendationCoverImage.setImageURI(item.coverImage?.image(context))

            mediaRatingTv.text = item.averageScore?.toString().naText()

            if (item.type == MediaType.ANIME.ordinal) {
                mediaEpisodeFormatTv.text = context.getString(R.string.episode_format_s).format(
                    item.episodes.naText(),
                    item.format?.let { context.resources.getStringArray(R.array.media_format)[it] }.naText()
                )
            } else {
                mediaEpisodeFormatTv.text = context.getString(R.string.chapter_format_s).format(
                    item.chapters.naText(),
                    item.format?.let { context.resources.getStringArray(R.array.media_format)[it] }.naText()
                )
            }

            mediaEpisodeFormatTv.status = item.mediaEntryListModel?.status
            mediaSeasonYearTv.text = item.seasonYear?.toString().naText()
            mediaRecommendationRating.text = data.rating?.toString()
            mediaRecommendationRating.setTextColor(Color.WHITE)

            item.status?.let {
                statusDivider.setBackgroundColor(statusColors[it])
            }

            updateRecommendationLikeView(data)

            mediaRecommendationLikeIv.setOnClickListener(null)
            mediaRecommendationDislikeIv.setOnClickListener(null)

            this.setOnClickListener {
                BrowseMediaEvent(
                    MediaBrowserMeta(
                        item.mediaId!!,
                        item.type!!,
                        item.title!!.romaji!!,
                        item.coverImage!!.image(context),
                        item.coverImage!!.largeImage,
                        item.bannerImage
                    ), recommendationCoverImage
                ).postEvent
            }

            this.setOnLongClickListener {
                if (context.loggedIn()) {
                    ListEditorEvent(
                        ListEditorMeta(
                            item.mediaId,
                            item.type!!,
                            item.title!!.title(context)!!,
                            item.coverImage!!.image(context),
                            item.bannerImage
                        ), recommendationCoverImage
                    ).postEvent
                } else {
                    context.makeToast(R.string.please_log_in, null, R.drawable.ic_person)
                }
                true
            }

            mediaRecommendationLikeIv.setOnClickListener {
                if (it.checkLoggedIn()) {
                    val observer = object : Observer<Resource<UpdateRecommendationModel>> {
                        override fun onChanged(it: Resource<UpdateRecommendationModel>?) {
                            if (it != null) {
                                viewModel.removeUpdateRecommendationObserver(this)
                            }

                            when (it?.status) {
                                Status.SUCCESS -> {
                                    viewModel.mediaRecommendedList[it.data?.recommendationId]?.let { cache ->
                                        cache.rating = it.data?.rating
                                        cache.userRating = it.data?.userRating
                                    }
                                    if (data.recommendationId == it.data!!.recommendationId) {
                                        updateRecommendationLikeView(data)
                                    }
                                }
                                Status.ERROR -> {
                                    if (it.exception is ApolloHttpException) {
                                        when (it.exception.code()) {
                                            HTTP_TOO_MANY_REQUEST -> {
                                                this@updateView.itemView.context.makeToast(R.string.too_many_request)
                                            }
                                            else -> {
                                                this@updateView.itemView.context.makeToast(R.string.operation_failed)
                                            }
                                        }
                                    } else {
                                        this@updateView.itemView.context.makeToast(R.string.operation_failed)
                                    }
                                }
                            }
                        }
                    }

                    updateRecommendation(observer, data,
                        data.userRating?.let { RecommendationRating.values()[it] }
                            ?: RecommendationRating.NO_RATING,
                        RecommendationRating.RATE_UP
                    )
                }
            }

            mediaRecommendationDislikeIv.setOnClickListener {
                if (it.checkLoggedIn()) {
                    val observer = object : Observer<Resource<UpdateRecommendationModel>> {
                        override fun onChanged(it: Resource<UpdateRecommendationModel>?) {
                            if (it != null) {
                                viewModel.removeUpdateRecommendationObserver(this)
                            }

                            when (it?.status) {
                                Status.SUCCESS -> {
                                    viewModel.mediaRecommendedList[it.data?.recommendationId]?.let { cache ->
                                        cache.rating = it.data?.rating
                                        cache.userRating = it.data?.userRating
                                    }
                                    if (data.recommendationId == it.data!!.recommendationId) {
                                        updateRecommendationLikeView(data)
                                    }
                                }

                                Status.ERROR -> {
                                    if (it.exception is ApolloHttpException) {
                                        when (it.exception.code()) {
                                            HTTP_TOO_MANY_REQUEST -> {
                                                this@updateView.itemView.context.makeToast(R.string.too_many_request)
                                            }
                                            else -> {
                                                this@updateView.itemView.context.makeToast(R.string.operation_failed)
                                            }
                                        }
                                    } else {
                                        this@updateView.itemView.context.makeToast(R.string.operation_failed)
                                    }
                                }
                            }
                        }
                    }

                    updateRecommendation(observer, data,
                        data.userRating?.let { RecommendationRating.values()[it] }
                            ?: RecommendationRating.NO_RATING,
                        RecommendationRating.RATE_DOWN
                    )
                }
            }

        }
    }


    private fun View.updateRecommendationLikeView(item: MediaRecommendationModel) {
        mediaRecommendationLikeIv?.imageTintList = ColorStateList.valueOf(Color.WHITE)
        mediaRecommendationDislikeIv?.imageTintList = ColorStateList.valueOf(Color.WHITE)
        mediaRecommendationRating.setTextColor(Color.WHITE)
        mediaRecommendationRating?.text = item.rating?.toString()

        when (item.userRating) {
            RecommendationRating.RATE_UP.ordinal -> {
                mediaRecommendationLikeIv?.imageTintList =
                    ColorStateList.valueOf(DynamicTheme.getInstance().get().accentColor)
            }
            RecommendationRating.RATE_DOWN.ordinal -> {
                mediaRecommendationDislikeIv?.imageTintList =
                    ColorStateList.valueOf(DynamicTheme.getInstance().get().accentColor)
            }
        }
    }


    private fun updateRecommendation(
        observer: Observer<Resource<UpdateRecommendationModel>>,
        data: MediaRecommendationModel,
        currentRating: RecommendationRating,
        newRating: RecommendationRating
    ) {
        viewModel.updateRecommendation(UpdateRecommendationField().also { field ->
            field.mediaId = data.mediaId
            field.mediaRecommendationId = data.mediaRecommendationId

            field.rating = if (currentRating == newRating) {
                RecommendationRating.NO_RATING.ordinal
            } else {
                newRating.ordinal
            }
        }).observe(lifecycleOwner, observer)
    }

    private fun View.checkLoggedIn(): Boolean {
        return context.loggedIn().also {
            if (!it) {
                context.makeToast(R.string.please_log_in, null, R.drawable.ic_person)
            }
        }
    }

}