package com.revolgenx.anilib.media.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.data.constant.AlMediaSort
import com.revolgenx.anilib.common.data.tuples.to
import com.revolgenx.anilib.common.ui.component.action.BottomSheetConfirmationAction
import com.revolgenx.anilib.common.ui.component.menu.DropdownMenu
import com.revolgenx.anilib.common.ui.component.menu.MultiSelectDropdownMenu
import com.revolgenx.anilib.common.ui.component.menu.SortDropdownMenu
import com.revolgenx.anilib.common.ui.component.menu.AlSortMenuItem
import com.revolgenx.anilib.common.ui.component.menu.AlSortOrder
import com.revolgenx.anilib.media.data.field.MediaField
import com.revolgenx.anilib.media.ui.model.toMediaFormat
import com.revolgenx.anilib.media.ui.model.toMediaSeason
import com.revolgenx.anilib.media.ui.model.toMediaStatus
import com.revolgenx.anilib.type.MediaSort
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

class MediaFilterBottomSheetScreen : AndroidScreen() {
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<MediaFilterBottomSheetViewModel>()
        val windowInsets = WindowInsets.systemBars
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(windowInsets)
        ) {
            MediaFilterBottomSheetContent(
                field = viewModel.field,
                onPositiveClicked = {
                    viewModel.updateFilter()
                    bottomSheetNavigator.hide()
                },
                onNegativeClicked = {
                    bottomSheetNavigator.hide()
                })
        }
    }
}


private val yearLesser = Calendar.getInstance().get(Calendar.YEAR) + 2
private val yearGreater = 1940
private val yearList by lazy {
    (yearLesser downTo yearGreater).map { it.toString() }
}


@Composable
private fun MediaFilterBottomSheetContent(
    modifier: Modifier = Modifier,
    field: MediaField,
    onNegativeClicked: (() -> Unit)? = null,
    onPositiveClicked: (() -> Unit)? = null,
    dismiss: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp)
    ) {
        BottomSheetConfirmationAction(
            onPositiveClicked = {
                onPositiveClicked?.invoke()
                dismiss?.invoke()
            },
            onNegativeClicked = {
                onNegativeClicked?.invoke()
                dismiss?.invoke()
            }
        )

        LazyColumn() {
            item {
                val selectedFormats = field.formatsIn?.map { it.ordinal } ?: emptyList()
                val formats = stringArrayResource(id = R.array.media_format)
                MultiSelectDropdownMenu(
                    labelRes = R.string.format,
                    entries = formats.mapIndexed { index, s ->
                        selectedFormats.contains(
                            index
                        ) to s
                    },
                ) { selectedItems ->
                    field.formatsIn = selectedItems.takeIf { it.isNotEmpty() }
                        ?.mapNotNull { it.first.toMediaFormat() }
                }
            }
            item {
                DropdownMenu(
                    labelRes = R.string.status,
                    entries = stringArrayResource(id = R.array.media_status).toList(),
                    selectedItemPosition = field.status?.ordinal,
                    showNoneItem = true
                ) { selectedItem ->
                    field.status = selectedItem.takeIf { it > -1 }?.toMediaStatus()
                }
            }
            item {
                DropdownMenu(
                    labelRes = R.string.season,
                    entries = stringArrayResource(id = R.array.media_season).toList(),
                    selectedItemPosition = field.season?.ordinal
                ) { selectedItem ->
                    field.season = selectedItem.takeIf { it > -1 }?.toMediaSeason()
                }
            }
            item {
                val sort = field.sort
                var selectedSortIndex: Int? = null
                var selectedSortOrder: AlSortOrder = AlSortOrder.NONE

                if (sort != null) {
                    val isDesc = sort.rawValue.endsWith("_DESC")
                    val alMediaSort =
                        AlMediaSort.from(if (isDesc) sort.ordinal - 1 else sort.ordinal)
                    selectedSortIndex = alMediaSort?.ordinal
                    selectedSortOrder = if (isDesc) AlSortOrder.DESC else AlSortOrder.ASC
                }

                val sortMenus =
                    stringArrayResource(id = R.array.media_sort).mapIndexed { index, s ->
                        AlSortMenuItem(
                            s,
                            if (index == selectedSortIndex) selectedSortOrder else AlSortOrder.NONE
                        )
                    }

                SortDropdownMenu(
                    labelRes = R.string.sort,
                    entries = sortMenus,
                ) { index, selectedItem ->
                    var mediaSort: MediaSort? = null

                    if (selectedItem != null) {
                        val alMediaSort = AlMediaSort.values()[index].sort
                        val selectedSort = if (selectedItem.order == AlSortOrder.DESC) {
                            alMediaSort + 1
                        } else {
                            alMediaSort
                        }
                        mediaSort = MediaSort.values()[selectedSort]
                    }

                    field.sort = mediaSort
                }
            }
            item {
                DropdownMenu(
                    labelRes = R.string.year,
                    entries = yearList,
                    selectedItemPosition = field.seasonYear?.takeIf { it in yearGreater..yearLesser }
                        ?.let { yearList.indexOf(it.toString()) }
                ) { selectedItem ->
                    field.seasonYear =
                        selectedItem.takeIf { it > -1 }?.let { yearList[it].toInt() }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaFilterModalBottomSheet(
    openBottomSheet: MutableState<Boolean> = mutableStateOf(false),
    bottomSheetState: SheetState = rememberModalBottomSheetState(),
    viewModel: MediaFilterBottomSheetViewModel = koinViewModel()
) {
    if (openBottomSheet.value) {
        val windowInsets = NavigationBarDefaults.windowInsets
        ModalBottomSheet(
            onDismissRequest = { openBottomSheet.value = false },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            MediaFilterBottomSheetContent(
                modifier = Modifier
                    .windowInsetsPadding(windowInsets),
                field = viewModel.field,
                onPositiveClicked = {
                    viewModel.updateFilter()
                }
            ) {
                openBottomSheet.value = false
            }
        }
    }
}

