package com.teamon.app.utils.graphics

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.profileViewModel
import com.teamon.app.teamOnViewModel
import com.teamon.app.tasks.TaskPriority
import com.teamon.app.utils.classes.Task
import com.teamon.app.tasks.TaskStatus
import com.teamon.app.tasksViewModel
import com.teamon.app.usersViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TasksViewDropdownMenu(
    mainExpanded: Boolean,
    onMainExpandedChange: (Boolean) -> Unit,
    sortExpanded: Boolean,
    onSortExpandedChange: (Boolean) -> Unit,
    filterExpanded: Boolean,
    onFilterExpandedChange: (Boolean) -> Unit
) {
    DropdownMenu(
        modifier = Modifier,
        expanded = mainExpanded,
        onDismissRequest = { onMainExpandedChange(false) }) {
        DropdownMenuItem(
            text = { Text("Sort") },
            leadingIcon = {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Expand sorting options"
                )
            }, onClick = { onSortExpandedChange(true) },
            trailingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.round_sort_24),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                    contentDescription = "Sorting icon"
                )
            }
        )
        DropdownMenuItem(
            text = { Text("Filter") },
            leadingIcon = {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Expand filtering options"
                )
            },
            trailingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.round_filter_list_24),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                    contentDescription = "Filtering icon"
                )
            },
            onClick = { onFilterExpandedChange(true) }
        )
    }
}

@Composable
fun TaskCardDropdownMenu(
    selectedTabItem: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    actions: Actions,
    taskId: String,
    projectId: String,
    admins: List<String>,
    onTaskDelete: (String)-> Unit) {

    var deletingTask by remember { mutableStateOf(false) }
    LaunchedEffect(deletingTask) {
        if(deletingTask) {
            tasksViewModel!!.deleteTask(projectId,taskId)
            deletingTask = false
        }
    }

    var showingDeletionDialog by remember { mutableStateOf(false)}
    DropdownMenu(
        modifier = Modifier,
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) }
    ) {
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = "Update task") },
            text = { Text("Update") },
            onClick = { actions.editTask(selectedTabItem, taskId) }
        )
        DropdownMenuItem(
            leadingIcon = { Image(painter = painterResource(id = R.drawable.outline_comment_24), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface), contentDescription = "Add a comment") },
            text = { Text("Add a comment") },
            onClick = { actions.openTaskComments(selectedTabItem, taskId) }
        )
        DropdownMenuItem(
            leadingIcon = { Image(painter = painterResource(id = R.drawable.outline_content_paste_24), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),  contentDescription = "Add attachment") },
            text = { Text("Add an attachment") },
            onClick = { actions.openTaskAttachments(selectedTabItem, taskId) }
        )

        if(admins != null && admins.any { it == profileViewModel!!.userId }) {
            HorizontalDivider()
            DropdownMenuItem(
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.round_delete_24),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
                        contentDescription = "Delete task"
                    )
                },
                text = { Text("Delete task", color = MaterialTheme.colorScheme.error) },
                onClick = { showingDeletionDialog = true }
            )
            if (showingDeletionDialog) {
                AlertDialog(
                    onDismissRequest = { showingDeletionDialog = false },
                    title = { Text(text = "Are you sure you want to delete the task?") },
                    text = { Text(text = "By continuing, this task will be permanently deleted.") },
                    confirmButton = {
                        ElevatedButton(
                            elevation = ButtonDefaults.elevatedButtonElevation(4.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            onClick = {
                                showingDeletionDialog = false
                                Log.d("TaskCardDropdownMenu", "Task deleted")
                                onExpandedChange(false)
                                deletingTask = true
                            }
                        ) { Text(text = "Delete") }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showingDeletionDialog = false }
                        ) { Text(text = "Cancel") }
                    }
                )
            }
        }
    }
}

fun List<Task>.prepare(
    sortingOrder: Boolean,
    sortingOption: String,
    deadlineFilter: String,
    statusFilter: String,
    priorityFilter: String,
    tagQuery: String,
    memberQuery: String,
    showRecursive: Boolean,
    query: String
): List<Task> {
    var tasks = this.asSequence()

    if (sortingOrder)
        tasks = when (sortingOption) {
            TasksSortingOption.ProjectName.title -> tasks.sortedByDescending { it.projectName.decapitalize() }
            TasksSortingOption.TaskName.title -> tasks.sortedByDescending { it.taskName.decapitalize() }
            TasksSortingOption.Members.title -> tasks.sortedByDescending { it.listUser.size }
            TasksSortingOption.Status.title -> tasks.sortedByDescending { it.status }
            TasksSortingOption.Priority.title -> tasks.sortedByDescending { it.priority }
            else -> tasks.sortedByDescending { it.endDate }

        }
    else
        tasks = when (sortingOption) {
            TasksSortingOption.ProjectName.title -> tasks.sortedBy { it.projectName.decapitalize() }
            TasksSortingOption.TaskName.title -> tasks.sortedBy { it.taskName.decapitalize() }
            TasksSortingOption.Members.title -> tasks.sortedBy { it.listUser.size }
            TasksSortingOption.Status.title -> tasks.sortedBy { it.status }
            TasksSortingOption.Priority.title -> tasks.sortedBy { it.priority }
            else -> tasks.sortedBy { it.endDate }
        }

    tasks = when (deadlineFilter) {
        TasksDeadlineFilteringOptions.NotStarted.title -> tasks.filter { it.creationDate > Timestamp.now() }
        TasksDeadlineFilteringOptions.Overdue.title -> tasks.filter { it.creationDate < Timestamp.now() && it.endDate < Timestamp.now() }
        TasksDeadlineFilteringOptions.InTime.title -> tasks.filter { it.creationDate < Timestamp.now() && it.endDate > Timestamp.now() }
        else -> { tasks }
    }

    tasks = when (statusFilter) {
        TasksStatusFilteringOptions.Completed.title -> tasks.filter { it.status == TaskStatus.Completed }
        TasksStatusFilteringOptions.InProgress.title -> tasks.filter { it.status == TaskStatus.Progress }
        TasksStatusFilteringOptions.Pending.title -> tasks.filter { it.status == TaskStatus.Pending }
        TasksStatusFilteringOptions.OnHold.title -> tasks.filter { it.status == TaskStatus.Hold }
        else -> {
            tasks
        }
    }

    tasks = when (priorityFilter) {
        TasksPriorityFilteringOptions.Low.title -> tasks.filter { it.priority == TaskPriority.Low }
        TasksPriorityFilteringOptions.Medium.title -> tasks.filter { it.priority == TaskPriority.Medium }
        TasksPriorityFilteringOptions.High.title -> tasks.filter { it.priority == TaskPriority.High }
        else -> {
            tasks
        }
    }

    if(query.isNotBlank()) tasks = tasks.filter { Regex(query, RegexOption.IGNORE_CASE).containsMatchIn(it.taskName) }
    if(tagQuery.isNotBlank()) tasks = tasks.filter { Regex(tagQuery, RegexOption.IGNORE_CASE).containsMatchIn(it.tag) }
    if(memberQuery.isNotBlank()) tasks = tasks.filter {
        it.listUser.any {
            val user = usersViewModel!!.users.value[it]!!
            Regex(memberQuery.replace(" ", ""), RegexOption.IGNORE_CASE).containsMatchIn("${user.name}${user.surname}") ||
                    Regex(memberQuery.replace(" ", ""), RegexOption.IGNORE_CASE).containsMatchIn("${user.surname}${user.name}")
        }
    }

    return tasks.toList()
}
