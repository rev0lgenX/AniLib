package com.revolgenx.anilib.search.service

import com.apollographql.apollo3.api.Query
import com.revolgenx.anilib.*
import com.revolgenx.anilib.character.data.model.CharacterModel
import com.revolgenx.anilib.character.data.model.CharacterNameModel
import com.revolgenx.anilib.character.data.model.toModel
import com.revolgenx.anilib.common.data.model.BaseModel
import com.revolgenx.anilib.common.repository.network.BaseGraphRepository
import com.revolgenx.anilib.common.repository.util.Resource
import com.revolgenx.anilib.media.data.model.MediaConnectionModel
import com.revolgenx.anilib.media.data.model.toModel
import com.revolgenx.anilib.search.data.field.SearchField
import com.revolgenx.anilib.staff.data.model.StaffModel
import com.revolgenx.anilib.staff.data.model.StaffNameModel
import com.revolgenx.anilib.staff.data.model.toModel
import com.revolgenx.anilib.studio.data.model.StudioModel
import com.revolgenx.anilib.user.data.model.UserModel
import com.revolgenx.anilib.user.data.model.toModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class SearchServiceImpl(private val baseGraphRepository: BaseGraphRepository) :
    SearchService {

    override fun search(
        field: SearchField,
        compositeDisposable: CompositeDisposable,
        callback: (Resource<List<BaseModel>>) -> Unit
    ) {
        val disposable = baseGraphRepository.request(field.toQueryOrMutation() as Query<*>)
            .map {
                when (val data = it.data) {
                    is MediaSearchQuery.Data -> {
                        data.page?.media?.mapNotNull {
                            it?.onMedia?.mediaContent?.toModel()
                        }
                    }
                    is CharacterSearchQuery.Data -> {
                        data.page?.characters?.mapNotNull { map ->
                            map?.narrowCharacterContent?.let {
                                CharacterModel().also { model ->
                                    model.id = it.id
                                    model.name = it.name?.let {
                                        CharacterNameModel(it.full)
                                    }
                                    model.image = it.image?.characterImage?.toModel()
                                }
                            }
                        }
                    }

                    is StaffSearchQuery.Data -> {
                        data.page?.staff?.mapNotNull { map ->
                            map?.narrowStaffContent?.let {
                                StaffModel().also { model ->
                                    model.id = it.id
                                    model.name = it.name?.let {
                                        StaffNameModel(it.full)
                                    }
                                    model.image = it.image?.staffImage?.toModel()
                                }
                            }
                        }
                    }

                    is StudioSearchQuery.Data -> {
                        data.page?.studios?.mapNotNull { map ->
                            map?.studioContent?.let {
                                StudioModel().also { model ->
                                    model.id = it.id
                                    model.studioName = it.name
                                    model.media = it.media?.let {
                                        MediaConnectionModel().also { connectionModel ->
                                            connectionModel.nodes =
                                                it.nodes?.mapNotNull {
                                                    it?.takeIf { if (field.canShowAdult) true else it.mediaContent.isAdult == false }?.mediaContent?.toModel()
                                                }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is UserSearchQuery.Data -> {
                        data.page?.users?.mapNotNull { user ->
                            user?.let {
                                UserModel().apply {
                                    id = it.id
                                    name = it.name
                                    avatar = it.avatar?.userAvatar?.toModel()
                                }
                            }
                        }
                    }
                    else -> {
                        emptyList()
                    }
                }
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback.invoke(Resource.success(it ?: emptyList()))
            }, {
                Timber.e(it)
                callback.invoke(Resource.error(it.message , null, it))
            })
        compositeDisposable.add(disposable)
    }
}