package com.revolgenx.anilib.common.ui.component.menu

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.revolgenx.anilib.common.data.tuples.MutablePair
import anilib.i18n.R as I18nR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectMenu(
    modifier: Modifier = Modifier,
    label: String? = null,
    entries: List<MutablePair<Boolean, String>>,
    onItemsSelected: (items: List<Pair<Int, String>>) -> Unit
) {
    val mutableEntries by remember {
        mutableStateOf(entries.map { mutableStateOf(it) })
    }
    val items = mutableEntries.map { it.value.second }
    fun getSelectedItems() = mutableEntries.filter { it.value.first }.map { it.value.second }
    fun getSelectedItemsWithIndex() = getSelectedItems().map { items.indexOf(it) to it }

    var expanded by remember { mutableStateOf(false) }
    var selectedItems by remember {
        mutableStateOf(getSelectedItems())
    }

    val shape = if (expanded)
        RoundedCornerShape(8.dp).copy(bottomEnd = CornerSize(0.dp), bottomStart = CornerSize(0.dp))
    else RoundedCornerShape(8.dp)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {

        val selectedItem = selectedItems.takeIf { it.isNotEmpty() }?.joinToString(", ")
            ?: stringResource(id = I18nR.string.none)
        TextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = selectedItem,
            onValueChange = {},
            shape = shape,
            label = { label?.let { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
        )


        DropdownMenu(
            modifier = modifier
                .exposedDropdownSize(),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            mutableEntries.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s.value.second) },
                    onClick = {
                        s.value = s.value.copy(first = !s.value.first)
                        selectedItems = getSelectedItems()
                        onItemsSelected.invoke(getSelectedItemsWithIndex())
                    },
                    leadingIcon = {
                        if (s.value.first) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null
                            )
                        }
                    })
            }

        }
    }
}
