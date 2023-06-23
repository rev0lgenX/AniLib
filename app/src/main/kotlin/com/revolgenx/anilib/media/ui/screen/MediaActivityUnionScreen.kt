package com.revolgenx.anilib.media.ui.screen

import androidx.compose.runtime.Composable
import com.revolgenx.anilib.social.ui.screen.ActivityUnionScreenContent
import com.revolgenx.anilib.social.ui.viewmodel.ActivityUnionViewModel
import com.revolgenx.anilib.type.ActivityType
import org.koin.androidx.compose.koinViewModel

@Composable
fun MediaActivityUnionScreen(
    viewModel: ActivityUnionViewModel = koinViewModel()
) {
    viewModel.field.also {
        it.type = ActivityType.MEDIA_LIST
    }
    ActivityUnionScreenContent(viewModel = viewModel)
}