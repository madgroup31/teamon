@file:Suppress("UNCHECKED_CAST", "KotlinConstantConditions")

package com.teamon.app.board.project.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.teamon.app.Actions
import com.teamon.app.tasks.NewTaskBottomSheetContent
import com.teamon.app.tasks.RecursiveTasksBox
import com.teamon.app.tasks.TaskCard
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.graphics.AnimatedGrid
import com.teamon.app.utils.graphics.SearchBar
import com.teamon.app.utils.graphics.TasksDeadlineFilteringOptions
import com.teamon.app.utils.graphics.TasksFilteringOptionsDropdownMenu
import com.teamon.app.utils.graphics.TasksPriorityFilteringOptions
import com.teamon.app.utils.graphics.TasksSortingOption
import com.teamon.app.utils.graphics.TasksSortingOptionsDropdownMenu
import com.teamon.app.utils.graphics.TasksStatusFilteringOptions
import com.teamon.app.utils.graphics.TasksViewDropdownMenu
import com.teamon.app.utils.graphics.prepare
import com.teamon.app.utils.viewmodels.NewTaskViewModel
import com.teamon.app.utils.viewmodels.ProjectViewModel

@Composable
fun TasksActions(
    mainExpanded: Boolean,
    onMainExpandedChange: (Boolean) -> Unit,
    sortExpanded: Boolean,
    onSortExpandedChange: (Boolean) -> Unit,
    filterExpanded: Boolean,
    onFilterExpandedChange: (Boolean) -> Unit,
    sortingOrder: Boolean,
    onSortingOrderChange: (Boolean) -> Unit,
    sortingOption: String,
    onSortingOptionChange: (TasksSortingOption) -> Unit,
    deadlineFilter: String,
    onDeadlineFilterChange: (TasksDeadlineFilteringOptions) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (TasksStatusFilteringOptions) -> Unit,
    priorityFilter: String,
    onPriorityFilterChange: (TasksPriorityFilteringOptions) -> Unit,
    tagQuery: String,
    onTagQueryChange: (String) -> Unit,
    memberQuery: String,
    onMemberQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit
) {
    IconButton(onClick = { onSearchActiveChange(true) }) {
        Icon(Icons.Rounded.Search, contentDescription = "Search")
    }
    IconButton(onClick = { onMainExpandedChange(true) }) {
        BadgedBox(
            modifier = Modifier.wrapContentSize(),
            badge = {
                if(deadlineFilter != TasksDeadlineFilteringOptions.All.title ||
                    statusFilter != TasksStatusFilteringOptions.All.title ||
                    tagQuery.isNotBlank() || memberQuery.isNotBlank() || priorityFilter != TasksPriorityFilteringOptions.All.title)
                    Badge(modifier = Modifier.offset(x = (-5).dp))
            }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "More tasks options")
        }
    }

    TasksViewDropdownMenu(
        filterBadge = deadlineFilter != TasksDeadlineFilteringOptions.All.title ||
                statusFilter != TasksStatusFilteringOptions.All.title ||
                tagQuery.isNotBlank() || memberQuery.isNotBlank() || priorityFilter != TasksPriorityFilteringOptions.All.title,
        mainExpanded = mainExpanded,
        onMainExpandedChange = onMainExpandedChange,
        onSortExpandedChange = onSortExpandedChange,
        onFilterExpandedChange = onFilterExpandedChange
    )

    TasksSortingOptionsDropdownMenu(
        sortExpanded = sortExpanded,
        sortingOrder = sortingOrder,
        onSortingOrderChange = onSortingOrderChange,
        onSortExpandedChange = onSortExpandedChange,
        onMainExpandedChange = onMainExpandedChange,
        sortingOption = sortingOption,
        onSortingOptionChange = onSortingOptionChange
    )

    TasksFilteringOptionsDropdownMenu(
        filterExpanded = filterExpanded,
        onFilterExpandedChange = onFilterExpandedChange,
        deadlineFilter = deadlineFilter,
        onDeadlineFilterChange = onDeadlineFilterChange,
        statusFilter = statusFilter,
        onStatusFilterChange = onStatusFilterChange,
        priorityFilter = priorityFilter,
        onPriorityFilterChange = onPriorityFilterChange,
        tagQuery = tagQuery,
        onTagQueryChange = onTagQueryChange,
        memberQuery = memberQuery,
        onMemberQueryChange = onMemberQueryChange
    )
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandscapeTasksView(
    actions: Actions,
    query: String,
    onQueryChange: (String) -> Unit,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    newTaskVM: NewTaskViewModel,
    projectVM: ProjectViewModel,
    snackbarHostState: SnackbarHostState,
    sortingOrder: Boolean,
    sortingOption: String,
    deadlineFilter: String,
    statusFilter: String,
    priorityFilter: String,
    tagQuery: String,
    memberQuery: String
) {
    val sheetState = rememberModalBottomSheetState()
    AnimatedContent(targetState = searchActive, label = "") { isSearching ->
        if (isSearching)
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(20f)
                    .padding(10.dp),
                query = query,
                placeholder = "Search Tasks...",
                onQueryChange = onQueryChange,
                searchActive = searchActive,
                onSearchActiveChange = onSearchActiveChange
            ) {
                val tasks = projectVM.tasks
                    .prepare(
                        sortingOrder,
                        sortingOption,
                        deadlineFilter,
                        statusFilter,
                        priorityFilter,
                        tagQuery,
                        memberQuery,
                        query
                    )
                if (tasks.isEmpty())

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Available Tasks.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic
                        )
                    }
                else


                    AnimatedGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = StaggeredGridCells.FixedSize(400.dp),
                        items = tasks.groupBy { it.recurringSet ?: it.taskId }.values.toList()
                    ) { it, _ ->
                        val t = (it as List<Task>).sortedBy { it.endDate }
                        if (t.size > 1)
                            RecursiveTasksBox(
                                tasks = t, actions = actions,
                                snackbarHostState = snackbarHostState
                            )
                        else
                            TaskCard(
                                taskId = (t.first()).taskId,
                                actions = actions,
                                snackbarHostState = snackbarHostState
                            )

                    }
            }
        else {

            val tasks = projectVM.tasks
                .prepare(
                    sortingOrder,
                    sortingOption,
                    deadlineFilter,
                    statusFilter,
                    priorityFilter,
                    tagQuery,
                    memberQuery,
                    ""
                )
            if (tasks.isEmpty())
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Tasks available.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic
                    )
                }
            else
                AnimatedGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = StaggeredGridCells.FixedSize(400.dp),
                    items = tasks.groupBy { it.recurringSet ?: it.taskId }.values.toList()
                ) { it, _ ->
                    val t = (it as List<Task>).sortedBy { it.endDate }
                    if (t.size > 1)
                        RecursiveTasksBox(
                            tasks = t, actions = actions,
                            snackbarHostState = snackbarHostState
                        )
                    else
                        TaskCard(
                            taskId = (t.first()).taskId,
                            actions = actions,
                            snackbarHostState = snackbarHostState
                        )

                }
        }
    }
    if (newTaskVM.isShowing) {
        ModalBottomSheet(
            modifier = Modifier,
            onDismissRequest = {
                newTaskVM.toggleShow()
            },
            sheetState = sheetState
        ) {
            // Sheet content
            NewTaskBottomSheetContent(
                newTaskVM = newTaskVM,
                projectVM = projectVM,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortraitTasksView(
    actions: Actions,
    query: String,
    snackbarHostState: SnackbarHostState,
    onQueryChange: (String) -> Unit,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    newTaskVM: NewTaskViewModel,
    projectVM: ProjectViewModel,
    sortingOrder: Boolean,
    sortingOption: String,
    deadlineFilter: String,
    statusFilter: String,
    priorityFilter: String,
    tagQuery: String,
    memberQuery: String
) {
    val sheetState = rememberModalBottomSheetState()

    AnimatedContent(targetState = searchActive, label = "") { isSearching ->
        if (isSearching)
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                query = query,
                placeholder = "Search Tasks...",
                onQueryChange = onQueryChange,
                searchActive = true,
                onSearchActiveChange = onSearchActiveChange
            ) {
                val tasks = projectVM.tasks
                    .prepare(
                        sortingOrder,
                        sortingOption,
                        deadlineFilter,
                        statusFilter,
                        priorityFilter,
                        tagQuery,
                        memberQuery,
                        query
                    )
                if (tasks.isEmpty())
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Available Tasks.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic
                        )
                    }
                else

                    AnimatedGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = StaggeredGridCells.FixedSize(400.dp),
                        items = tasks.groupBy { it.recurringSet ?: it.taskId }.values.toList()
                    ) { it, _ ->
                        val t = (it as List<Task>).sortedBy { it.endDate }
                        if (t.size > 1)
                            RecursiveTasksBox(
                                tasks = t, actions = actions,
                                snackbarHostState = snackbarHostState
                            )
                        else
                            TaskCard(
                                taskId = (t.first()).taskId,
                                actions = actions,
                                snackbarHostState = snackbarHostState
                            )

                    }
            }
        else {
            val tasks = projectVM.tasks
                .prepare(
                    sortingOrder,
                    sortingOption,
                    deadlineFilter,
                    statusFilter,
                    priorityFilter,
                    tagQuery,
                    memberQuery,
                    ""
                )
            if (tasks.isEmpty())

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Available Tasks.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic
                    )
                }
            else
                AnimatedGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = StaggeredGridCells.FixedSize(400.dp),
                    items = tasks.groupBy { it.recurringSet ?: it.taskId }.values.toList()
                ) { it, _ ->
                    val t = (it as List<Task>).sortedBy { it.endDate }
                    if (t.size > 1)
                        RecursiveTasksBox(
                            tasks = t, actions = actions,
                            snackbarHostState = snackbarHostState
                        )
                    else
                        TaskCard(
                            taskId = (t.first()).taskId,
                            actions = actions,
                            snackbarHostState = snackbarHostState
                        )

                }
        }
    }
    if (newTaskVM.isShowing) {
        ModalBottomSheet(
            modifier = Modifier,
            onDismissRequest = {
                newTaskVM.toggleShow()
            },
            sheetState = sheetState
        ) {
            // Sheet content
            NewTaskBottomSheetContent(
                newTaskVM = newTaskVM,
                projectVM = projectVM,
                snackbarHostState = snackbarHostState
            )
        }
    }
}