package com.revolgenx.anilib.fragment.review

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.observe
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.field.review.ReviewField
import com.revolgenx.anilib.fragment.base.BaseToolbarFragment
import com.revolgenx.anilib.markwon.MarkwonImpl
import com.revolgenx.anilib.meta.ReviewComposerMeta
import com.revolgenx.anilib.model.review.ReviewModel
import com.revolgenx.anilib.preference.userId
import com.revolgenx.anilib.repository.util.Status
import com.revolgenx.anilib.ui.view.makeToast
import com.revolgenx.anilib.util.getClipBoardText
import com.revolgenx.anilib.view.util.attachHelperToView
import com.revolgenx.anilib.viewmodel.review.ReviewComposerViewModel
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.loading_layout.*
import kotlinx.android.synthetic.main.markwon_helper_layout.*
import kotlinx.android.synthetic.main.resource_status_container_layout.*
import kotlinx.android.synthetic.main.review_composer_fragment_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class ReviewComposerFragment : BaseToolbarFragment() {
    companion object {
        const val reviewComposerMetaKey = "ReviewComposerMetaKey"
    }

    override val title: Int = R.string.review_composer
    override val contentRes: Int = R.layout.review_composer_fragment_layout

    private val viewModel by viewModel<ReviewComposerViewModel>()

    private var privateReviewMenu: MenuItem? = null
    private var deleteReviewMenu: MenuItem? = null
    private var saveReviewMenu: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.review_composer_menu, menu)
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
                        writeReviewEt.setSelection(writeReviewEt.length())
                        writeReviewEt.requestFocus()
                        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.showSoftInput(
                            writeReviewEt,
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
        attachHelperToView(markdownHelperLayout, writeReviewEt)
        writeReviewEt.addTextChangedListener(
            MarkwonEditorTextWatcher.withProcess(
                MarkwonImpl.createMarkwonEditor(
                    requireContext()
                )
            )
        )
        summaryPasteIv.setOnClickListener {
            reviewSummaryEt.append(requireContext().getClipBoardText())
        }
    }

    private fun updateTheme() {
        reviewSummaryTextInputLayout.counterTextColor =
            ColorStateList.valueOf(DynamicTheme.getInstance().get().accentColor)
        writeReviewTextInputLayout.counterTextColor =
            ColorStateList.valueOf(DynamicTheme.getInstance().get().accentColor)
    }


    private fun updateView() {
        viewModel.field.model?.apply {
            updatePrivateToolbar()
            summary?.let {
                reviewSummaryEt.setText(it)
            }

            body?.html?.let {
                writeReviewEt.setText(it)
            }

            score?.let {
                reviewScorePlusMinusTv.setCounter(it.toDouble())
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
        resourceStatusContainer.visibility = if (b) View.VISIBLE else View.GONE
        progressLayout.visibility = if (b) View.VISIBLE else View.GONE
        errorLayout.visibility = View.GONE
    }

    private fun showError() {
        resourceStatusContainer.visibility = View.VISIBLE
        errorLayout.visibility = View.VISIBLE
        progressLayout.visibility = View.GONE
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

        val reviewLength = writeReviewEt.length()
        if (reviewLength < 2201) {
            writeReviewTextInputLayout.error = getString(R.string.check_character_length)
            return false
        }

        val summaryLength = reviewSummaryEt.length()
        if (summaryLength < 21 || summaryLength > 120) {
            reviewSummaryTextInputLayout.error = getString(R.string.check_character_length)
            return false
        }


        viewModel.field.model?.apply {
            summary = reviewSummaryEt.text.toString()
            body.html = writeReviewEt.text.toString()
            score = reviewScorePlusMinusTv.counterHolder.toInt()
        }
        return true
    }
}