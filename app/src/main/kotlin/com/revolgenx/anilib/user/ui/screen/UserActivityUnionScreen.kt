package com.revolgenx.anilib.user.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.ext.activityComposerScreen
import com.revolgenx.anilib.common.ext.emptyWindowInsets
import com.revolgenx.anilib.common.ui.component.action.ActionMenu
import com.revolgenx.anilib.common.ui.component.action.DisappearingFAB
import com.revolgenx.anilib.common.ui.component.appbar.AppBar
import com.revolgenx.anilib.common.ui.component.bottombar.BottomNestedScrollConnection
import com.revolgenx.anilib.common.ui.component.bottombar.ScrollState
import com.revolgenx.anilib.common.ui.component.scaffold.ScreenScaffold
import com.revolgenx.anilib.common.ui.composition.localNavigator
import com.revolgenx.anilib.common.ui.theme.colorScheme
import com.revolgenx.anilib.social.ui.screen.ActivityUnionScreenContent
import com.revolgenx.anilib.social.ui.viewmodel.ActivityUnionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserActivityUnionScreen(viewModel: ActivityUnionViewModel) {
    val navigator = localNavigator()
    val scrollState = remember { mutableStateOf<ScrollState>(ScrollState.ScrollDown) }
    val bottomScrollConnection =
        remember { BottomNestedScrollConnection(state = scrollState) }
    ScreenScaffold(
        floatingActionButton = {
            DisappearingFAB(
                scrollState = scrollState,
                content = {
                    Row(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .padding(4.dp)
                    ) {
                        ActionMenu(iconRes = R.drawable.ic_filter) {
                        }
                        Divider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .padding(vertical = 10.dp),
                            color = colorScheme().onSurfaceVariant
                        )
                        ActionMenu(iconRes = R.drawable.ic_create) {
                            navigator.activityComposerScreen()
                        }
                    }
                })
        },
        topBar = {},
        navigationIcon = {},
        contentWindowInsets = emptyWindowInsets()
    ) {
        Box(
            modifier = Modifier
                .nestedScroll(bottomScrollConnection)
        ) {
            ActivityUnionScreenContent(viewModel = viewModel)
        }
    }
}