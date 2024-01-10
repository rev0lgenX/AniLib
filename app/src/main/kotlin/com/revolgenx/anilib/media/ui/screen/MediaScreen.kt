package com.revolgenx.anilib.media.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import com.revolgenx.anilib.common.ui.component.card.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revolgenx.anilib.common.ui.screen.voyager.AndroidScreen
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.ext.emptyWindowInsets
import com.revolgenx.anilib.common.ext.naText
import com.revolgenx.anilib.common.ext.orZero
import com.revolgenx.anilib.common.ext.prettyNumberFormat
import com.revolgenx.anilib.common.ui.component.action.ActionMenu
import com.revolgenx.anilib.common.ui.component.action.OpenInBrowserOverflowMenu
import com.revolgenx.anilib.common.ui.component.action.OverflowMenu
import com.revolgenx.anilib.common.ui.component.action.ShareOverflowMenu
import com.revolgenx.anilib.common.ui.component.appbar.CollapsingAppbar
import com.revolgenx.anilib.common.ui.component.appbar.collapse
import com.revolgenx.anilib.common.ui.component.image.ImageAsync
import com.revolgenx.anilib.common.ui.component.image.ImageOptions
import com.revolgenx.anilib.common.ui.component.scaffold.PagerScreenScaffold
import com.revolgenx.anilib.common.ui.component.scaffold.ScreenScaffold
import com.revolgenx.anilib.common.ui.component.text.MediumText
import com.revolgenx.anilib.common.ui.component.text.SemiBoldText
import com.revolgenx.anilib.common.ui.component.text.shadow
import com.revolgenx.anilib.common.ui.icons.AppIcons
import com.revolgenx.anilib.common.ui.icons.appicon.IcHeart
import com.revolgenx.anilib.common.ui.icons.appicon.IcHeartOutline
import com.revolgenx.anilib.common.ui.icons.appicon.IcMoreHoriz
import com.revolgenx.anilib.common.ui.icons.appicon.IcReview
import com.revolgenx.anilib.common.ui.icons.appicon.IcStar
import com.revolgenx.anilib.list.ui.model.toStringRes
import com.revolgenx.anilib.media.ui.component.MediaCoverImageType
import com.revolgenx.anilib.media.ui.component.MediaTitleType
import com.revolgenx.anilib.media.ui.model.MediaModel
import com.revolgenx.anilib.media.ui.model.isAnime
import com.revolgenx.anilib.media.ui.model.toStringRes
import com.revolgenx.anilib.media.ui.viewmodel.MediaCharacterViewModel
import com.revolgenx.anilib.media.ui.viewmodel.MediaRecommendationViewModel
import com.revolgenx.anilib.media.ui.viewmodel.MediaReviewViewModel
import com.revolgenx.anilib.media.ui.viewmodel.MediaScreenPageType
import com.revolgenx.anilib.media.ui.viewmodel.MediaStaffViewModel
import com.revolgenx.anilib.media.ui.viewmodel.MediaStatsViewModel
import com.revolgenx.anilib.media.ui.viewmodel.MediaViewModel
import com.revolgenx.anilib.social.ui.viewmodel.ActivityUnionViewModel
import com.revolgenx.anilib.type.ActivityType
import com.revolgenx.anilib.type.MediaType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import anilib.i18n.R as I18nR


