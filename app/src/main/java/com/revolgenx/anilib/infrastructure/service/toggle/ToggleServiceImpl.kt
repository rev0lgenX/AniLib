package com.revolgenx.anilib.infrastructure.service.toggle

import com.revolgenx.anilib.common.data.field.ToggleFavouriteField
import com.revolgenx.anilib.social.data.model.LikeableUnionModel
import com.revolgenx.anilib.common.repository.network.BaseGraphRepository
import com.revolgenx.anilib.common.repository.util.Resource
import com.revolgenx.anilib.social.data.field.ToggleActivitySubscriptionField
import com.revolgenx.anilib.social.data.field.ToggleLikeV2Field
import com.revolgenx.anilib.social.data.model.ActivityUnionModel
import com.revolgenx.anilib.social.data.model.ListActivityModel
import com.revolgenx.anilib.social.data.model.MessageActivityModel
import com.revolgenx.anilib.social.data.model.TextActivityModel
import com.revolgenx.anilib.user.data.field.UserToggleFollowField
import com.revolgenx.anilib.user.data.model.UserModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class ToggleServiceImpl(private val graphRepository: BaseGraphRepository) :
    ToggleService {
    override fun toggleLikeV2(
        field: ToggleLikeV2Field,
        compositeDisposable: CompositeDisposable,
        callback: (Resource<LikeableUnionModel>) -> Unit
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation())
            .map {
                it.data?.toggleLikeV2?.let {
                    it.onTextActivity?.let {
                        LikeableUnionModel().also { model ->
                            model.id = it.id
                            model.isLiked = it.isLiked ?: false
                            model.likeCount = it.likeCount
                        }
                    } ?: it.onListActivity?.let {
                        LikeableUnionModel().also { model ->
                            model.id = it.id
                            model.isLiked = it.isLiked ?: false
                            model.likeCount = it.likeCount

                        }
                    } ?: it.onActivityReply?.let {
                        LikeableUnionModel().also { model ->
                            model.id = it.id
                            model.isLiked = it.isLiked ?: false
                            model.likeCount = it.likeCount
                        }
                    } ?: it.onMessageActivity?.let {
                        LikeableUnionModel().also { model ->
                            model.id = it.id
                            model.isLiked = it.isLiked ?: false
                            model.likeCount = it.likeCount
                        }
                    }
                }
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback.invoke(Resource.success(it))
            }, {
                Timber.e(it)
                callback.invoke(Resource.error(it.message , null, it))
            })
        compositeDisposable.add(disposable)
    }


    override fun toggleActivitySubscription(
        field: ToggleActivitySubscriptionField,
        compositeDisposable: CompositeDisposable,
        callback: (Resource<ActivityUnionModel>) -> Unit
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation())
            .map {
                it.data?.toggleActivitySubscription?.let {
                    it.onTextActivity?.let {
                        TextActivityModel().also { model ->
                            model.id = it.id
                            model.isSubscribed = it.isSubscribed ?: false
                        }
                    } ?: it.onListActivity?.let {
                        ListActivityModel().also { model ->
                            model.id = it.id
                            model.isSubscribed = it.isSubscribed ?: false
                        }
                    } ?: it.onMessageActivity?.let {
                        MessageActivityModel().also { model ->
                            model.id = it.id
                            model.isSubscribed = it.isSubscribed ?: false
                        }
                    }
                }
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback.invoke(Resource.success(it))
            }, {
                callback.invoke(Resource.error(it.message , null, it))
            })

        compositeDisposable.add(disposable)
    }

    override fun toggleFavourite(
        field: ToggleFavouriteField,
        compositeDisposable: CompositeDisposable?,
        callback: (Resource<Boolean>) -> Unit
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback.invoke(Resource.success(true))
            }, {
                Timber.w(it)
                callback.invoke(Resource.error(it.message , null, it))
            })
        compositeDisposable?.add(disposable)
    }


    // TODO create global store
    override fun toggleUserFollow(
        field: UserToggleFollowField,
        compositeDisposable: CompositeDisposable,
        callback: ((Resource<UserModel>) -> Unit)?
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation()).map {
            it.data?.toggleFollow?.let {
                UserModel().also { model ->
                    model.id = it.id
                    model.isFollowing = it.isFollowing ?: false
                }
            }
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback?.invoke(Resource.success(it))
            }, {
                Timber.e(it)
                callback?.invoke(Resource.error(it.message , null, it))
            })
        compositeDisposable.add(disposable)
    }

}