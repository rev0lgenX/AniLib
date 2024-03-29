package com.revolgenx.anilib.social.ui.viewmodel.activity_composer

import com.revolgenx.anilib.common.repository.util.Resource
import com.revolgenx.anilib.social.data.field.SaveActivityReplyField
import com.revolgenx.anilib.social.data.model.reply.ActivityReplyModel
import com.revolgenx.anilib.social.infrastructure.service.ActivityUnionService

class ActivityReplyComposerViewModel(private val activityUnionService: ActivityUnionService) : ActivityComposerViewModel<ActivityReplyModel, ActivityReplyModel,SaveActivityReplyField>() {
    override val field = SaveActivityReplyField()

    override fun save(callback: (Resource<ActivityReplyModel>) -> Unit) {
        activityUnionService.saveActivityReply(field, compositeDisposable, callback)
    }
}