package com.revolgenx.anilib.fragment.review

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.pranavpandey.android.dynamic.theme.Theme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.event.BrowseMediaEvent
import com.revolgenx.anilib.event.UserBrowseEvent
import com.revolgenx.anilib.field.review.RateReviewField
import com.revolgenx.anilib.fragment.base.BaseToolbarFragment
import com.revolgenx.anilib.markwon.MarkwonImpl
import com.revolgenx.anilib.meta.MediaBrowserMeta
import com.revolgenx.anilib.meta.ReviewMeta
import com.revolgenx.anilib.model.review.ReviewModel
import com.revolgenx.anilib.preference.loggedIn
import com.revolgenx.anilib.repository.util.Resource
import com.revolgenx.anilib.repository.util.Status
import com.revolgenx.anilib.type.ReviewRating
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.viewmodel.review.ReviewViewModel
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.loading_layout.*
import kotlinx.android.synthetic.main.resource_status_container_layout.resourceStatusContainer
import kotlinx.android.synthetic.main.review_fragment_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReviewFragment : BaseToolbarFragment() {
    override val title: Int = R.string.review
    override val contentRes: Int = R.layout.review_fragment_layout

    companion object {
        const val reviewMetaKey = "review_meta_key"
    }


    private lateinit var reviewMeta: ReviewMeta
    private val viewModel by viewModel<ReviewViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        arguments?.classLoader = ReviewMeta::class.java.classLoader
        reviewMeta = arguments?.getParcelable(reviewMetaKey) ?: return

        viewModel.field.reviewId = reviewMeta.reviewId

        viewModel.reviewLiveData.observe(viewLifecycleOwner) { res ->
            when (res.status) {
                Status.SUCCESS -> {
                    showLoading(false)
                    updateView(res.data)
                    initListener()
                }
                Status.ERROR -> {
                    showError()
                }
                Status.LOADING -> {
                    showLoading(true)
                }
            }
        }

        if (savedInstanceState == null) {
            viewModel.getReview()
        }
    }

    private fun initListener() {
        if (viewModel.field.model == null) return
        reviewByIv.setOnClickListener {
            UserBrowseEvent(viewModel.field.model?.userPrefModel?.userId).postEvent
        }
    }

    private fun updateView(model: ReviewModel?) {
        if (model == null) return
        viewModel.field.model = model

        viewModel.field.model!!.apply {
            reviewMediaBannerImage.setImageURI(
                mediaModel?.bannerImage ?: mediaModel?.coverImage?.largeImage
            )
            reviewMediaTitleTv.text = mediaModel!!.title!!.title(requireContext())
            reviewMediaTitleTv.setOnClickListener {
                model.mediaModel?.let { item ->
                    BrowseMediaEvent(
                        MediaBrowserMeta(
                            item.mediaId,
                            item.type!!,
                            item.title!!.romaji!!,
                            item.coverImage!!.image(requireContext()),
                            item.coverImage!!.largeImage,
                            item.bannerImage
                        ), null
                    ).postEvent
                }
            }
            reviewByIv.setImageURI(userPrefModel?.avatar?.image)
            reviewByTv.text = getString(R.string.review_by).format(userPrefModel?.userName)
            createdAtTv.text = createdAt
            reviewByScoreTv.text = getString(R.string.review_score_format).format(score?.toString())
            MarkwonImpl.createHtmlInstance(requireContext()).setMarkdown(reviewTv, body.html ?: "")
            reviewLikesInfo.text =
                getString(R.string.s_out_of_s_liked_this_review).format(rating, ratingAmount)

            updateLikeDisLike(userRating)
            initListenerForLikeDislike()
        }
    }

    private fun initListenerForLikeDislike() {
        reviewLikeIv.setOnClickListener {
            if (requireContext().loggedIn()) {
                viewModel.rateReview(RateReviewField().also {
                    it.reviewId = reviewMeta.reviewId
                    when (viewModel.field.model?.userRating) {
                        ReviewRating.UP_VOTE.ordinal -> {
                            it.reviewRating = ReviewRating.NO_VOTE.ordinal
                        }
                        else -> {
                            it.reviewRating = ReviewRating.UP_VOTE.ordinal
                        }
                    }
                }) {
                    checkReviewRatingCondition(it)
                }
            } else {
                makeToast(R.string.please_log_in, null, R.drawable.ic_person)
            }
        }

        reviewDisLikeIv.setOnClickListener {
            if (requireContext().loggedIn()) {
                viewModel.rateReview(RateReviewField().also {
                    it.reviewId = reviewMeta.reviewId
                    when (viewModel.field.model?.userRating) {
                        ReviewRating.DOWN_VOTE.ordinal -> {
                            it.reviewRating = ReviewRating.NO_VOTE.ordinal
                        }
                        else -> {
                            it.reviewRating = ReviewRating.DOWN_VOTE.ordinal
                        }
                    }
                }) {
                    checkReviewRatingCondition(it)
                }
            } else {
                makeToast(R.string.please_log_in, null, R.drawable.ic_person)
            }
        }
    }

    private fun checkReviewRatingCondition(it: Resource<ReviewModel>) {
        when (it.status) {
            Status.SUCCESS -> {
                viewModel.field.model?.let { model ->
                    model.userRating = it.data?.userRating
                    model.ratingAmount = it.data?.ratingAmount
                    model.rating = it.data?.rating
                    reviewLikesInfo.text =
                        getString(R.string.s_out_of_s_liked_this_review).format(
                            model.rating,
                            model.ratingAmount
                        )
                }

                updateLikeDisLike(it.data?.userRating)
            }
            Status.ERROR -> {
                makeToast(R.string.operation_failed)
            }
            Status.LOADING -> {

            }
        }
    }

    private fun updateLikeDisLike(userRating: Int?) {
        resetLikeDislike()
        when (userRating) {
            ReviewRating.UP_VOTE.ordinal -> {
                reviewLikeIv.colorType = Theme.ColorType.TINT_ACCENT
            }
            ReviewRating.DOWN_VOTE.ordinal -> {
                reviewDisLikeIv.colorType = Theme.ColorType.TINT_ACCENT
            }
        }

    }

    private fun resetLikeDislike() {
        reviewLikeIv.colorType = Theme.ColorType.TINT_SURFACE
        reviewDisLikeIv.colorType = Theme.ColorType.TINT_SURFACE
    }

    private fun showLoading(b: Boolean) {
        resourceStatusContainer.visibility = if (b) View.VISIBLE else View.GONE
        progressLayout.visibility = if (b) View.VISIBLE else View.GONE
        errorLayout.visibility = View.GONE
    }

    private fun showError() {
        resourceStatusContainer.visibility = View.VISIBLE
        errorLayout.visibility = View.VISIBLE
        progressLayout.visibility = View.GONE
    }
}