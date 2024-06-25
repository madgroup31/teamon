package com.teamon.app.utils.graphics

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamon.app.R

sealed class TasksDeadlineFilteringOptions(
    var title: String,
    var icon: Int
) {

    data object All :
        TasksDeadlineFilteringOptions(
            "All",
            R.drawable.round_do_not_disturb_alt_24
        )

    data object NotStarted:
    TasksDeadlineFilteringOptions(
        "Not Started",
        R.drawable.round_start_24
    )

    data object Overdue :
        TasksDeadlineFilteringOptions(
            "Overdue",
            R.drawable.round_error_outline_24
        )

    data object InTime :
        TasksDeadlineFilteringOptions(
            "In Time",
            R.drawable.round_access_time_24
        )
}

sealed class TasksStatusFilteringOptions(
    var title: String,
    var icon: Int
) {

    data object All :
        TasksStatusFilteringOptions(
            "All",
            R.drawable.round_do_not_disturb_alt_24
        )

    data object Completed :
        TasksStatusFilteringOptions(
            "Completed",
            R.drawable.round_check_circle_outline_24
        )

    data object InProgress :
        TasksStatusFilteringOptions(
            "In Progress",
            R.drawable.outline_pending_24
        )

    data object Pending :
        TasksStatusFilteringOptions(
            "Pending",
            R.drawable.rounded_pending_24
        )

    data object OnHold :
        TasksStatusFilteringOptions(
            "On Hold",
            R.drawable.round_on_hold_24
        )
}

sealed class TasksPriorityFilteringOptions(
    var title: String,
    var icon: Int
) {

    data object All :
        TasksPriorityFilteringOptions(
            "All",
            R.drawable.round_do_not_disturb_alt_24
        )

    data object Low :
        TasksPriorityFilteringOptions(
            "Low",
            R.drawable.round_priority_high_24
        )

    data object Medium :
        TasksPriorityFilteringOptions(
            "Medium",
            R.drawable.round_priority_high_24
        )

    data object High :
        TasksPriorityFilteringOptions(
            "High",
            R.drawable.round_priority_high_24
        )
}

@Composable
fun TasksFilteringOptionsDropdownMenu(
    filterExpanded: Boolean, onFilterExpandedChange: (Boolean) -> Unit,
    deadlineFilter: String, onDeadlineFilterChange: (TasksDeadlineFilteringOptions) -> Unit,
    statusFilter: String, onStatusFilterChange: (TasksStatusFilteringOptions) -> Unit,
    priorityFilter: String, onPriorityFilterChange: (TasksPriorityFilteringOptions) -> Unit,
    tagQuery: String, onTagQueryChange: (String) -> Unit,
    memberQuery: String, onMemberQueryChange: (String) -> Unit
) {
    DropdownMenu(
        expanded = filterExpanded,
        onDismissRequest = { onFilterExpandedChange(false) }
    ) {
        val deadlineOptions = listOf(
            TasksDeadlineFilteringOptions.All,
            TasksDeadlineFilteringOptions.NotStarted,
            TasksDeadlineFilteringOptions.InTime,
            TasksDeadlineFilteringOptions.Overdue
        )
        val statusOptions = listOf(
            TasksStatusFilteringOptions.All,
            TasksStatusFilteringOptions.Completed,
            TasksStatusFilteringOptions.InProgress,
            TasksStatusFilteringOptions.Pending,
            TasksStatusFilteringOptions.OnHold
        )
        val priorityOptions = listOf(
            TasksPriorityFilteringOptions.All,
            TasksPriorityFilteringOptions.Low,
            TasksPriorityFilteringOptions.Medium,
            TasksPriorityFilteringOptions.High
        )
        if (deadlineFilter != TasksDeadlineFilteringOptions.All.title
            || statusFilter != TasksStatusFilteringOptions.All.title
            || priorityFilter != TasksPriorityFilteringOptions.All.title
            || tagQuery.isNotBlank()
            || memberQuery.isNotBlank()
        ) {
            DropdownMenuItem(text = {
                Text(
                    "Reset filters",
                    color = MaterialTheme.colorScheme.error
                )
            },
                leadingIcon = {},
                onClick = {
                    onDeadlineFilterChange(TasksDeadlineFilteringOptions.All)
                    onStatusFilterChange(TasksStatusFilteringOptions.All)
                    onPriorityFilterChange(TasksPriorityFilteringOptions.All)
                    onTagQueryChange("")
                    onMemberQueryChange("")
                })
            HorizontalDivider(modifier = Modifier.padding(10.dp))
        }
        Column {
                Text(
                    text = "Deadline",
                    modifier = Modifier.padding(15.dp, 10.dp, 0.dp, 0.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            deadlineOptions.forEach {
                DropdownMenuItem(
                    text = { Text(it.title) },
                    leadingIcon = {
                        if (deadlineFilter == it.title) Icon(
                            Icons.Rounded.Check,
                            contentDescription = it.title
                        )
                    },
                    trailingIcon = {
                        if (it.title != TasksDeadlineFilteringOptions.All.title) Image(
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                            painter = painterResource(id = it.icon),
                            contentDescription = it.title
                        )
                    },
                    onClick = { onDeadlineFilterChange(it) })
            }
        }
        HorizontalDivider(modifier = Modifier.padding(10.dp))
        Column {
            Text(
                text = "Status",
                modifier = Modifier.padding(15.dp, 10.dp, 0.dp, 0.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            statusOptions.forEach {
                DropdownMenuItem(
                    text = { Text(it.title) },
                    leadingIcon = {
                        if (statusFilter == it.title) Icon(
                            Icons.Rounded.Check,
                            contentDescription = it.title
                        )
                    },
                    trailingIcon = {
                        if (it.title != TasksStatusFilteringOptions.All.title) Image(
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                            painter = painterResource(
                                id = it.icon
                            ), contentDescription = it.title
                        )
                    },
                    onClick = { onStatusFilterChange(it) })
            }
        }
        HorizontalDivider(modifier = Modifier.padding(10.dp))
        Column {
                OutlinedTextField(
                    label = { Text("Tag")},
                    modifier = Modifier
                        .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp)
                        .width(150.dp),
                    placeholder = { Text("All", fontStyle = FontStyle.Italic)},
                    value = tagQuery, onValueChange = {onTagQueryChange(it)},
                    maxLines = 1)

        }
        HorizontalDivider(modifier = Modifier.padding(10.dp))
        Column {
                OutlinedTextField(
                    label = { Text("Member") },
                    modifier = Modifier
                        .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp)
                        .width(150.dp),
                    placeholder = { Text("All", fontStyle = FontStyle.Italic)},
                    value = memberQuery, onValueChange = {onMemberQueryChange(it)},
                    maxLines = 1)

        }

        HorizontalDivider(modifier = Modifier.padding(10.dp))
        Column {
                Text(
                    text = "Priority",
                    modifier = Modifier.padding(15.dp, 10.dp, 0.dp, 0.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            priorityOptions.forEach {
                DropdownMenuItem(
                    text = { Text(it.title) },
                    leadingIcon = {
                        if (priorityFilter == it.title) Icon(
                            Icons.Rounded.Check,
                            contentDescription = it.title
                        )
                    },
                    onClick = { onPriorityFilterChange(it) })
            }
        }

    }
}