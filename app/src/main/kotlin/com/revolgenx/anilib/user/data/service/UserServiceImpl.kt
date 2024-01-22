package com.revolgenx.anilib.user.data.service

import com.revolgenx.anilib.UserStatsQuery
import com.revolgenx.anilib.character.ui.model.toModel
import com.revolgenx.anilib.common.data.model.PageModel
import com.revolgenx.anilib.common.data.repository.ApolloRepository
import com.revolgenx.anilib.common.data.service.BaseService
import com.revolgenx.anilib.common.ext.orZero
import com.revolgenx.anilib.common.ui.model.BaseModel
import com.revolgenx.anilib.fragment.PageInfo
import com.revolgenx.anilib.media.ui.model.MediaTagModel
import com.revolgenx.anilib.media.ui.model.toModel
import com.revolgenx.anilib.relation.data.field.UserRelationField
import com.revolgenx.anilib.setting.data.store.MediaSettingsPreferencesDataStore
import com.revolgenx.anilib.staff.ui.model.toModel
import com.revolgenx.anilib.studio.ui.model.StudioModel
import com.revolgenx.anilib.studio.ui.model.toModel
import com.revolgenx.anilib.user.data.field.UserFavouriteField
import com.revolgenx.anilib.user.data.field.UserField
import com.revolgenx.anilib.user.data.field.UserStatsTypeField
import com.revolgenx.anilib.user.data.field.UserStatsOverviewField
import com.revolgenx.anilib.user.ui.model.MediaListOptionModel
import com.revolgenx.anilib.user.ui.model.UserModel
import com.revolgenx.anilib.user.ui.model.statistics.BaseStatisticModel
import com.revolgenx.anilib.user.ui.model.statistics.UserGenreStatisticModel
import com.revolgenx.anilib.user.ui.model.statistics.UserStaffStatisticModel
import com.revolgenx.anilib.user.ui.model.statistics.UserStatisticTypesModel
import com.revolgenx.anilib.user.ui.model.statistics.UserStudioStatisticModel
import com.revolgenx.anilib.user.ui.model.statistics.UserTagStatisticModel
import com.revolgenx.anilib.user.ui.model.statistics.UserVoiceActorStatisticModel
import com.revolgenx.anilib.user.ui.model.statistics.toModel
import com.revolgenx.anilib.user.ui.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.annotations.Nullable

