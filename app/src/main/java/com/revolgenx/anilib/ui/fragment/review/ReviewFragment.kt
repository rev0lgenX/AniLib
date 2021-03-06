package com.revolgenx.anilib.ui.fragment.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.pranavpandey.android.dynamic.theme.Theme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.infrastructure.event.BrowseMediaEvent
import com.revolgenx.anilib.infrastructure.event.UserBrowseEvent
import com.revolgenx.anilib.data.field.review.RateReviewField
import com.revolgenx.anilib.common.ui.fragment.BaseToolbarFragment
import com.revolgenx.anilib.markwon.MarkwonImpl
import com.revolgenx.anilib.data.meta.MediaBrowserMeta
import com.revolgenx.anilib.data.meta.ReviewMeta
import com.revolgenx.anilib.data.model.review.ReviewModel
import com.revolgenx.anilib.common.preference.loggedIn
import com.revolgenx.anilib.databinding.ReviewFragmentLayoutBinding
import com.revolgenx.anilib.infrastructure.repository.util.Resource
import com.revolgenx.anilib.infrastructure.repository.util.Status
import com.revolgenx.anilib.type.ReviewRating
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.ui.viewmodel.review.ReviewViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReviewFragment : BaseToolbarFragment<ReviewFragmentLayoutBinding>() {
    override val title: Int = R.string.review

    companion object {
        const val reviewMetaKey = "review_meta_key"
    }


    private lateinit var reviewMeta: ReviewMeta
    private val viewModel by viewModel<ReviewViewModel>()

    override fun bindView(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ReviewFragmentLayoutBinding {
        return ReviewFragmentLayoutBinding.inflate(inflater, parent, false)
    }

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
        binding.reviewByIv.setOnClickListener {
            UserBrowseEvent(viewModel.field.model?.userPrefModel?.userId).postEvent
        }
    }

    private fun updateView(model: ReviewModel?) {
        if (model == null) return
        viewModel.field.model = model

        viewModel.field.model!!.apply {
            binding.reviewMediaBannerImage.setImageURI(
                mediaModel?.bannerImage ?: mediaModel?.coverImage?.largeImage
            )
            binding.reviewMediaTitleTv.text = mediaModel!!.title!!.title(requireContext())
            binding.reviewMediaTitleTv.setOnClickListener {
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
            binding.reviewByIv.setImageURI(userPrefModel?.avatar?.image)
            binding.reviewByTv.text = getString(R.string.review_by).format(userPrefModel?.userName)
            binding.createdAtTv.text = createdAt
            binding.reviewByScoreTv.text = getString(R.string.review_score_format).format(score?.toString())
            MarkwonImpl.createHtmlInstance(requireContext()).setMarkdown(binding.reviewTv, body.html ?: "")
            binding.reviewLikesInfo.text =
                getString(R.string.s_out_of_s_liked_this_review).format(rating, ratingAmount)

            updateLikeDisLike(userRating)
            initListenerForLikeDislike()
        }
    }

    private fun initListenerForLikeDislike() {
        binding.reviewLikeIv.setOnClickListener {
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

        binding.reviewDisLikeIv.setOnClickListener {
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
                    binding.reviewLikesInfo.text =
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
                binding.reviewLikeIv.colorType = Theme.ColorType.TINT_ACCENT
            }
            ReviewRating.DOWN_VOTE.ordinal -> {
                binding.reviewDisLikeIv.colorType = Theme.ColorType.TINT_ACCENT
            }
        }

    }

    private fun resetLikeDislike() {
        binding.reviewLikeIv.colorType = Theme.ColorType.TINT_SURFACE
        binding.reviewDisLikeIv.colorType = Theme.ColorType.TINT_SURFACE
    }

    private fun showLoading(b: Boolean) {
        binding.resourceStatusContainer.resourceStatusContainer.visibility = if (b) View.VISIBLE else View.GONE
        binding.resourceStatusContainer.resourceProgressLayout.progressLayout.visibility = if (b) View.VISIBLE else View.GONE
        binding.resourceStatusContainer.resourceErrorLayout.errorLayout.visibility = View.GONE
    }

    private fun showError() {
        binding.resourceStatusContainer.resourceStatusContainer.visibility = View.VISIBLE
        binding.resourceStatusContainer.resourceErrorLayout.errorLayout.visibility = View.VISIBLE
        binding.resourceStatusContainer.resourceProgressLayout.progressLayout.visibility =View.GONE
    }
}