package com.teamon.app.utils.graphics

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamon.app.R

sealed class TasksSortingOption(
    var title: String,
    var icon: Int
) {
    data object ProjectName :
        TasksSortingOption(
            "Project Name",
            R.drawable.round_text_fields_24
        )

    data object TaskName :
        TasksSortingOption(
            "Task Name",
            R.drawable.round_text_fields_24
        )

    data object Deadline :
        TasksSortingOption(
            "Deadline",
            R.drawable.round_calendar_today_24
        )

    data object Members :
        TasksSortingOption(
            "Total Members",
            R.drawable.round_groups_24
        )

    data object Status :
        TasksSortingOption(
            "Status",
            R.drawable.round_mode_state_24
        )

    data object Priority :
        TasksSortingOption(
            "Priority",
            R.drawable.round_priority_high_24
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksSortingOptionsDropdownMenu(
    sortExpanded: Boolean,
    sortingOrder: Boolean,
    onSortingOrderChange: (Boolean) -> Unit,
    onSortExpandedChange: (Boolean) -> Unit,
    onMainExpandedChange: (Boolean) -> Unit,
    sortingOption: String,
    onSortingOptionChange: (TasksSortingOption) -> Unit
) {
    DropdownMenu(
        expanded = sortExpanded,
        onDismissRequest = { onSortExpandedChange(false) }
    ) {
        if (sortingOrder || sortingOption != TasksSortingOption.Deadline.title) {
            DropdownMenuItem(text = {
                Text(
                    "Reset options",
                    color = MaterialTheme.colorScheme.error
                )
            },
                leadingIcon = {},
                onClick = {
                    onSortingOrderChange(true); onSortingOptionChange(TasksSortingOption.Deadline); onMainExpandedChange(
                    false
                )
                })
            HorizontalDivider(modifier = Modifier.padding(10.dp))
        }
        Column {
            Text(
                text = "Sort by",
                modifier = Modifier.padding(15.dp, 10.dp, 0.dp, 0.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            listOf(
                TasksSortingOption.ProjectName,
                TasksSortingOption.TaskName,
                TasksSortingOption.Deadline,
                TasksSortingOption.Members,
                TasksSortingOption.Status,
                TasksSortingOption.Priority
            )
                .forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.title) },
                        trailingIcon = {
                            Image(
                                painterResource(id = item.icon),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        },
                        leadingIcon = {
                            if (sortingOption == item.title) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = "Sorting option selected"
                                )
                            }
                        },
                        onClick = {
                            onSortingOptionChange(item)
                            onMainExpandedChange(false)
                            onSortExpandedChange(false)
                        }
                    )
                }
        }
        HorizontalDivider()
        Column {
            Text(
                text = "Order",
                modifier = Modifier.padding(15.dp, 10.dp, 0.dp, 0.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                SegmentedButton(selected = !sortingOrder,
                    onClick = { onSortingOrderChange(sortingOrder) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    label = {
                        Image(
                            painterResource(id = R.drawable.rounded_arrow_downward_alt_24),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                            contentDescription = "Ascending order",
                        )
                    })
                SegmentedButton(selected = sortingOrder,
                    onClick = { onSortingOrderChange(sortingOrder) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    label = {
                        Image(
                            painterResource(id = R.drawable.rounded_arrow_upward_alt_24),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                            contentDescription = "Descending order"
                        )
                    })
            }
        }
    }
}