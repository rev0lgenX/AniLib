package com.revolgenx.anilib.common.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun LoadingScreen() {
    LoadingLayout(modifier = Modifier.fillMaxSize())
}

@Composable
fun LazyItemScope.LoadingScreen() {
    LoadingLayout(modifier = Modifier.fillParentMaxSize())
}

@Composable
fun LoadingSection(
    modifier: Modifier = Modifier,
) {
    LoadingLayout(modifier = modifier.fillMaxWidth())
}

@Composable
fun LoadingLayout(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}