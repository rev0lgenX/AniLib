package com.revolgenx.anilib.staff.service

import androidx.lifecycle.MutableLiveData
import com.revolgenx.anilib.character.data.model.*
import com.revolgenx.anilib.common.repository.network.BaseGraphRepository
import com.revolgenx.anilib.common.repository.network.converter.toModel
import com.revolgenx.anilib.common.repository.util.Resource
import com.revolgenx.anilib.media.data.model.MediaModel
import com.revolgenx.anilib.media.data.model.toModel
import com.revolgenx.anilib.staff.data.field.StaffField
import com.revolgenx.anilib.staff.data.field.StaffMediaCharacterField
import com.revolgenx.anilib.staff.data.field.StaffMediaRoleField
import com.revolgenx.anilib.staff.data.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class StaffServiceImpl(
    private val graphRepository: BaseGraphRepository
) : StaffService {
    override val staffInfoLiveData: MutableLiveData<Resource<StaffModel>> by lazy {
        MutableLiveData<Resource<StaffModel>>()
    }

    override fun getStaffInfo(
        field: StaffField,
        compositeDisposable: CompositeDisposable,
        callback: (Resource<StaffModel>) -> Unit
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation())
            .map {
                it.data?.staff?.let {
                    StaffModel().also { model ->
                        model.id = it.id
                        model.name = it.name?.let {
                            StaffNameModel(it.full, it.native, it.alternative?.filterNotNull())
                        }
                        model.image = it.image?.staffImage?.toModel()
                        model.languageV2 = it.languageV2
                        model.favourites = it.favourites
                        model.isFavourite = it.isFavourite
                        model.siteUrl = it.siteUrl
                        model.description = it.description
                        model.age = it.age
                        model.bloodType = it.bloodType
                        model.homeTown = it.homeTown
                        model.primaryOccupations = it.primaryOccupations?.filterNotNull()
                        model.gender = it.gender
                        model.dateOfBirth = it.dateOfBirth?.fuzzyDate?.toModel()
                        model.dateOfDeath = it.dateOfDeath?.fuzzyDate?.toModel()
                        model.yearsActive = it.yearsActive?.filterNotNull()
                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback.invoke(Resource.success(it))
            }, {
                Timber.e(it)
                callback.invoke(Resource.error(it.message, null, it))
            })

        compositeDisposable.add(disposable)
    }

    override fun getStaffMediaCharacter(
        field: StaffMediaCharacterField,
        compositeDisposable: CompositeDisposable,
        resourceCallback: (Resource<List<MediaModel>>) -> Unit
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation())
            .map {
                it.data?.staff?.let { s ->
                    s.characterMedia?.edges?.mapNotNull { edge ->
                        edge?.characters?.mapNotNull { character ->
                            edge.node?.onMedia?.mediaContent?.toModel()?.also { mediaModel ->
                                mediaModel.character = character?.let {
                                    CharacterModel().also { characterModel ->
                                        characterModel.id = character.id
                                        characterModel.name = CharacterNameModel(character.name?.full)
                                        characterModel.image =
                                            character.image?.characterImage?.toModel()

                                    }
                                }
                                mediaModel.characterRole = edge.characterRole?.ordinal
                            }
                        }
                    } ?: s.characters?.edges?.mapNotNull { edge ->
                        edge?.media?.mapNotNull { media ->
                            media?.onMedia?.mediaContent?.toModel()?.also { mediaModel ->
                                mediaModel.character = edge.node?.let { c ->
                                    CharacterModel().also { characterModel ->
                                        characterModel.id = c.id
                                        characterModel.name = CharacterNameModel(c.name?.full)
                                        characterModel.image = c.image?.characterImage?.toModel()
                                    }
                                }
                                mediaModel.characterRole = edge.role?.ordinal
                            }
                        }
                    }
                }?.flatten()
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                resourceCallback.invoke(Resource.success(it))
            },
                {
                    Timber.e(it)
                    resourceCallback.invoke(Resource.error(it.message ?: "ERROR", null, it))
                })
        compositeDisposable.add(disposable)
    }

    override fun getStaffMediaRole(
        field: StaffMediaRoleField,
        compositeDisposable: CompositeDisposable,
        resourceCallback: (Resource<List<MediaModel>>) -> Unit
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation())
            .map {
                it.data?.staff?.staffMedia?.edges?.mapNotNull { edge ->
                    edge?.node?.onMedia?.mediaContent?.toModel()?.also { model ->
                        model.staffRole = edge.staffRole
                    }
                }
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                resourceCallback.invoke(Resource.success(it))
            }, {
                resourceCallback.invoke(Resource.error(it.message, null, it))
            })
        compositeDisposable.add(disposable)
    }
}