package com.revolgenx.anilib.media.service

import com.github.mikephil.charting.data.Entry
import com.revolgenx.anilib.character.data.model.*
import com.revolgenx.anilib.common.data.model.stats.ScoreDistributionModel
import com.revolgenx.anilib.common.data.model.stats.StatusDistributionModel
import com.revolgenx.anilib.common.repository.network.BaseGraphRepository
import com.revolgenx.anilib.common.repository.network.converter.toModel
import com.revolgenx.anilib.common.repository.util.Resource
import com.revolgenx.anilib.media.data.field.*
import com.revolgenx.anilib.media.data.model.*
import com.revolgenx.anilib.staff.data.model.*
import com.revolgenx.anilib.user.data.model.UserModel
import com.revolgenx.anilib.user.data.model.stats.*
import com.revolgenx.anilib.user.data.model.stats.MediaStatsModel
import com.revolgenx.anilib.user.data.model.toModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class MediaInfoServiceImpl(graphRepository: BaseGraphRepository) :
    MediaInfoService(graphRepository) {

    override fun getMediaOverview(
        field: MediaOverviewField,
        compositeDisposable: CompositeDisposable?,
        callback: (Resource<MediaModel>) -> Unit
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation())
            .map { response ->
                response.data?.media!!.toModel()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback.invoke(Resource.success(it))
            }, {
                Timber.e(it)
                callback.invoke(Resource.error(it.message , null, it))
            })

        compositeDisposable?.add(disposable)
    }

    override fun getMediaWatch(
        field: MediaWatchField,
        compositeDisposable: CompositeDisposable?,
        callback: (Resource<List<MediaStreamingEpisodeModel>>) -> Unit
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation())
            .map { response ->
                response.data?.media!!.toModel()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback.invoke(Resource.success(it))
            }, {
                Timber.w(it)
                callback.invoke(Resource.error(it.message , null, it))
            })

        compositeDisposable?.add(disposable)
    }

    override fun getMediaCharacter(
        field: MediaCharacterField,
        compositeDisposable: CompositeDisposable?,
        callback: (Resource<List<CharacterEdgeModel>>) -> Unit
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation())
            .map { response ->
                response.data?.media?.characters?.edges?.mapNotNull { edgeData ->
                    edgeData?.let {
                        CharacterEdgeModel().also { edge ->
                            edge.role = it.role?.ordinal
                            edge.voiceActors = it.voiceActors?.mapNotNull { v ->
                                v?.let {
                                    StaffModel().also { model ->
                                        model.id = it.id
                                        model.name = it.name?.let { name ->
                                            StaffNameModel(name.full)
                                        }
                                        model.languageV2 = it.languageV2
                                        model.image = it.image?.staffImage?.toModel()
                                    }
                                }
                            }
                            edge.node = it.node?.let { node ->
                                CharacterModel().also { charac ->
                                    charac.id = node.id
                                    charac.name = CharacterNameModel(node.name?.full)
                                    charac.image = node.image?.characterImage?.toModel()
                                    charac.siteUrl = it.node.siteUrl
                                }

                            }

                        }
                    }

                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback.invoke(Resource.success(it))
            }, {
                Timber.e(it)
                callback.invoke(Resource.error(it.message , null, it))
            })

        compositeDisposable?.add(disposable)
    }

    override fun getMediaStaff(
        field: MediaStaffField,
        compositeDisposable: CompositeDisposable?,
        callback: (Resource<List<StaffEdgeModel>>) -> Unit
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation())
            .map {
                it.data?.media?.staff?.edges?.mapNotNull { sData ->
                    sData?.let { s ->
                        StaffEdgeModel().also { model ->
                            model.role = s.role
                            model.node = s.node?.let { staff ->
                                StaffModel().also { staffModel ->
                                    staffModel.id = staff.id
                                    staffModel.name = staff.name?.let {
                                        StaffNameModel(it.full)
                                    }
                                    staffModel.image = staff.image?.staffImage?.toModel()
                                }
                            }
                        }
                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback.invoke(Resource.success(it))
            }, {
                Timber.e(it)
                callback.invoke(Resource.error(it.message , null, it))
            })

        compositeDisposable?.add(disposable)
    }

    override fun getMediaReview(
        field: MediaReviewField,
        compositeDisposable: CompositeDisposable?,
        callback: (Resource<List<MediaReviewModel>>) -> Unit
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation())
            .map { response ->
                runBlocking {
                    response.data?.media?.reviews?.edges?.mapNotNull { edgeData ->
                        edgeData?.let { edge ->
                            MediaReviewModel().also { model ->
                                edge.node?.let { node ->
                                    model.id = node.id
                                    model.rating = node.rating
                                    model.ratingAmount = node.ratingAmount
                                    model.summary = node.summary
                                    model.userRating = node.userRating?.ordinal
                                    model.user = node.user?.let {
                                        UserModel().also { model ->
                                            model.id = it.id
                                            model.avatar = it.avatar?.userAvatar?.toModel()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback.invoke(Resource.success(it))
            }, {
                Timber.e(it)
                callback.invoke(Resource.error(it.message , null, it))
            })

        compositeDisposable?.add(disposable)
    }

    override fun getMediaStats(
        field: MediaStatsField,
        compositeDisposable: CompositeDisposable?,
        callback: (Resource<MediaStatsModel>) -> Unit
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation())
            .map { response ->
                response.data?.media?.let { media ->
                    MediaStatsModel().also { model ->
                        model.rankings = media.rankings?.mapNotNull { rankData ->
                            rankData?.let { rank ->
                                MediaRankModel().also { rankModel ->
                                    rankModel.id = rank.id
                                    rankModel.context = rank.context
                                    rankModel.allTime = rank.allTime ?: false
                                    rankModel.rank = rank.rank
                                    rankModel.season = rank.season?.ordinal
                                    rankModel.year = rank.year
                                    rankModel.rankType = rank.type.ordinal
                                }
                            }
                        }


                        model.trends = media.trends?.nodes?.mapNotNull { nodeData ->
                            nodeData?.let { node ->
                                MediaTrendModel().also { trendModel ->
                                    trendModel.date = node.date
                                    trendModel.trending = node.trending
                                }
                            }
                        }

                        model.trendsEntries = model.trends?.sortedBy { it.date }?.map {
                            Entry(it.date.toFloat(), it.trending.toFloat())
                        }

                        model.statusDistribution =
                            media.stats?.statusDistribution?.mapNotNull { statusData ->
                                statusData?.let { status ->
                                    StatusDistributionModel().also { statusModel ->
                                        statusModel.amount = status.amount
                                        statusModel.status = status.status?.ordinal
                                    }
                                }
                            }

                        model.scoreDistribution =
                            media.stats?.scoreDistribution?.mapNotNull { scoreData ->
                                scoreData?.let { score->
                                    ScoreDistributionModel().also { scoreModel ->
                                        scoreModel.amount = score.amount
                                        scoreModel.score = score.score
                                    }
                                }
                            }

                        model.airingTrends =
                            media.airingTrends?.nodes?.mapNotNull {
                                it?.takeIf { it.episode != null }?.let { trends ->
                                    AiringTrendsModel(
                                        trends.episode!!.toFloat(),
                                        trends.averageScore?.toFloat() ?: 0f,
                                        trends.inProgress?.toFloat() ?: 0f
                                    )
                                }
                            }?.sortedBy { it.episode }

                        model.airingTrends?.let { trends ->
                            val airingWatchersProgressionEntries = mutableListOf<Entry>()
                            val airingScoreProgressionEntries = mutableListOf<Entry>()
                            trends.forEach {
                                airingWatchersProgressionEntries.add(
                                    Entry(
                                        it.episode,
                                        it.inProgress
                                    )
                                )
                                airingScoreProgressionEntries.add(
                                    Entry(
                                        it.episode,
                                        it.averageScore
                                    )
                                )
                            }

                            model.airingWatchersProgressionEntries =
                                airingWatchersProgressionEntries
                            model.airingScoreProgressionEntries = airingScoreProgressionEntries
                        }
                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback.invoke(Resource.success(it))
            }, {
                Timber.e(it)
                callback.invoke(Resource.error(it.message , null, it))
            })
        compositeDisposable?.add(disposable)
    }


    override fun getMediaSocialFollowing(
        field: MediaSocialFollowingField,
        compositeDisposable: CompositeDisposable,
        callback: (Resource<List<MediaSocialFollowingModel>>) -> Unit
    ) {
        val disposable = graphRepository.request(field.toQueryOrMutation())
            .map {
                it.data?.page?.mediaList?.mapNotNull { list ->
                    list?.toModel()
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback.invoke(Resource.success(it ?: emptyList()))
            }, {
                Timber.e(it)
                callback.invoke(Resource.error(it.message , null, it))
            })
        compositeDisposable.add(disposable)
    }

}