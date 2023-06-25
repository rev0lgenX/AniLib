package com.revolgenx.anilib.user.data.service

import com.revolgenx.anilib.common.data.model.PageModel
import com.revolgenx.anilib.common.ui.model.BaseModel
import com.revolgenx.anilib.user.data.field.UserFavouriteField
import com.revolgenx.anilib.user.data.field.UserField
import com.revolgenx.anilib.user.ui.model.UserModel
import kotlinx.coroutines.flow.Flow

interface UserService {
    fun getUser(field: UserField): Flow<UserModel?>
    fun getUserFavourite(field: UserFavouriteField): Flow<PageModel<BaseModel>>
}