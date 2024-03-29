package com.revolgenx.anilib.social.ui.fragments.activity_composer.reply

import android.os.Bundle
import androidx.core.os.bundleOf
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.ui.fragment.BaseFragment
import com.revolgenx.anilib.common.event.OnActivityInfoUpdateEvent
import com.revolgenx.anilib.common.repository.util.Resource
import com.revolgenx.anilib.social.ui.fragments.activity_composer.ActivityComposerContainerFragment
import com.revolgenx.anilib.social.ui.viewmodel.activity_composer.ActivityReplyComposerViewModel
import com.revolgenx.anilib.ui.view.makeToast
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ActivityReplyComposerContainerFragment :
    ActivityComposerContainerFragment<ActivityReplyComposerViewModel>() {
    companion object {
        private const val REPLY_ACTIVITY_ID_KEY = "REPLY_ACTIVITY_ID_KEY"
        fun newInstance(activityId: Int) = ActivityReplyComposerContainerFragment().also {
            it.arguments = bundleOf(REPLY_ACTIVITY_ID_KEY to activityId)
        }
    }

    private val activityId get() = arguments?.getInt(REPLY_ACTIVITY_ID_KEY)

    override val viewModel: ActivityReplyComposerViewModel by sharedViewModel()
    override val tabEntries: Array<String>
        get() = resources.getStringArray(R.array.reply_composer_tab_entries)

    override val activityComposerFragments: List<BaseFragment> by lazy {
        listOf(
            ActivityReplyComposerFragment(),
            ActivityReplyComposerPreviewFragment()
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.field.activityId = activityId
    }

    override fun publish() {
        if(activityId == null){
            makeToast(R.string.reply_not_attached_to_activity)
            return
        }
        this.viewModel.saveActivity {
            when (it) {
                is Resource.Success -> {
                    if (this.viewModel.field.id == null) {
                        makeToast(R.string.replied_successfully)
                    } else {
                        makeToast(R.string.updated)
                    }
                    OnActivityInfoUpdateEvent(it.data!!.activityId!!).postEvent
                    popBackStack()
                }
                is Resource.Error -> {
                    makeToast(R.string.operation_failed)
                }
                is Resource.Loading -> {
                    makeToast(R.string.replying)
                }
            }
        }
    }
}