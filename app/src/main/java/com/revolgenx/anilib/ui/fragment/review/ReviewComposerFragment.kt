package com.revolgenx.anilib.ui.fragment.review

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.data.field.review.ReviewField
import com.revolgenx.anilib.common.ui.fragment.BaseToolbarFragment
import com.revolgenx.anilib.markwon.MarkwonImpl
import com.revolgenx.anilib.data.meta.ReviewComposerMeta
import com.revolgenx.anilib.data.model.review.ReviewModel
import com.revolgenx.anilib.common.preference.userId
import com.revolgenx.anilib.databinding.MarkwonHelperLayoutBinding
import com.revolgenx.anilib.databinding.ReviewComposerFragmentLayoutBinding
import com.revolgenx.anilib.infrastructure.repository.util.Status
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.getClipBoardText
import com.revolgenx.anilib.ui.view.util.attachHelperToView
import com.revolgenx.anilib.ui.viewmodel.review.ReviewComposerViewModel
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import org.koin.androidx.viewmodel.ext.android.viewModel


class ReviewComposerFragment : BaseToolbarFragment<ReviewComposerFragmentLayoutBinding>() {
    companion object {
        const val reviewComposerMetaKey = "ReviewComposerMetaKey"
    }

    override val title: Int = R.string.review_composer

    private val viewModel by viewModel<ReviewComposerViewModel>()

    private var privateReviewMenu: MenuItem? = null
    private var deleteReviewMenu: MenuItem? = null
    private var saveReviewMenu: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.review_composer_menu, menu)
    }

    override fun bindView(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ReviewComposerFragmentLayoutBinding {
        return ReviewComposerFragmentLayoutBinding.inflate(inflater, parent, false)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        privateReviewMenu = menu.findItem(R.id.privateReview)
        deleteReviewMenu = menu.findItem(R.id.deleteReview)
        saveReviewMenu = menu.findItem(R.id.saveReview)

        deleteReviewMenu!!.isVisible = false
        updatePrivateToolbar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.privateReview -> {
                item.setIcon(if (item.isChecked) R.drawable.ic_eye else R.drawable.ic_dropped)
                item.isChecked = !item.isChecked
                viewModel.field.model?.private = item.isChecked
                true
            }
            R.id.deleteReview -> {
                deleteReview()
                true
            }
            R.id.saveReview -> {
                saveReview()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.classLoader = ReviewComposerMeta::class.java.classLoader
        val reviewMeta =
            arguments?.getParcelable<ReviewComposerMeta>(reviewComposerMetaKey) ?: return

        viewModel.field.mediaId = reviewMeta.mediaId
        viewModel.field.userId = requireContext().userId()

        viewModel.reviewLiveData.observe(viewLifecycleOwner) { res ->
            when (res.status) {
                Status.SUCCESS -> {
                    showLoading(false)

                    if (viewModel.field.model == null) {
                        if (res.data != null) {
                            deleteReviewMenu!!.isVisible = true
                            viewModel.field.model = res.data
                            updateView()
                        } else {
                            viewModel.field.model = ReviewModel()
                        }
                        binding.writeReviewEt.setSelection(binding.writeReviewEt.length())
                        binding.writeReviewEt.requestFocus()
                        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.showSoftInput(
                            binding.writeReviewEt,
                            0
                        )
                    }
                }
                Status.ERROR -> {
                    showError()
                }
                Status.LOADING -> {
                    showLoading(true)
                }
            }
        }

        updateTheme()
        initEditText()
        if (savedInstanceState == null) {
            viewModel.getReview()
        }
    }

    private fun initEditText() {
        attachHelperToView(
            MarkwonHelperLayoutBinding.bind(binding.markwonHelper.markdownHelperLayout),
            binding.writeReviewEt
        )
        binding.writeReviewEt.addTextChangedListener(
            MarkwonEditorTextWatcher.withProcess(
                MarkwonImpl.createMarkwonEditor(
                    requireContext()
                )
            )
        )
        binding.summaryPasteIv.setOnClickListener {
            binding.reviewSummaryEt.append(requireContext().getClipBoardText())
        }
    }

    private fun updateTheme() {
        binding.reviewSummaryTextInputLayout.counterTextColor =
            ColorStateList.valueOf(DynamicTheme.getInstance().get().accentColor)
        binding.writeReviewTextInputLayout.counterTextColor =
            ColorStateList.valueOf(DynamicTheme.getInstance().get().accentColor)
    }


    private fun updateView() {
        viewModel.field.model?.apply {
            updatePrivateToolbar()
            summary?.let {
                binding.reviewSummaryEt.setText(it)
            }

            body.html?.let {
                binding.writeReviewEt.setText(it)
            }

            score?.let {
                binding.reviewScorePlusMinusTv.setCounter(it.toDouble())
            }
        }
    }


    private fun updatePrivateToolbar() {
        viewModel.field.model?.apply {
            privateReviewMenu?.setIcon(if (private == true) R.drawable.ic_dropped else R.drawable.ic_eye)
            privateReviewMenu?.isChecked = private ?: false
        }
    }

    private fun showLoading(b: Boolean) {
        binding.resourceStatusLayout.resourceStatusContainer.visibility =
            if (b) View.VISIBLE else View.GONE
        binding.resourceStatusLayout.resourceProgressLayout.progressLayout.visibility =
            if (b) View.VISIBLE else View.GONE
        binding.resourceStatusLayout.resourceErrorLayout.errorLayout.visibility = View.GONE
    }

    private fun showError() {
        binding.resourceStatusLayout.resourceStatusContainer.visibility = View.VISIBLE
        binding.resourceStatusLayout.resourceErrorLayout.errorLayout.visibility = View.VISIBLE
        binding.resourceStatusLayout.resourceProgressLayout.progressLayout.visibility = View.GONE
    }

    private fun saveReview() {
        viewModel.field.type = ReviewField.ReviewFieldType.MUTATE
        if (!updateModel()) return
        viewModel.saveReview {
            when (it.status) {
                Status.SUCCESS -> {
                    showLoading(false)
                    finishActivity()
                }
                Status.LOADING -> {
                    showLoading(true)
                }
                Status.ERROR -> {
                    showLoading(false)
                    makeToast(R.string.operation_failed, icon = R.drawable.ic_error)
                }
            }
        }
    }

    private fun deleteReview() {
        viewModel.field.type = ReviewField.ReviewFieldType.DELETE
        viewModel.deleteReview {
            when (it.status) {
                Status.SUCCESS -> {
                    showLoading(false)
                    finishActivity()
                }
                Status.LOADING -> {
                    showLoading(true)
                }
                Status.ERROR -> {
                    showLoading(false)
                    makeToast(R.string.operation_failed, icon = R.drawable.ic_error)
                }
            }
        }
    }

    private fun updateModel(): Boolean {

        val reviewLength = binding.writeReviewEt.length()
        if (reviewLength < 2201) {
            binding.writeReviewTextInputLayout.error = getString(R.string.check_character_length)
            return false
        }

        val summaryLength = binding.reviewSummaryEt.length()
        if (summaryLength < 21 || summaryLength > 120) {
            binding.reviewSummaryTextInputLayout.error = getString(R.string.check_character_length)
            return false
        }


        viewModel.field.model?.apply {
            summary = binding.reviewSummaryEt.text.toString()
            body.html = binding.writeReviewEt.text.toString()
            score = binding.reviewScorePlusMinusTv.counterHolder.toInt()
        }
        return true
    }
}