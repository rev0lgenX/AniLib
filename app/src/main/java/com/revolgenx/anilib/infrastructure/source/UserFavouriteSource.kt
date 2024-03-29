package com.revolgenx.anilib.infrastructure.source

import com.otaliastudios.elements.Element
import com.otaliastudios.elements.Page
import com.revolgenx.anilib.common.source.BaseRecyclerSource
import com.revolgenx.anilib.user.data.field.UserFavouriteField
import com.revolgenx.anilib.common.data.model.BaseModel
import com.revolgenx.anilib.user.service.UserService
import io.reactivex.disposables.CompositeDisposable

class UserFavouriteSource(
    field: UserFavouriteField,
    private val userService: UserService,
    private val compositeDisposable: CompositeDisposable
) : BaseRecyclerSource<BaseModel, UserFavouriteField>(field) {
    override fun areItemsTheSame(first: BaseModel, second: BaseModel): Boolean {
        return first.id == second.id
    }

    override fun getElementType(data: BaseModel): Int {
        return field.favType?.ordinal!!
    }
    override fun onPageOpened(page: Page, dependencies: List<Element<*>>) {
        super.onPageOpened(page, dependencies)
        field.page = pageNo
        userService.getUserFavourite(field, compositeDisposable) {
            postResult(page, it)
        }
    }
}