class MediaScreen(
    private val id: Int,
    private val mediaType: MediaType?
) : AndroidScreen() {
    @Composable
    override fun Content() {
        MediaScreenContent(id, mediaType ?: MediaType.ANIME)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MediaScreenContent(
    mediaId: Int,
    mediaType: MediaType,
) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: MediaViewModel = koinViewModel()
    val recommendationViewModel: MediaRecommendationViewModel = koinViewModel()
    val statsViewModel: MediaStatsViewModel = koinViewModel()
    val activityUnionViewModel: ActivityUnionViewModel = koinViewModel()
    val reviewViewModel: MediaReviewViewModel = koinViewModel()
    val staffViewModel: MediaStaffViewModel = koinViewModel()
    val characterViewModel: MediaCharacterViewModel = koinViewModel()

    val visiblePages by remember {
        derivedStateOf { viewModel.pages.filter { it.isVisible.value } }
    }
    val pagerState = rememberPagerState() { visiblePages.size }

    LaunchedEffect(viewModel) {
        viewModel.field.mediaId = mediaId
        recommendationViewModel.field.mediaId = mediaId
        statsViewModel.field.mediaId = mediaId
        reviewViewModel.field.mediaId = mediaId
        staffViewModel.field.mediaId = mediaId
        characterViewModel.field.mediaId = mediaId
        activityUnionViewModel.field.also {
            it.mediaId = mediaId
            it.type = ActivityType.MEDIA_LIST
        }
        viewModel.getResource()
    }


    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState(), canScroll = {
            pagerState.currentPage == MediaScreenPageType.OVERVIEW.ordinal
        })
    ScreenScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MediaScreenTopAppBar(
                mediaType = mediaType,
                mediaModel = viewModel.resource.value?.stateValue,
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = emptyWindowInsets()
    ) {
        PagerScreenScaffold(
            pages = visiblePages,
            pagerState = pagerState,
            navigationIcon = {},
            actions = {},
            contentWindowInsets = NavigationBarDefaults.windowInsets,
            windowInsets = emptyWindowInsets()
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                when (visiblePages[page].type) {
                    MediaScreenPageType.OVERVIEW -> MediaOverviewScreen(viewModel, mediaType,
                        recommendationScreen = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(MediaScreenPageType.RECOMMENDATIONS.ordinal)
                            }
                        })

                    MediaScreenPageType.RECOMMENDATIONS -> MediaRecommendationScreen(
                        recommendationViewModel
                    )

                    MediaScreenPageType.WATCH -> MediaWatchScreen(viewModel)
                    MediaScreenPageType.CHARACTER -> MediaCharacterScreen(
                        characterViewModel,
                        mediaType
                    )

                    MediaScreenPageType.STAFF -> MediaStaffScreen(staffViewModel)
                    MediaScreenPageType.REVIEW -> MediaReviewScreen(reviewViewModel)
                    MediaScreenPageType.STATS -> MediaStatsScreen(statsViewModel, mediaType)
                    MediaScreenPageType.SOCIAL -> MediaActivityUnionScreen(activityUnionViewModel)
                }

                when (visiblePages[page].type) {
                    MediaScreenPageType.OVERVIEW -> {}
                    else -> {
                        LaunchedEffect(scrollBehavior) {
                            scrollBehavior.state.collapse(scrollBehavior.snapAnimationSpec)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaScreenTopAppBar(
    mediaType: MediaType,
    mediaModel: MediaModel?,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val containerHeight = 320.dp
    CollapsingAppbar(
        scrollBehavior = scrollBehavior,
        containerHeight = containerHeight,
        containerContent = {
            MediaTopAppBarContainerContent(
                containerHeight = containerHeight,
                media = mediaModel,
                mediaType = mediaType,
                isCollapsed = it
            )
        },
        title = { isCollapsed ->
            if (isCollapsed) {
                MediaTitleType {
                    Text(
                        text = mediaModel?.title?.title(it)
                            ?: stringResource(id = I18nR.string.media),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }
        },
        actions = { isCollapsed ->
            mediaModel?.siteUrl?.let { site ->
                OverflowMenu(
                    tonalButton = !isCollapsed
                ) {
                    OpenInBrowserOverflowMenu(link = site)
                    ShareOverflowMenu(text = site)
                }
            }
        }
    )
}


@Composable
private fun BoxScope.MediaTopAppBarContainerContent(
    containerHeight: Dp,
    media: MediaModel?,
    mediaType: MediaType,
    isCollapsed: Boolean
) {
    val isAnime = mediaType.isAnime
    ImageAsync(
        modifier = Modifier
            .height(containerHeight)
            .fillMaxWidth(),
        imageUrl = media?.bannerImage,
        imageOptions = ImageOptions(
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        ),
        previewPlaceholder = R.drawable.bleach,
        viewable = true
    )

    val surfaceContainerLowest = MaterialTheme.colorScheme.surfaceContainerLowest
    Box(
        modifier = Modifier
            .height(containerHeight)
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        surfaceContainerLowest.copy(0.1f), surfaceContainerLowest
                    )
                )
            )
    ) {

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomStart),
            enter = fadeIn(),
            exit = fadeOut(),
            visible = !isCollapsed
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MediaCoverImageType {
                        ImageAsync(
                            modifier = Modifier
                                .size(width = 110.dp, height = 160.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            imageUrl = media?.coverImage?.image(it),
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center
                            ),
                            previewPlaceholder = R.drawable.bleach,
                            viewable = true
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Bottom),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        MediaTitleType {
                            SemiBoldText(
                                text = media?.title?.title(it).naText(),
                                fontSize = 20.sp,
                                lineHeight = 22.sp,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = LocalTextStyle.current.shadow(surfaceContainerLowest)
                            )
                        }



                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(12.dp),
                                imageVector = AppIcons.IcStar,
                                contentDescription = null
                            )
                            MediumText(
                                text = media?.popularity.orZero().prettyNumberFormat(),
                                fontSize = 14.sp,
                                lineHeight = 17.sp,
                                style = LocalTextStyle.current.shadow(surfaceContainerLowest)
                            )

                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                modifier = Modifier.size(12.dp),
                                imageVector = AppIcons.IcHeart,
                                contentDescription = null
                            )
                            MediumText(
                                text = media?.favourites.orZero().prettyNumberFormat(),
                                fontSize = 14.sp,
                                lineHeight = 17.sp,
                                style = LocalTextStyle.current.shadow(surfaceContainerLowest)
                            )
                        }


                        if (isAnime) {
                            val season =
                                media?.season?.toStringRes()?.let { stringResource(id = it) }
                                    .naText()


                            MediumText(
                                text = stringResource(id = I18nR.string.season_dot_year).format(
                                    season,
                                    media?.seasonYear.naText()
                                ),
                                fontSize = 14.sp,
                                lineHeight = 17.sp,
                                style = LocalTextStyle.current.shadow(surfaceContainerLowest)
                            )
                        }

                        media?.nextAiringEpisode?.let {
                            MediumText(
                                text = stringResource(id = I18nR.string.episode_airing_date).format(
                                    it.episode,
                                    it.airingAtModel.airingDateTime.naText()
                                ),
                                style = LocalTextStyle.current.shadow(surfaceContainerLowest),
                                fontSize = 16.sp,
                                lineHeight = 20.sp,
                            )
                        }

                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { /*TODO*/ }) {
                            Text(
                                text = media?.mediaListEntry?.status?.toStringRes(mediaType = mediaType)
                                    .let {
                                        stringResource(
                                            id = it ?: I18nR.string.add
                                        )
                                    }
                            )
                        }

                        ActionMenu(icon = AppIcons.IcMoreHoriz) {

                        }

                        ActionMenu(icon = AppIcons.IcReview) {

                        }

                        IconToggleButton(
                            checked = media?.isFavourite == true,
                            onCheckedChange = {
                                //todo mutation
                            }
                        ) {
                            Icon(
                                imageVector = if (media?.isFavourite == true) AppIcons.IcHeart else AppIcons.IcHeartOutline,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}