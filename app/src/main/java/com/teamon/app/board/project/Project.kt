package com.teamon.app.board.project

import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamon.app.Actions
import com.teamon.app.utils.viewmodels.Factory
import com.teamon.app.R
import com.teamon.app.board.project.feedbacks.FeedbacksView
import com.teamon.app.board.project.info.LandscapeProjectInfoView
import com.teamon.app.board.project.info.PortraitProjectInfoView
import com.teamon.app.board.project.teams.LandscapeTeamsView
import com.teamon.app.board.project.teams.TeamsActions
import com.teamon.app.board.project.teams.PortraitTeamsView
import com.teamon.app.board.project.performance.PerformanceActions
import com.teamon.app.board.project.performance.PerformanceView
import com.teamon.app.board.project.tasks.LandscapeTasksView
import com.teamon.app.utils.viewmodels.NewTaskViewModel
import com.teamon.app.board.project.tasks.PortraitTasksView
import com.teamon.app.board.project.tasks.TasksActions
import com.teamon.app.profileViewModel
import com.teamon.app.tasksViewModel
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.TasksDeadlineFilteringOptions
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.TasksPriorityFilteringOptions
import com.teamon.app.utils.graphics.ScrollableTab
import com.teamon.app.utils.graphics.TasksStatusFilteringOptions
import com.teamon.app.utils.graphics.TabItem
import com.teamon.app.utils.graphics.TasksSortingOption
import com.teamon.app.utils.graphics.TeamsSortingOption
import com.teamon.app.utils.viewmodels.ProjectViewModel
import kotlinx.coroutines.CoroutineScope


