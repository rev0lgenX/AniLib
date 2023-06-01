package com.revolgenx.anilib.media.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.ext.naText
import com.revolgenx.anilib.common.ui.component.common.MediaCoverImageType
import com.revolgenx.anilib.common.ui.component.common.MediaTitleType
import com.revolgenx.anilib.common.ui.component.image.AsyncImage
import com.revolgenx.anilib.common.ui.component.text.LightText
import com.revolgenx.anilib.common.ui.component.text.MediumText
import com.revolgenx.anilib.common.util.OnClick
import com.revolgenx.anilib.media.ui.model.MediaModel
import com.revolgenx.anilib.media.ui.model.toColor
import com.revolgenx.anilib.media.ui.model.toStringRes
import com.skydoves.landscapist.ImageOptions

@Composable
fun MediaItemCard(mediaModel: MediaModel, width: Dp? = null, onClick: (id: Int) -> Unit) {
    Card(
        modifier = Modifier
            .let {
                if (width != null) {
                    it.width(width)
                } else {
                    it.fillMaxWidth()
                }
            }
            .height(236.dp)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    onClick(mediaModel.id)
                },
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            MediaItemContent(mediaModel)
            val format = mediaModel.format?.let {
                stringArrayResource(id = R.array.media_format)[it.ordinal]
            }.naText()
            val year = mediaModel.seasonYear.naText()
            Box(
                modifier = Modifier
                    .padding(vertical = 2.dp, horizontal = 4.dp),
            ) {
                LightText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.format_year).format(format, year)
                )
            }
        }
    }
}

@Composable
fun MediaItemContent(mediaModel: MediaModel) {
    Column {
        MediaCoverImageType { type ->
            AsyncImage(
                modifier = Modifier
                    .height(165.dp)
                    .fillMaxWidth(),
                imageUrl = mediaModel.coverImage?.image(type),
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                ),
                previewPlaceholder = R.drawable.bleach
            )
        }

        MediaTitleType { type ->
            MediumText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 4.dp),
                text = mediaModel.title?.title(type).naText()
            )
        }
    }
}


@Composable
fun MediaItemRowContent(
    media: MediaModel,
    content: @Composable () -> Unit = {},
    onClick: OnClick
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.spacedBy(
            6.dp,
            alignment = Alignment.Start
        )
    ) {
        MediaCoverImageType { type ->
            AsyncImage(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(72.dp),
                imageUrl = media.coverImage?.image(type),
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                ),
                previewPlaceholder = R.drawable.bleach
            )
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 2.dp)
        ) {

            MediaTitleType { type ->
                MediumText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = media.title?.title(type).naText(),
                )
            }


            Text(
                stringResource(id = media.status.toStringRes()),
                color = media.status.toColor(),
                fontSize = 12.sp
            )

            val format = media.format.toStringRes()?.let {
                stringResource(id = it)
            }.naText()
            val year = media.seasonYear.naText()

            LightText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.format_year).format(format, year),
            )

            content()
        }
    }
}
