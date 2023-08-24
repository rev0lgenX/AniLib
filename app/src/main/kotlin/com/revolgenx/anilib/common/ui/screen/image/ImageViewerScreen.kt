package com.revolgenx.anilib.common.ui.screen.image

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.imageLoader
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.ext.localContext
import com.revolgenx.anilib.common.ui.component.appbar.AppBarDefaults
import com.revolgenx.anilib.common.ui.component.appbar.AppBarLayoutDefaults
import com.revolgenx.anilib.common.ui.component.image.ImageAsync
import com.revolgenx.anilib.common.ui.component.scaffold.ScreenScaffold
import com.revolgenx.anilib.common.ui.component.image.ImageOptions


class ImageViewerScreen(private val url: String) : AndroidScreen() {
    @Composable
    override fun Content() {
        ImageViewerScreenContent(imageUrl = url)
    }
}

private var imageLoader: ImageLoader? = null


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageViewerScreenContent(imageUrl: String) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = rememberSaveable { systemUiController.systemBarsDarkContentEnabled }
    DisposableEffect(systemUiController, useDarkIcons) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false,
            isNavigationBarContrastEnforced = false
        )
        onDispose {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = useDarkIcons,
                isNavigationBarContrastEnforced = false
            )
        }
    }
    ScreenScaffold(
        containerColor = Color.Black,
        appBarLayoutColors = AppBarLayoutDefaults.appBarLayoutColors(Color.Black, Color.Black),
        appBarColors = AppBarDefaults.appBarColors(
            containerColor = Color.Black,
            navigationIconContentColor = Color.White,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    ) {

        val context = localContext()
        imageLoader = imageLoader ?: context.imageLoader.newBuilder()
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()


        val loading = remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            ImageAsync(
                modifier = Modifier.fillMaxSize(),
                imageUrl = imageUrl,
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center
                ),
                onLoading = {
                    loading.value = true
                },
                onSuccess = {
                    loading.value = false
                },
                onError = {
                    loading.value = false
                },
                imageLoader = imageLoader
            )

            if (loading.value) {
                Box(modifier = Modifier.matchParentSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

