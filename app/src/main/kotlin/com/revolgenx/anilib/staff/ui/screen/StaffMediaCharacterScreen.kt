package com.revolgenx.anilib.staff.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.navigator.Navigator
import com.revolgenx.anilib.common.ext.characterScreen
import com.revolgenx.anilib.common.ui.component.action.DisappearingFAB
import com.revolgenx.anilib.common.ui.component.bottombar.BottomNestedScrollConnection
import com.revolgenx.anilib.common.ui.component.bottombar.ScrollState
import com.revolgenx.anilib.common.ui.component.scaffold.ScreenScaffold
import com.revolgenx.anilib.common.ui.compose.paging.LazyPagingList
import com.revolgenx.anilib.common.ui.composition.localNavigator
import com.revolgenx.anilib.common.ui.icons.AppIcons
import com.revolgenx.anilib.common.ui.icons.appicon.IcFilter
import com.revolgenx.anilib.common.ui.viewmodel.collectAsLazyPagingItems
import com.revolgenx.anilib.media.ui.component.MediaComponentState
import com.revolgenx.anilib.media.ui.component.rememberMediaComponentState
import com.revolgenx.anilib.staff.ui.component.StaffMediaCharacterCard
import com.revolgenx.anilib.staff.ui.viewmodel.StaffMediaCharacterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffMediaCharacterScreen(viewModel: StaffMediaCharacterViewModel) {
    val navigator = localNavigator()
    val scrollState = remember { mutableStateOf<ScrollState>(ScrollState.ScrollDown) }
    val bottomScrollConnection =
        remember { BottomNestedScrollConnection(state = scrollState) }

    ScreenScaffold(
        topBar = {},
        floatingActionButton = {
            DisappearingFAB(scrollState = scrollState, icon = AppIcons.IcFilter) {
                //todo filter
            }
        },
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
    ) {
        val mediaComponentState = rememberMediaComponentState(navigator = navigator)
        StaffMediaCharacterPagingContent(viewModel, bottomScrollConnection, mediaComponentState, navigator)
    }
}

@Composable
private fun StaffMediaCharacterPagingContent(
    viewModel: StaffMediaCharacterViewModel,
    bottomScrollConnection: BottomNestedScrollConnection,
    mediaComponentState: MediaComponentState,
    navigator: Navigator
) {
    val pagingItems = viewModel.collectAsLazyPagingItems()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(bottomScrollConnection)
    ) {
        LazyPagingList(
            pagingItems = pagingItems,
            onRefresh = {
                viewModel.refresh()
            },
        ) { model ->
            model ?: return@LazyPagingList
            StaffMediaCharacterCard(
                mediaModel = model,
                character = model.character,
                mediaComponentState = mediaComponentState,
                onCharacterClick = {
                    navigator.characterScreen(it)
                }
            )
        }
    }
}