class UserServiceImpl(
    apolloRepository: ApolloRepository,
    mediaSettingsPreferencesDataStore: MediaSettingsPreferencesDataStore
) :
    UserService, BaseService(apolloRepository, mediaSettingsPreferencesDataStore) {
    override fun getUser(field: UserField): Flow<UserModel?> {
        return field.toQuery().map {
            it.dataAssertNoErrors.let { data ->
                data.user?.toModel()?.also {
                    it.followers = data.followerPage?.pageInfo?.total.orZero()
                    it.following = data.followingPage?.pageInfo?.total.orZero()
                }
            }
        }
    }

    override fun getUserRelation(field: UserRelationField): Flow<PageModel<UserModel>> {
        return field.toQuery().map {
            it.dataAssertNoErrors.let {
                var pageInfo: PageInfo? = null
                var data: List<UserModel>? = null
                it.followersPage?.let {
                    pageInfo = it.pageInfo.pageInfo
                    data = it.followers?.mapNotNull { it?.userRelation?.toModel() }
                }

                it.followingPage?.let {
                    pageInfo = it.pageInfo.pageInfo
                    data = it.following?.mapNotNull { it?.userRelation?.toModel() }
                }

                PageModel(
                    pageInfo = pageInfo,
                    data = data
                )
            }
        }
    }

    override fun getUserFavourite(field: UserFavouriteField): Flow<PageModel<BaseModel>> {
        return field.toQuery().map {
            val fav = it.dataAssertNoErrors.user?.favourites
            var pageInfo: PageInfo? = null
            var data: List<BaseModel>? = null
            when {
                fav?.anime != null -> {
                    pageInfo = fav.anime.pageInfo.pageInfo
                    data = fav.anime.nodes?.mapNotNull {
                        it?.onMedia?.media?.toModel()
                    }
                }

                fav?.manga != null -> {
                    pageInfo = fav.manga.pageInfo.pageInfo
                    data = fav.manga.nodes?.mapNotNull {
                        it?.onMedia?.media?.toModel()
                    }
                }

                fav?.characters != null -> {
                    pageInfo = fav.characters.pageInfo.pageInfo
                    data = fav.characters.nodes?.mapNotNull {
                        it?.onCharacter?.smallCharacter?.toModel()
                    }
                }

                fav?.staff != null -> {
                    pageInfo = fav.staff.pageInfo.pageInfo
                    data = fav.staff.nodes?.mapNotNull {
                        it?.onStaff?.smallStaff?.toModel()
                    }
                }

                fav?.studios != null -> {
                    pageInfo = fav.studios.pageInfo.pageInfo
                    data = fav.studios.nodes?.mapNotNull {
                        it?.onStudio?.studio?.toModel(field)
                    }
                }
            }

            PageModel(
                pageInfo = pageInfo,
                data = data
            )
        }
    }

    override fun getUserStatsOverview(field: UserStatsOverviewField): Flow<UserModel?> {
        return field.toQuery().map {
            it.dataAssertNoErrors.user?.let { user ->
                UserModel(
                    id = user.id,
                    name = user.name,
                    mediaListOptions = user.mediaListOptions?.let { option ->
                        MediaListOptionModel(
                            scoreFormat = option.scoreFormat
                        )
                    },
                    statistics = user.statistics?.let { stats ->
                        UserStatisticTypesModel(
                            anime = stats.anime?.userStatisticsOverview?.toModel(true),
                            manga = stats.manga?.userStatisticsOverview?.toModel(false)
                        )
                    }
                )
            }
        }
    }

    override fun getUserStats(field: UserStatsTypeField): Flow<List<BaseStatisticModel>> {
        return field.toQuery().map {
            it.dataAssertNoErrors.user?.statistics?.let {
                if (it.anime != null) {
                    when {
                        it.anime.genres != null -> {
                            getStatsGenreModel(it.anime.genres.filterNotNull())
                        }

                        it.anime.tags != null -> {
                            getStatsTagModel(it.anime.tags.filterNotNull())
                        }

                        it.anime.studios != null -> {
                            getStatsStudioModel(it.anime.studios.filterNotNull())
                        }

                        it.anime.voiceActors != null -> {
                            getStatsVoiceActorModel(it.anime.voiceActors.filterNotNull())
                        }

                        it.anime.staff != null -> {
                            getStatsStaffModel(it.anime.staff.filterNotNull())
                        }

                        else -> {
                            emptyList()
                        }
                    }
                } else {
                    if (it.manga != null) {
                        when {
                            it.manga.genres != null -> {
                                getStatsGenreModel1(it.manga.genres.filterNotNull())
                            }

                            it.manga.tags != null -> {
                                getStatsTagModel1(it.manga.tags.filterNotNull())
                            }

                            it.manga.staff != null -> {
                                getStatsStaffModel1(it.manga.staff.filterNotNull())
                            }

                            else -> {
                                emptyList()
                            }
                        }
                    } else {
                        emptyList()
                    }
                }
            }.orEmpty()
        }
    }


    private fun getStatsGenreModel(genres: List<UserStatsQuery.Genre>): List<UserGenreStatisticModel> {
        return genres.map { genre ->
            UserGenreStatisticModel(
                genre = genre.genre,
                count = genre.count,
                minutesWatched = genre.minutesWatched,
                meanScore = genre.meanScore,
                mediaIds = genre.mediaIds.filterNotNull()
            )
        }
    }

    private fun getStatsGenreModel1(genres: List<UserStatsQuery.Genre1>): List<UserGenreStatisticModel> {
        return genres.map { genre ->
            UserGenreStatisticModel(
                genre = genre.genre,
                count = genre.count,
                chaptersRead = genre.chaptersRead,
                meanScore = genre.meanScore,
                mediaIds = genre.mediaIds.filterNotNull()
            )
        }
    }

    private fun getStatsTagModel(tags: List<UserStatsQuery.Tag>): List<UserTagStatisticModel> {
        return tags.map { stats ->
            UserTagStatisticModel(
                count = stats.count,
                meanScore = stats.meanScore,
                minutesWatched = stats.minutesWatched,
                mediaIds = stats.mediaIds.filterNotNull(),
                tag = stats.tag?.let {
                    MediaTagModel(
                        id = it.id,
                        name = it.name
                    )
                }
            )
        }
    }

    private fun getStatsTagModel1(tags: List<UserStatsQuery.Tag2>): List<UserTagStatisticModel> {
        return tags.map { stats ->
            UserTagStatisticModel(
                count = stats.count,
                meanScore = stats.meanScore,
                chaptersRead = stats.chaptersRead,
                mediaIds = stats.mediaIds.filterNotNull(),
                tag = stats.tag?.let {
                    MediaTagModel(
                        id = it.id,
                        name = it.name
                    )
                }
            )
        }
    }


    private fun getStatsStaffModel(staff: @Nullable List<UserStatsQuery.Staff>): List<UserStaffStatisticModel> {
        return staff.map { stats ->
            UserStaffStatisticModel(
                count = stats.count,
                meanScore = stats.meanScore,
                minutesWatched = stats.minutesWatched,
                mediaIds = stats.mediaIds.filterNotNull(),
                staff = stats.staff?.smallStaff?.toModel()
            )
        }
    }


    private fun getStatsStaffModel1(staff: List<UserStatsQuery.Staff2>): List<UserStaffStatisticModel> {
        return staff.map { stats ->
            UserStaffStatisticModel(
                count = stats.count,
                meanScore = stats.meanScore,
                chaptersRead = stats.chaptersRead,
                mediaIds = stats.mediaIds.filterNotNull(),
                staff = stats.staff?.smallStaff?.toModel()
            )
        }
    }


    private fun getStatsStudioModel(studios: List<UserStatsQuery.Studio>): List<UserStudioStatisticModel> {
        return studios.map { stats ->
            UserStudioStatisticModel(
                count = stats.count,
                meanScore = stats.meanScore,
                minutesWatched = stats.minutesWatched,
                mediaIds = stats.mediaIds.filterNotNull(),
                studio = stats.studio?.let {
                    StudioModel(
                        id = it.id,
                        name = it.name
                    )
                }
            )
        }
    }

    private fun getStatsVoiceActorModel(voiceActors: List<UserStatsQuery.VoiceActor>): List<UserVoiceActorStatisticModel> {
        return voiceActors.map { stats ->
            UserVoiceActorStatisticModel(
                count = stats.count,
                meanScore = stats.meanScore,
                minutesWatched = stats.minutesWatched,
                mediaIds = stats.mediaIds.filterNotNull(),
                voiceActor = stats.voiceActor?.smallStaff?.toModel()
            )
        }
    }

}