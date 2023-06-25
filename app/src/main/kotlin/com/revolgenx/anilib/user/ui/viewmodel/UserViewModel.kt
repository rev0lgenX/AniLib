package com.revolgenx.anilib.user.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.data.store.AppDataStore
import com.revolgenx.anilib.common.ui.screen.pager.PagerScreen
import com.revolgenx.anilib.common.ui.viewmodel.ResourceViewModel
import com.revolgenx.anilib.user.data.field.UserField
import com.revolgenx.anilib.user.data.service.UserService
import com.revolgenx.anilib.user.ui.model.UserModel
import kotlinx.coroutines.flow.Flow


private typealias UserScreenPage = PagerScreen<UserScreenPageType>


enum class UserScreenPageType {
    OVERVIEW,
    ACTIVITY,
    FAVOURITES
}

class UserViewModel(
    private val userService: UserService,
    val appDataStore: AppDataStore
) :
    ResourceViewModel<UserModel, UserField>() {
    override val field: UserField = UserField()
    val userId = mutableStateOf<Int?>(null)


    val pages = listOf(
        UserScreenPage(UserScreenPageType.OVERVIEW, R.string.overview),
        UserScreenPage(
            UserScreenPageType.ACTIVITY,
            R.string.activity,
            isVisible = mutableStateOf(false)
        ),
        UserScreenPage(
            UserScreenPageType.FAVOURITES,
            R.string.favourites,
            isVisible = mutableStateOf(false)
        )
    )

    fun showAllPages() {
        pages.forEach { it.isVisible.value = true }
    }

    override fun loadData(): Flow<UserModel?> = userService.getUser(field)
}