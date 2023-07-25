package com.revolgenx.anilib.common.ui.screen.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.revolgenx.anilib.common.data.field.BaseField
import com.revolgenx.anilib.common.data.state.ResourceState
import com.revolgenx.anilib.common.ui.viewmodel.ResourceViewModel
import com.revolgenx.anilib.common.util.OnClick

@Composable
fun <T> ResourceScreen(
    resourceState: ResourceState<T>?,
    loading: MutableState<Boolean> = mutableStateOf(false),
    refresh: OnClick,
    content: @Composable (data: T) -> Unit
) {

    if (loading.value) {
        LinearLoadingSection()
    }
    when (resourceState) {
        is ResourceState.Error -> ErrorScreen(error = resourceState.message) {
            refresh.invoke()
        }

        is ResourceState.Loading -> LoadingScreen()

        is ResourceState.Success -> {
            val data = resourceState.data ?: return
            content(data)
        }

        else -> {}
    }
}

@Composable
fun <M : Any, F : BaseField<*>> ResourceScreen(
    viewModel: ResourceViewModel<M, F>,
    loading: MutableState<Boolean> = mutableStateOf(false),
    content: @Composable (data: M) -> Unit
) {
    if (loading.value) {
        LinearLoadingSection()
    }

    when (val resourceState = viewModel.resource.value) {
        is ResourceState.Error -> ErrorScreen(error = resourceState.message) {
        }

        is ResourceState.Loading -> LoadingScreen()

        is ResourceState.Success -> {
            val data = resourceState.data ?: return
            content(data)
        }

        else -> {}
    }
}