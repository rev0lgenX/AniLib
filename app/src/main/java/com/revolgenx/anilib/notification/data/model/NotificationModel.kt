package com.revolgenx.anilib.notification.data.model

import com.revolgenx.anilib.constant.NotificationUnionType
import com.revolgenx.anilib.common.data.model.BaseModel

abstract class NotificationModel : BaseModel() {
    var contexts: List<String>? = null
    var context: String? = null
    var createdAt: String? = null
    var type: Int? = null
    var notification: String? = null
    var notificationUnionType: NotificationUnionType? = null
}

abstract class ReasonableNotificationModel:NotificationModel(){
    var reason:String? = null
}