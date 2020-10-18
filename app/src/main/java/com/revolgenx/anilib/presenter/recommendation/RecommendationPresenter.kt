package com.revolgenx.anilib.presenter.recommendation

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
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
import com.revolgenx.anilib.event.BrowseMediaEvent
import com.revolgenx.anilib.event.ListEditorEvent
import com.revolgenx.anilib.field.recommendation.UpdateRecommendationField
import com.revolgenx.anilib.meta.ListEditorMeta
import com.revolgenx.anilib.meta.MediaBrowserMeta
import com.revolgenx.anilib.model.UpdateRecommendationModel
import com.revolgenx.anilib.model.recommendation.RecommendationModel
import com.revolgenx.anilib.preference.loggedIn
import com.revolgenx.anilib.repository.util.Resource
import com.revolgenx.anilib.repository.util.Status
import com.revolgenx.anilib.type.RecommendationRating
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.naText
import com.revolgenx.anilib.viewmodel.home.RecommendationViewModel
import kotlinx.android.synthetic.main.recommendation_presenter_layout.view.*

class RecommendationPresenter(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: RecommendationViewModel
) :
    Presenter<RecommendationModel>(context) {
    override val elementTypes: Collection<Int>
        get() = listOf(0)

    private val statusColors by lazy {
        context.resources.getStringArray(R.array.status_color)
    }

    private val mediaStatus by lazy {
        context.resources.getStringArray(R.array.media_status)
    }

    private val mediaFormats by lazy {
        context.resources.getStringArray(R.array.media_format)
    }

    private val updateRecommendationField by lazy {
        UpdateRecommendationField()
    }


    override fun onCreate(parent: ViewGroup, elementType: Int): Holder {
        return Holder(
            getLayoutInflater().inflate(
                R.layout.recommendation_presenter_layout,
                parent,
                false
            )
        )
    }

    override fun onBind(page: Page, holder: Holder, element: Element<RecommendationModel>) {
        super.onBind(page, holder, element)
        val item = element.data ?: return
        viewModel.recommendedList[item.recommendationId!!] = item

        holder.itemView.apply {
            item.recommendationFrom?.let { from ->
                recommendedFromTitleTv.text = from.title?.title(context)
                recommendedFromImageView.setImageURI(from.coverImage?.image(context))
                recommendedFromRatingTv.text = from.averageScore?.toString().naText()
                recommendedFromStatusTv.text = from.status?.let {
                    recommendedFromStatusTv.color = Color.parseColor(statusColors[it])
                    mediaStatus[it]
                }.naText()
                recommendedFromFormatYearTv.text =
                    context.getString(R.string.media_format_year_s).format(
                        from.format?.let { mediaFormats[it] }.naText(),
                        from.seasonYear?.toString().naText()
                    )

                recommendedFromImageConstraintLayout.setOnClickListener {
                    BrowseMediaEvent(
                        MediaBrowserMeta(
                            from.mediaId,
                            from.type!!,
                            from.title!!.romaji!!,
                            from.coverImage!!.image(context),
                            from.coverImage!!.largeImage,
                            from.bannerImage
                        ), recommendedFromImageView
                    ).postEvent
                }

                recommendedFromImageConstraintLayout.setOnLongClickListener {
                    if (context.loggedIn()) {
                        ListEditorEvent(
                            ListEditorMeta(
                                from.mediaId,
                                from.type!!,
                                from.title!!.title(context)!!,
                                from.coverImage!!.image(context),
                                from.bannerImage
                            ), recommendedFromImageView
                        ).postEvent
                    } else {
                        context.makeToast(R.string.please_log_in, null, R.drawable.ic_person)
                    }
                    true
                }
            }

            item.recommended?.let { rec ->
                recommendedTitleTv.text = rec.title?.title(context)
                recommendedImageView.setImageURI(rec.coverImage?.image(context))
                recommendedMediaRatingTv.text = rec.averageScore?.toString().naText()
                recommendedStatusTv.text = rec.status?.let {
                    recommendedStatusTv.color = Color.parseColor(statusColors[it])
                    mediaStatus[it]
                }.naText()
                recommendedFormatYearTv.text =
                    context.getString(R.string.media_format_year_s).format(
                        rec.format?.let { mediaFormats[it] }.naText(),
                        rec.seasonYear?.toString().naText()
                    )

                recommendedImageConstraintLayout.setOnClickListener {
                    BrowseMediaEvent(
                        MediaBrowserMeta(
                            rec.mediaId,
                            rec.type!!,
                            rec.title!!.romaji!!,
                            rec.coverImage!!.image(context),
                            rec.coverImage!!.largeImage,
                            rec.bannerImage
                        ), recommendedImageView
                    ).postEvent
                }

                recommendedImageConstraintLayout.setOnLongClickListener {
                    if (context.loggedIn()) {
                        ListEditorEvent(
                            ListEditorMeta(
                                rec.mediaId,
                                rec.type!!,
                                rec.title!!.title(context)!!,
                                rec.coverImage!!.image(context),
                                rec.bannerImage
                            ), recommendedImageView
                        ).postEvent
                    } else {
                        context.makeToast(R.string.please_log_in, null, R.drawable.ic_person)
                    }
                    true
                }
            }

            updateRecommendationLikeView(item)

            recommendationLikeIv.setOnClickListener {
                if (it.checkLoggedIn()) {
                    val observer = object : Observer<Resource<UpdateRecommendationModel>> {
                        override fun onChanged(it: Resource<UpdateRecommendationModel>?) {
                            if (it != null) {
                                viewModel.removeUpdateRecommendationObserver(this)
                            }
                            when (it?.status) {
                                Status.SUCCESS -> {
                                    viewModel.recommendedList[it.data?.recommendationId]?.let { cache ->
                                        cache.rating = it.data?.rating
                                        cache.userRating = it.data?.userRating
                                    }

                                    if (item.recommendationId == it.data?.recommendationId) {
                                        updateRecommendationLikeView(item)
                                    }
                                }
                                Status.ERROR -> {
                                    if (it.exception is ApolloHttpException) {
                                        when (it.exception.code()) {
                                            HTTP_TOO_MANY_REQUEST -> {
                                                context.makeToast(R.string.too_many_request)
                                            }
                                            else -> {
                                                context.makeToast(R.string.operation_failed)
                                            }
                                        }
                                    } else {
                                        context.makeToast(R.string.operation_failed)
                                    }
                                }
                            }
                        }
                    }

                    updateRecommendation(observer, item,
                        item.userRating?.let { RecommendationRating.values()[it] }
                            ?: RecommendationRating.NO_RATING,
                        RecommendationRating.RATE_UP
                    )
                }
            }

            recommendationDislikeIv.setOnClickListener {
                if (it.checkLoggedIn()) {
                    val observer = object : Observer<Resource<UpdateRecommendationModel>> {
                        override fun onChanged(it: Resource<UpdateRecommendationModel>?) {
                            if (it != null) {
                                viewModel.removeUpdateRecommendationObserver(this)
                            }

                            when (it?.status) {
                                Status.SUCCESS -> {
                                    viewModel.recommendedList[it.data?.recommendationId]?.let { cache ->
                                        cache.rating = it.data?.rating
                                        cache.userRating = it.data?.userRating
                                    }

                                    if (item.recommendationId == it.data?.recommendationId) {
                                        updateRecommendationLikeView(item)
                                    }
                                }
                                Status.ERROR -> {
                                    context.makeToast(R.string.operation_failed)
                                }
                            }
                        }
                    }

                    updateRecommendation(observer, item,
                        item.userRating?.let { RecommendationRating.values()[it] }
                            ?: RecommendationRating.NO_RATING,
                        RecommendationRating.RATE_DOWN
                    )
                }
            }
        }
    }


    private fun updateRecommendation(
        observer: Observer<Resource<UpdateRecommendationModel>>,
        data: RecommendationModel,
        currentRating: RecommendationRating,
        newRating: RecommendationRating
    ) {
        viewModel.updateRecommendation(updateRecommendationField.also { field ->
            field.mediaId = data.recommendationFrom?.mediaId
            field.mediaRecommendationId = data.recommended?.mediaId
            field.rating = if (currentRating == newRating) {
                RecommendationRating.NO_RATING.ordinal
            } else {
                newRating.ordinal
            }
        }).observe(lifecycleOwner, observer)
    }

    private fun View.updateRecommendationLikeView(item: RecommendationModel) {
        recommendationLikeIv?.imageTintList = ColorStateList.valueOf(Color.WHITE)
        recommendationDislikeIv?.imageTintList = ColorStateList.valueOf(Color.WHITE)
        recommendationRatingTv.setTextColor(Color.WHITE)
        recommendationRatingTv?.text = item.rating?.toString()

        when (item.userRating) {
            RecommendationRating.RATE_UP.ordinal -> {
                recommendationLikeIv?.imageTintList =
                    ColorStateList.valueOf(DynamicTheme.getInstance().get().accentColor)
            }
            RecommendationRating.RATE_DOWN.ordinal -> {
                recommendationDislikeIv?.imageTintList =
                    ColorStateList.valueOf(DynamicTheme.getInstance().get().accentColor)
            }
        }
    }

    private fun View.checkLoggedIn(): Boolean {
        return context.loggedIn().also {
            if (!it) {
                context.makeToast(R.string.please_log_in, null, R.drawable.ic_person)
            }
        }
    }
}
