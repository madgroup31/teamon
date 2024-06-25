@file:Suppress("UNCHECKED_CAST")

package com.teamon.app.tasks

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamon.app.Actions
import com.teamon.app.NavigationItem
import com.teamon.app.R
import com.teamon.app.profileViewModel
import com.teamon.app.projectsViewModel
import com.teamon.app.tasksViewModel
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.graphics.AnimatedGrid
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.LoadingOverlay
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.SearchBar
import com.teamon.app.utils.graphics.TasksDeadlineFilteringOptions
import com.teamon.app.utils.graphics.TasksFilteringOptionsDropdownMenu
import com.teamon.app.utils.graphics.TasksPriorityFilteringOptions
import com.teamon.app.utils.graphics.TasksSortingOption
import com.teamon.app.utils.graphics.TasksSortingOptionsDropdownMenu
import com.teamon.app.utils.graphics.TasksStatusFilteringOptions
import com.teamon.app.utils.graphics.TasksViewDropdownMenu
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.prepare
import com.teamon.app.utils.viewmodels.Factory
import com.teamon.app.utils.viewmodels.NewTaskViewModel

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun TasksView(
    actions: Actions,
    taskId: String? = null
) {
    if (taskId != null) actions.openTask(NavigationItem.MyTasks.title, taskId)
    Theme(color = profileViewModel.color, applyToStatusBar = true) {

        var sortingOrder by rememberSaveable {
            mutableStateOf(false)
        }
        var sortingOption by rememberSaveable {
            mutableStateOf(TasksSortingOption.Deadline.title)
        }
        var deadlineFilter by remember {
            mutableStateOf(TasksDeadlineFilteringOptions.All.title)
        }
        var statusFilter by rememberSaveable {
            mutableStateOf(TasksStatusFilteringOptions.All.title)
        }
        var tagQuery by rememberSaveable {
            mutableStateOf("")
        }
        var memberQuery by rememberSaveable {
            mutableStateOf("")
        }
        var priorityFilter by rememberSaveable {
            mutableStateOf(TasksPriorityFilteringOptions.All.title)
        }
        var searchActive by rememberSaveable {
            mutableStateOf(false)
        }
        var query by rememberSaveable {
            mutableStateOf("")
        }

        val onSortingOrderChange: (Boolean) -> Unit = { sortingOrder = !it }

        val onSortingOptionChange: (TasksSortingOption) -> Unit =
            { sortingOption = it.title }
        val onDeadlineFilterChange: (TasksDeadlineFilteringOptions) -> Unit =
            { deadlineFilter = it.title }
        val onStatusFilterChange: (TasksStatusFilteringOptions) -> Unit =
            { statusFilter = it.title }
        val onPriorityFilterChange: (TasksPriorityFilteringOptions) -> Unit =
            { priorityFilter = it.title }
        val onSearchActiveChange: (Boolean) -> Unit =
            { searchActive = it }
        val onQueryChange: (String) -> Unit =
            { query = it }
        val onTagQueryChange: (String) -> Unit =
            { tagQuery = it }
        val onMemberQueryChange: (String) -> Unit =
            { memberQuery = it }

        val myID = profileViewModel.userId
        val projects by projectsViewModel.getProjects().collectAsState(initial = emptyMap())
        val userTasks by tasksViewModel.getUserTasks().collectAsState(initial = null)
        val newTaskViewModel = if (projects.isNotEmpty())
            viewModel<NewTaskViewModel>(
                factory = Factory(
                    LocalContext.current.applicationContext,
                    projectId = "",
                    userId = myID
                )
            )
        else null

        var landscape by remember { mutableStateOf(false) }
        landscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (landscape) LandscapeView(
            query = query,
            onQueryChange = onQueryChange,
            searchActive = searchActive,
            onSearchActiveChange = onSearchActiveChange,
            sortingOrder = sortingOrder,
            onSortingOrderChange = onSortingOrderChange,
            sortingOption = sortingOption,
            onSortingOptionChange = onSortingOptionChange,
            deadlineFilter = deadlineFilter,
            onDeadlineFilterChange = onDeadlineFilterChange,
            statusFilter = statusFilter,
            onStatusFilterChange = onStatusFilterChange,
            priorityFilter = priorityFilter,
            onPriorityFilterChange = onPriorityFilterChange,
            tagQuery = tagQuery,
            onTagQueryChange = onTagQueryChange,
            memberQuery = memberQuery,
            onMemberQueryChange = onMemberQueryChange,
            actions = actions,
            data = userTasks?.values?.toList(),
            newTaskViewModel = newTaskViewModel
        )
        else PortraitView(
            query = query,
            onQueryChange = onQueryChange,
            searchActive = searchActive,
            onSearchActiveChange = onSearchActiveChange,
            sortingOrder = sortingOrder,
            onSortingOrderChange = onSortingOrderChange,
            sortingOption = sortingOption,
            onSortingOptionChange = onSortingOptionChange,
            deadlineFilter = deadlineFilter,
            onDeadlineFilterChange = onDeadlineFilterChange,
            statusFilter = statusFilter,
            onStatusFilterChange = onStatusFilterChange,
            priorityFilter = priorityFilter,
            onPriorityFilterChange = onPriorityFilterChange,
            tagQuery = tagQuery,
            onTagQueryChange = onTagQueryChange,
            memberQuery = memberQuery,
            onMemberQueryChange = onMemberQueryChange,
            actions = actions,
            data = userTasks?.values?.toList(),
            newTaskViewModel = newTaskViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun LandscapeView(
    query: String,
    onQueryChange: (String) -> Unit,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
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
    actions: Actions,
    data: List<Task>?,
    newTaskViewModel: NewTaskViewModel?
) {
    val snackbarHostState = remember { SnackbarHostState() }
    AppSurface(
        orientation = Orientation.LANDSCAPE,
        actions = actions,
        snackbarHostState = snackbarHostState,
        title = "My Tasks",
        trailingTopBarActions = {
            if(!data.isNullOrEmpty()) {
                var mainExpanded by remember {
                    mutableStateOf(false)
                }
                var filterExpanded by remember {
                    mutableStateOf(false)
                }
                var sortExpanded by remember {
                    mutableStateOf(false)
                }
                IconButton(onClick = { onSearchActiveChange(true) }) {
                    Icon(Icons.Rounded.Search, contentDescription = "Search tasks")
                }
                IconButton(onClick = { mainExpanded = !mainExpanded }) {
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
                    onMainExpandedChange = { mainExpanded = it },
                    onSortExpandedChange = { sortExpanded = it },
                    onFilterExpandedChange = { filterExpanded = it }
                )

                TasksSortingOptionsDropdownMenu(
                    sortExpanded = sortExpanded,
                    sortingOrder = sortingOrder,
                    onSortingOrderChange = onSortingOrderChange,
                    onSortExpandedChange = { sortExpanded = it },
                    onMainExpandedChange = { mainExpanded = it },
                    sortingOption = sortingOption,
                    onSortingOptionChange = onSortingOptionChange
                )

                TasksFilteringOptionsDropdownMenu(
                    filterExpanded = filterExpanded,
                    onFilterExpandedChange = { filterExpanded = it },
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
        },
        floatingActionButton =
        {
            if (newTaskViewModel != null) {
                FloatingActionButton(
                    onClick = { newTaskViewModel.toggleShow() },
                    content = {
                        Box(modifier = Modifier.size(24.dp)) {
                            Box(modifier = Modifier.align(Alignment.Center)) {
                                Image(
                                    painter = painterResource(R.drawable.round_calendar_today_24),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                    contentDescription = null
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(top = 4.dp)
                            ) {
                                Icon(
                                    modifier = Modifier.size(15.dp),
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null
                                )
                            }
                        }

                    },
                    modifier = Modifier
                )
            }
        }
    ) {
        if(data == null)
            LoadingOverlay(isLoading = true)
        else {
            AnimatedContent(targetState = searchActive, label = "") { isSearching ->
                if (isSearching) {
                    SearchBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(20f)
                            .padding(10.dp),
                        query = query,
                        placeholder = "Search Tasks...",
                        onQueryChange = onQueryChange,
                        searchActive = true,
                        onSearchActiveChange = onSearchActiveChange
                    ) {
                        val tasks = data
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
                                items = tasks.groupBy {
                                    it.recurringSet ?: it.taskId
                                }.values.toList()
                            ) { it, _ ->
                                val t = (it as List<Task>).sortedBy { it.endDate }
                                if (t.size > 1)
                                    RecursiveTasksBox(
                                        tasks = t,
                                        actions = actions,
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
                } else {

                    val tasks = data
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
                        ) { task, _ ->
                            val t = (task as List<Task>).sortedBy { it.endDate }
                            if (t.size > 1)
                                RecursiveTasksBox(
                                    tasks = t,
                                    actions = actions,
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
        }



        val sheetState = rememberModalBottomSheetState()
        if (newTaskViewModel != null && newTaskViewModel.isShowing) {
            ModalBottomSheet(
                modifier = Modifier,
                onDismissRequest = {
                    newTaskViewModel.toggleShow()
                },
                sheetState = sheetState
            ) {
                // Sheet content
                NewTaskBottomSheetContent(
                    newTaskVM = newTaskViewModel,
                    projectVM = null,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun PortraitView(
    query: String,
    onQueryChange: (String) -> Unit,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
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
    actions: Actions,
    data: List<Task>?,
    newTaskViewModel: NewTaskViewModel?
) {
    val snackbarHostState = remember { SnackbarHostState() }
    AppSurface(
        orientation = Orientation.PORTRAIT,
        actions = actions,
        snackbarHostState = snackbarHostState,
        title = "My Tasks",
        trailingTopBarActions = {
            if (!data.isNullOrEmpty()) {
                var mainExpanded by remember {
                    mutableStateOf(false)
                }
                var filterExpanded by remember {
                    mutableStateOf(false)
                }
                var sortExpanded by remember {
                    mutableStateOf(false)
                }

                IconButton(onClick = { onSearchActiveChange(true) }) {
                    Icon(Icons.Rounded.Search, contentDescription = "Search tasks")
                }
                    IconButton(onClick = { mainExpanded = !mainExpanded }) {
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
                    onMainExpandedChange = { mainExpanded = it },
                    onSortExpandedChange = { sortExpanded = it },
                    onFilterExpandedChange = { filterExpanded = it }
                )

                TasksSortingOptionsDropdownMenu(
                    sortExpanded = sortExpanded,
                    sortingOrder = sortingOrder,
                    onSortingOrderChange = onSortingOrderChange,
                    onSortExpandedChange = { sortExpanded = it },
                    onMainExpandedChange = { mainExpanded = it },
                    sortingOption = sortingOption,
                    onSortingOptionChange = onSortingOptionChange
                )

                TasksFilteringOptionsDropdownMenu(
                    filterExpanded = filterExpanded,
                    onFilterExpandedChange = { filterExpanded = it },
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
        },
        floatingActionButton =
        {
            if (newTaskViewModel != null) {
                FloatingActionButton(
                    onClick = { newTaskViewModel.toggleShow() },
                    content = {
                        Box(modifier = Modifier.size(24.dp)) {
                            Box(modifier = Modifier.align(Alignment.Center)) {
                                Image(
                                    painter = painterResource(R.drawable.round_calendar_today_24),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                    contentDescription = null
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(top = 4.dp)
                            ) {
                                Icon(
                                    modifier = Modifier.size(15.dp),
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null
                                )
                            }
                        }

                    },
                    modifier = Modifier
                )
            }
        }
    ) {
        if (data == null)
            LoadingOverlay(isLoading = true)
        else {

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
                        val tasks = data
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
                                items = tasks.groupBy {
                                    it.recurringSet ?: it.taskId
                                }.values.toList()
                            ) { it, _ ->
                                val t = (it as List<Task>).sortedBy { it.endDate }
                                if (t.size > 1)
                                    RecursiveTasksBox(
                                        tasks = t,
                                        actions = actions,
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

                    val tasks = data
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
                        ) { task, _ ->
                            val t = (task as List<Task>).sortedBy { it.endDate }
                            if (t.size > 1)
                                RecursiveTasksBox(
                                    tasks = t,
                                    actions = actions,
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
        }

        val sheetState = rememberModalBottomSheetState()
        if (newTaskViewModel != null && newTaskViewModel.isShowing) {
            ModalBottomSheet(
                modifier = Modifier,
                onDismissRequest = {
                    newTaskViewModel.toggleShow()
                },
                sheetState = sheetState
            ) {
                // Sheet content
                NewTaskBottomSheetContent(
                    newTaskVM = newTaskViewModel,
                    projectVM = null,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}