@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectView(actions: Actions, projectVM: ProjectViewModel, startingTab: String = TabItem.ProjectTasks.title) {


    Theme(color = projectVM.projectColor, applyToStatusBar = true) {

        val items = listOf(
            TabItem.ProjectInfo,
            TabItem.ProjectTasks,
            TabItem.ProjectTeams,
            TabItem.ProjectFeedbacks,
            TabItem.ProjectPerformance
        )

        var landscape by remember { mutableStateOf(false) }
        landscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        var sortingOrder by rememberSaveable {
            mutableStateOf(false)
        }
        var sortingOption by rememberSaveable {
            mutableStateOf(TasksSortingOption.Deadline.title)
        }
        var deadlineFilter by rememberSaveable {
            mutableStateOf(TasksDeadlineFilteringOptions.All.title)
        }
        var statusFilter by rememberSaveable {
            mutableStateOf(TasksStatusFilteringOptions.All.title)
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
        var tagQuery by rememberSaveable {
            mutableStateOf("")
        }
        var memberQuery by rememberSaveable {
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

        var teamSortingOrder by rememberSaveable {
            mutableStateOf(false)
        }
        var teamSortingOption by rememberSaveable {
            mutableStateOf(TeamsSortingOption.CreationDate.title)
        }
        var teamMemberQuery by rememberSaveable {
            mutableStateOf("") }
        var teamCategoryQuery by rememberSaveable {
            mutableStateOf("") }
        var teamAdminQuery by rememberSaveable {
            mutableStateOf("")
        }
        var showRecursive by rememberSaveable {
            mutableStateOf(false)
        }

        val onShowRecursiveChange: (Boolean) -> Unit = { showRecursive = !showRecursive }
        val onTeamSortingOrderChange: (Boolean) -> Unit = { teamSortingOrder = !it }
        val onTeamSortingOptionChange: (TeamsSortingOption) -> Unit = { teamSortingOption = it.title}
        val onTeamMemberQueryChange: (String) -> Unit = { teamMemberQuery = it }
        val onTeamCategoryQueryChange: (String) -> Unit = { teamCategoryQuery = it }
        val onTeamAdminQueryChange: (String) -> Unit = { teamAdminQuery = it }

        var teamQuery by rememberSaveable {
            mutableStateOf("")
        }
        val onTeamQueryChange: (String) -> Unit = { teamQuery = it }
        val onTaskDelete: (String)->Unit = { taskId ->
            //Log.d("TaskView", "Task deleted: $taskId")
            //tasksViewModel!!.deleteTask(taskId)
        }
        var teamSearchActive by rememberSaveable {
            mutableStateOf(false)
        }
        val onTeamSearchActiveChange: (Boolean) -> Unit = {teamSearchActive = it}

        val newTaskViewModel = viewModel<NewTaskViewModel>(factory = Factory(LocalContext.current.applicationContext, projectId = projectVM.projectId, userId = profileViewModel!!.userId))

        val pagerState = rememberPagerState(pageCount = { items.size }, initialPage = items.indexOfFirst { it.title == startingTab })


            if (landscape) LandscapeView(
                taskQuery = query,
                onTaskQueryChange = onQueryChange,
                taskSearchActive = searchActive,
                onTaskSearchActiveChange = onSearchActiveChange,
                taskSortingOrder = sortingOrder,
                onTaskSortingOrderChange = onSortingOrderChange,
                taskSortingOption = sortingOption,
                onTaskSortingOptionChange = onSortingOptionChange,
                taskDeadlineFilter = deadlineFilter,
                onTaskDeadlineFilterChange = onDeadlineFilterChange,
                taskStatusFilter = statusFilter,
                onTaskStatusFilterChange = onStatusFilterChange,
                taskPriorityFilter = priorityFilter,
                onTaskPriorityFilterChange = onPriorityFilterChange,
                taskTagQuery = tagQuery,
                onTaskTagQueryChange = onTagQueryChange,
                taskMemberQuery = memberQuery,
                onTaskMemberQueryChange = onMemberQueryChange,
                actions = actions,
                newTaskViewModel = newTaskViewModel,
                projectVM = projectVM,
                pagerState = pagerState,
                onTaskDelete = onTaskDelete,
                teamSortingOrder = teamSortingOrder,
                onTeamSortingOrderChange = onTeamSortingOrderChange,
                teamSortingOption = teamSortingOption,
                onTeamSortingOptionChange = onTeamSortingOptionChange,
                teamMemberQuery = teamMemberQuery,
                onTeamMemberQueryChange = onTeamMemberQueryChange,
                teamCategoryQuery = teamCategoryQuery,
                onTeamCategoryQueryChange = onTeamCategoryQueryChange,
                teamAdminQuery=  teamAdminQuery,
                onTeamAdminQueryChange= onTeamAdminQueryChange,
                teamQuery = teamQuery,
                onTeamQueryChange = onTeamQueryChange,
                teamSearchActive = teamSearchActive,
                onTeamSearchActiveChange = onTeamSearchActiveChange,
                showRecursive = showRecursive,
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
                pagerState = pagerState,
                newTaskViewModel = newTaskViewModel,
                projectVM = projectVM,
                onTaskDelete = onTaskDelete,
                teamSortingOrder = teamSortingOrder,
                onTeamSortingOrderChange = onTeamSortingOrderChange,
                teamSortingOption = teamSortingOption,
                onTeamSortingOptionChange = onTeamSortingOptionChange,
                teamMemberQuery = teamMemberQuery,
                onTeamMemberQueryChange = onTeamMemberQueryChange,
                teamCategoryQuery = teamCategoryQuery,
                onTeamCategoryQueryChange = onTeamCategoryQueryChange,
                teamAdminQuery=  teamAdminQuery,
                onTeamAdminQueryChange= onTeamAdminQueryChange,
                teamQuery = teamQuery,
                onTeamQueryChange = onTeamQueryChange,
                teamSearchActive = teamSearchActive,
                onTeamSearchActiveChange = onTeamSearchActiveChange,
            )

    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LandscapeView(
    taskQuery: String,
    onTaskQueryChange: (String) -> Unit,
    taskSearchActive: Boolean,
    onTaskSearchActiveChange: (Boolean) -> Unit,
    taskSortingOrder: Boolean,
    onTaskSortingOrderChange: (Boolean) -> Unit,
    taskSortingOption: String,
    onTaskSortingOptionChange: (TasksSortingOption) -> Unit,
    taskDeadlineFilter: String,
    onTaskDeadlineFilterChange: (TasksDeadlineFilteringOptions) -> Unit,
    taskStatusFilter: String,
    onTaskStatusFilterChange: (TasksStatusFilteringOptions) -> Unit,
    taskPriorityFilter: String,
    onTaskPriorityFilterChange: (TasksPriorityFilteringOptions) -> Unit,
    taskTagQuery: String,
    onTaskTagQueryChange: (String) -> Unit,
    taskMemberQuery: String,
    onTaskMemberQueryChange: (String) -> Unit,
    actions: Actions,
    pagerState: PagerState,
    newTaskViewModel: NewTaskViewModel,
    projectVM: ProjectViewModel,
    onTaskDelete: (String)->Unit,
    teamSortingOrder: Boolean,
    onTeamSortingOrderChange: (Boolean) -> Unit,
    teamSortingOption: String,
    onTeamSortingOptionChange: (TeamsSortingOption) -> Unit,
    teamMemberQuery: String,
    onTeamMemberQueryChange: (String) -> Unit,
    teamCategoryQuery: String,
    onTeamCategoryQueryChange: (String) -> Unit,
    teamAdminQuery: String,
    onTeamAdminQueryChange: (String) -> Unit,
    teamQuery: String,
    onTeamQueryChange: (String) -> Unit,
    teamSearchActive: Boolean,
    onTeamSearchActiveChange: (Boolean) -> Unit,
    showRecursive: Boolean,
) {

    val snackbarHostState = remember { SnackbarHostState() }
    AppSurface(
        orientation = Orientation.LANDSCAPE,
        actions = actions,
        snackbarHostState = snackbarHostState,
        title = projectVM.projectName,
        tabActions = {
            val items = listOf(
                TabItem.ProjectInfo,
                TabItem.ProjectTasks,
                TabItem.ProjectTeams,
                TabItem.ProjectFeedbacks,
                TabItem.ProjectPerformance
            )
            ScrollableTab(
                orientation = Orientation.LANDSCAPE,
                items = items,
                pagerState = pagerState,
                selected = pagerState.currentPage,
            )
        },
        leadingTopBarActions = {
            IconButton(onClick = { actions.navCont.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
            }
        },
        trailingTopBarActions = {
            when (pagerState.currentPage) {
                0 -> {}
                1 -> {
                    var mainExpanded by remember {
                        mutableStateOf(false)
                    }
                    var filterExpanded by remember {
                        mutableStateOf(false)
                    }
                    var sortExpanded by remember {
                        mutableStateOf(false)
                    }

                    TasksActions(
                        mainExpanded = mainExpanded,
                        onMainExpandedChange = { mainExpanded = it },
                        sortExpanded = sortExpanded,
                        onSortExpandedChange = { sortExpanded = it },
                        filterExpanded = filterExpanded,
                        onFilterExpandedChange = { filterExpanded = it },
                        sortingOrder = taskSortingOrder,
                        onSortingOrderChange = onTaskSortingOrderChange,
                        sortingOption = taskSortingOption,
                        onSortingOptionChange = onTaskSortingOptionChange,
                        deadlineFilter = taskDeadlineFilter,
                        onDeadlineFilterChange = onTaskDeadlineFilterChange,
                        statusFilter = taskStatusFilter,
                        onStatusFilterChange = onTaskStatusFilterChange,
                        priorityFilter = taskPriorityFilter,
                        onPriorityFilterChange = onTaskPriorityFilterChange,
                        tagQuery = taskTagQuery,
                        onTagQueryChange = onTaskTagQueryChange,
                        memberQuery = taskMemberQuery,
                        onMemberQueryChange = onTaskMemberQueryChange,
                        onSearchActiveChange = onTaskSearchActiveChange,
                    )
                }

                2 -> {
                    var mainExpanded by remember {
                        mutableStateOf(false)
                    }
                    var filterExpanded by remember {
                        mutableStateOf(false)
                    }
                    var sortExpanded by remember {
                        mutableStateOf(false)
                    }

                    TeamsActions(
                        mainExpanded = mainExpanded,
                        onMainExpandedChange = { mainExpanded = it },
                        sortExpanded = sortExpanded,
                        onSortExpandedChange = { sortExpanded = it },
                        filterExpanded = filterExpanded,
                        onFilterExpandedChange = { filterExpanded = it },
                        sortingOrder = teamSortingOrder,
                        onSortingOrderChange = onTeamSortingOrderChange,
                    sortingOption = teamSortingOption,
                    onSortingOptionChange = onTeamSortingOptionChange,
                    memberQuery = teamMemberQuery,
                    onMemberQueryChange = onTeamMemberQueryChange,
                    categoryQuery = teamCategoryQuery,
                    onCategoryQueryChange = onTeamCategoryQueryChange,
                    adminQuery=  teamAdminQuery,
                    onAdminQueryChange= onTeamAdminQueryChange,
                    onSearchActiveChange = onTeamSearchActiveChange,
                    )
                }

                3 -> {

                }

                4 -> {
                    PerformanceActions()
                }
            }
        },
        floatingActionButton = {
            when (pagerState.currentPage) {

                0 -> {
                    if(projectVM.teams.filter { it.users.contains(profileViewModel.userId) }
                            .any { it.admin.contains(profileViewModel.userId) })
                    FloatingActionButton(
                        onClick = { projectVM.toggleEdit() },
                        modifier = Modifier
                            .padding(end = 10.dp),
                        content = {
                            if (!projectVM.isEditing)
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit Project Info"
                                )
                            else
                                Image(
                                    painterResource(id = R.drawable.round_save_24),
                                    contentDescription = "Save Changes",
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                                )
                        }
                    )
                }

                1 -> {
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
                            .padding(end = 10.dp)
                    )
                }

                2 -> {

                }

                3 -> {
FloatingActionButton(
                        onClick = {
                            if (!projectVM.isWritingFeedback)
                                projectVM.toggleIsWritingFeedback()
                            else {
                                //projectVM.validate()
                            }
                        },
                        modifier = Modifier
                            .padding(end = 10.dp),
                        content = {
                            if (!projectVM.isWritingFeedback)
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Add a Feedback"
                                )
                        }
                    )
                }

                4 -> {
                }
            }
        }
    ) {
        HorizontalPager(state = pagerState, pageSpacing = 10.dp) {
            when (it) {

                0 -> {
                    LandscapeProjectInfoView(
                        actions = actions,
                        projectVM = projectVM,
                    )
                }

                1 -> {

                    LandscapeTasksView(
                        actions = actions,
                        query = taskQuery,
                        onQueryChange = onTaskQueryChange,
                        searchActive = taskSearchActive,
                        onSearchActiveChange = onTaskSearchActiveChange,
                        newTaskVM = newTaskViewModel,
                        projectVM = projectVM,
                        snackbarHostState = snackbarHostState,
                        sortingOrder = taskSortingOrder,
                        sortingOption = taskSortingOption,
                        deadlineFilter = taskDeadlineFilter,
                        statusFilter = taskStatusFilter,
                        priorityFilter = taskPriorityFilter,
                        tagQuery = taskTagQuery,
                        memberQuery = taskMemberQuery,
                        showRecursive = showRecursive,
                        onTaskDelete = onTaskDelete
                    )
                }


                2 -> {
                    LandscapeTeamsView(
                        actions = actions,
                        projectVM = projectVM,
                        teamSortingOrder = teamSortingOrder,
                        teamSortingOption = teamSortingOption,
                        teamMemberQuery = teamMemberQuery,
                        teamCategoryQuery = teamCategoryQuery,
                        teamAdminQuery=  teamAdminQuery,
                        teamQuery = teamQuery,
                        onTeamQueryChange = onTeamQueryChange,
                        searchActive = teamSearchActive,
                        onTeamSearchActiveChange = onTeamSearchActiveChange)

                }

                3 -> {
                    FeedbacksView(actions = actions, projectVM = projectVM)
                }

                4 -> {
                    PerformanceView(actions = actions, projectVM = projectVM)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalFoundationApi::class)
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
    pagerState: PagerState,
    newTaskViewModel: NewTaskViewModel,
    projectVM: ProjectViewModel,
    onTaskDelete: (String)->Unit,
    teamSortingOrder: Boolean,
    onTeamSortingOrderChange: (Boolean) -> Unit,
    teamSortingOption: String,
    onTeamSortingOptionChange: (TeamsSortingOption) -> Unit,
    teamMemberQuery: String,
    onTeamMemberQueryChange: (String) -> Unit,
    teamCategoryQuery: String,
    onTeamCategoryQueryChange: (String) -> Unit,
    teamAdminQuery: String,
    onTeamAdminQueryChange: (String) -> Unit,
    teamQuery: String,
    onTeamQueryChange: (String) -> Unit,
    teamSearchActive: Boolean,
    onTeamSearchActiveChange: (Boolean) -> Unit,
) {

    val snackbarHostState = remember { SnackbarHostState() }
    AppSurface(
        orientation = Orientation.PORTRAIT,
        actions = actions,
        snackbarHostState = snackbarHostState,
        title = projectVM.projectName,
        tabActions = {
            val items = listOf(
                TabItem.ProjectInfo,
                TabItem.ProjectTasks,
                TabItem.ProjectTeams,
                TabItem.ProjectFeedbacks,
                TabItem.ProjectPerformance
            )
            ScrollableTab(
                orientation = Orientation.PORTRAIT,
                items = items,
                pagerState = pagerState,
                selected = pagerState.currentPage,
            )
        },
        leadingTopBarActions = {
            IconButton(
                onClick = { actions.navCont.popBackStack() },
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
            }
        },
        trailingTopBarActions = {
            when (pagerState.currentPage) {

                0 -> {}

                1 -> {
                    var mainExpanded by remember {
                        mutableStateOf(false)
                    }
                    var filterExpanded by remember {
                        mutableStateOf(false)
                    }
                    var sortExpanded by remember {
                        mutableStateOf(false)
                    }

                    TasksActions(
                        mainExpanded = mainExpanded,
                        onMainExpandedChange = { mainExpanded = it },
                        sortExpanded = sortExpanded,
                        onSortExpandedChange = { sortExpanded = it },
                        filterExpanded = filterExpanded,
                        onFilterExpandedChange = { filterExpanded = it },
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
                        onSearchActiveChange = onSearchActiveChange,
                    )
                }


                2 -> {
                    var mainExpanded by remember {
                        mutableStateOf(false)
                    }
                    var filterExpanded by remember {
                        mutableStateOf(false)
                    }
                    var sortExpanded by remember {
                        mutableStateOf(false)
                    }

                    TeamsActions(
                        mainExpanded = mainExpanded,
                        onMainExpandedChange = { mainExpanded = it },
                        sortExpanded = sortExpanded,
                        onSortExpandedChange = { sortExpanded = it },
                        filterExpanded = filterExpanded,
                        onFilterExpandedChange = { filterExpanded = it },
                        sortingOrder = teamSortingOrder,
                        onSortingOrderChange = onTeamSortingOrderChange,
                        sortingOption = teamSortingOption,
                        onSortingOptionChange = onTeamSortingOptionChange,
                        memberQuery = teamMemberQuery,
                        onMemberQueryChange = onTeamMemberQueryChange,
                        categoryQuery = teamCategoryQuery,
                        onCategoryQueryChange = onTeamCategoryQueryChange,
                        adminQuery=  teamAdminQuery,
                        onAdminQueryChange= onTeamAdminQueryChange,
                        onSearchActiveChange = onTeamSearchActiveChange,
                    )
                }

                3 -> {

                }

                4-> {
                    PerformanceActions()
                }
            }
        },
        floatingActionButton = {
            when (pagerState.currentPage) {

                0 -> {
                   if(projectVM.teams.filter { it.users.contains(profileViewModel.userId) }
                        .any { it.admin.contains(profileViewModel.userId) })
                    FloatingActionButton(
                        onClick = {
                            if (!projectVM.isEditing)
                                projectVM.toggleEdit()
                            else {
                                projectVM.checkAll()
                            }
                        },
                        modifier = Modifier
                            .padding(end = 10.dp),
                        content = {
                            if (!projectVM.isEditing)
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit Project Info"
                                )
                            else
                                Image(
                                    painterResource(id = R.drawable.round_save_24),
                                    contentDescription = "Save Changes",
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                                )
                        }
                    )
                }

                1 -> {
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

                        }
                    )
                }

                2 -> {

                }
                3 -> {
                    FloatingActionButton(
                        onClick = {
                            if (!projectVM.isWritingFeedback)
                                projectVM.toggleIsWritingFeedback()
                            else {
                                //projectVM.validate()
                            }
                        },
                        modifier = Modifier
                            .padding(end = 10.dp),
                        content = {
                            if (!projectVM.isWritingFeedback)
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Add a Feedback"
                                )
                        }
                    )
                }

                4 -> {
                }
            }
        }
    ) {

        HorizontalPager(state = pagerState, pageSpacing = 10.dp) {
                when (it) {

                    0 -> {
                        PortraitProjectInfoView(
                            actions = actions,
                            projectVM = projectVM,
                        )
                    }

                    1 -> {

                        PortraitTasksView(
                            actions = actions,
                            query = query,
                            snackbarHostState = snackbarHostState,
                            onQueryChange = onQueryChange,
                            searchActive = searchActive,
                            onSearchActiveChange = onSearchActiveChange,
                            newTaskVM = newTaskViewModel,
                            projectVM = projectVM,
                            sortingOrder = sortingOrder,
                            sortingOption = sortingOption,
                            deadlineFilter = deadlineFilter,
                            statusFilter = statusFilter,
                            priorityFilter = priorityFilter,
                            tagQuery = tagQuery,
                            memberQuery = memberQuery,
                            onTaskDelete = onTaskDelete,
                        )
                    }


                    2 -> {
                        PortraitTeamsView(actions = actions,
                            projectVM = projectVM,
                            teamSortingOrder = teamSortingOrder,
                            teamSortingOption = teamSortingOption,
                            teamMemberQuery = teamMemberQuery,
                            teamCategoryQuery = teamCategoryQuery,
                            teamAdminQuery=  teamAdminQuery,
                            teamQuery = teamQuery,
                            onTeamQueryChange = onTeamQueryChange,
                            searchActive = teamSearchActive,
                            onTeamSearchActiveChange = onTeamSearchActiveChange)
                    }

                    3 -> {
                        FeedbacksView(actions = actions, projectVM = projectVM)
                    }

                    4 -> {
                        PerformanceView(actions = actions, projectVM = projectVM)
                    }
                }
        }
    }
}
