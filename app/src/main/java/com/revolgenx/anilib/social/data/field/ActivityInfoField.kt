package com.revolgenx.anilib.social.data.field

import com.revolgenx.anilib.ActivityInfoQuery
import com.revolgenx.anilib.common.data.field.BaseField

class ActivityInfoField : BaseField<ActivityInfoQuery>(){
    var activityId:Int? = null
    override fun toQueryOrMutation(): ActivityInfoQuery {
        return ActivityInfoQuery(id = nn(activityId))
    }
}
