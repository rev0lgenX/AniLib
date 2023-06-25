package com.revolgenx.anilib.media.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.marker.Marker
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.ext.prettyNumberFormat
import com.revolgenx.anilib.common.ui.component.chart.rememberMarker
import com.revolgenx.anilib.common.ui.component.common.Grid
import com.revolgenx.anilib.common.ui.model.stats.StatusDistributionModel
import com.revolgenx.anilib.common.ui.screen.state.ResourceScreen
import com.revolgenx.anilib.list.ui.model.toColorRes
import com.revolgenx.anilib.list.ui.model.toStringRes
import com.revolgenx.anilib.media.ui.viewmodel.MediaStatsViewModel
import com.revolgenx.anilib.type.MediaRankType
import com.revolgenx.anilib.type.MediaType

@Composable
fun MediaStatsScreen(viewModel: MediaStatsViewModel, mediaType: MediaType) {
    LaunchedEffect(viewModel) {
        viewModel.getResource()
    }

    ResourceScreen(
        resourceState = viewModel.resource.value,
        refresh = { viewModel.refresh() }) { statsModel ->
        ProvideChartStyle(m3ChartStyle()) {
            val marker = rememberMarker()

            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                statsModel.rankings?.let {
                    HeaderText(text = stringResource(id = R.string.rankings))
                    Grid(items = it) { rank ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(66.dp)
                                .padding(3.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val rankDrawable =
                                    if (rank.rankType == MediaRankType.POPULAR) R.drawable.ic_heart else R.drawable.ic_star
                                val rankColor =
                                    if (rank.rankType == MediaRankType.RATED) R.color.rank_type_popular else R.color.rank_type_rated
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(id = rankDrawable),
                                    contentDescription = null,
                                    tint = colorResource(
                                        id = rankColor
                                    )
                                )
                                val seasons = stringArrayResource(id = R.array.media_season)
                                val rankText = remember {
                                    (rank.rank?.let { "#$it " } ?: "") +
                                            (rank.context?.trim()?.split(" ")
                                                ?.joinToString(separator = " ") { it.replaceFirstChar { it.uppercase() } }
                                                    + " ") +
                                            (rank.season?.let { seasons[it.ordinal] + " " } ?: "") +
                                            (rank.year ?: "")
                                }

                                Text(
                                    text = rankText,
                                    fontSize = 14.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    HeaderText(text = stringResource(id = R.string.recent_activity_per_day))
                    LineChart(marker = marker, model = statsModel.trendsEntries)

                    HeaderText(text = stringResource(id = R.string.airing_score_progression))
                    LineChart(marker = marker, model = statsModel.airingScoreProgressionEntries)

                    HeaderText(text = stringResource(id = R.string.airing_watchers_progression))
                    LineChart(marker = marker, model = statsModel.airingWatchersProgressionEntries)

                    statsModel.statusDistribution?.let { statusDistribution ->
                        HeaderText(text = stringResource(id = R.string.status_distribution))
                        Grid(
                            items = statusDistribution,
                            columnSpacing = 3.dp,
                            rowSpacing = 3.dp
                        ) { item ->
                            val statusColor =
                                item.status.toColorRes().let { color -> colorResource(id = color) }
                            val listStatus = item.status.toStringRes(mediaType)
                                .let { str -> stringResource(id = str) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .background(color = statusColor)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterStart),
                                    text = stringResource(id = R.string.status_distribution_string).format(
                                        item.amount.prettyNumberFormat(),
                                        listStatus
                                    ),
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                        ) {
                            val statusTotalAmount = remember {
                                statusDistribution.sumOf(StatusDistributionModel::amount).toFloat()
                            }
                            statusDistribution.forEach { item ->
                                val weight = item.amount.div(statusTotalAmount).times(100f)
                                Box(
                                    modifier = Modifier
                                        .weight(weight)
                                        .fillMaxHeight()
                                        .background(
                                            color = item.status
                                                .toColorRes()
                                                .let { color -> colorResource(id = color) })
                                )

                            }
                        }
                    }


                    HeaderText(text = stringResource(id = R.string.score_distribution))
                    ColumnChart(marker = marker, model = statsModel.scoreDistributionEntry)
                }
            }
        }
    }
}

@Composable
private fun HeaderText(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp
    )
}

@Composable
private fun LineChart(marker: Marker, model: ChartEntryModel) {
    Chart(
        marker = marker,
        chart = lineChart(),
        model = model,
        startAxis = startAxis(
            valueFormatter = { value, _ -> value.toInt().toString() }
        ),
        bottomAxis = bottomAxis(),
    )
}


@Composable
private fun ColumnChart(marker: Marker, model: ChartEntryModel) {
    Chart(
        marker = marker,
        chart = columnChart(
            spacing =  12.dp,
            innerSpacing = 2.dp
        ),
        model = model,
        startAxis = startAxis(
            valueFormatter = { value, _ -> value.toInt().toString() }
        ),
        bottomAxis = bottomAxis(),
    )
}