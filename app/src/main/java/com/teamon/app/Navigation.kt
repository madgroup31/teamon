package com.teamon.app

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.teamon.app.account.AccountView
import com.teamon.app.account.SignUpView
import com.teamon.app.utils.viewmodels.UserViewModel
import com.teamon.app.board.BoardView
import com.teamon.app.board.project.ProjectView
import com.teamon.app.utils.viewmodels.ProjectViewModel
import com.teamon.app.utils.viewmodels.ChatViewModel
import com.teamon.app.chats.ChatsView
import com.teamon.app.chats.PersonalChatView
import com.teamon.app.login.GoogleAuthUiClient
import com.teamon.app.login.Login
import com.teamon.app.myteams.TeamView
import com.teamon.app.utils.viewmodels.TeamViewModel
import com.teamon.app.utils.viewmodels.TasksViewModel
import com.teamon.app.tasks.TaskView
import com.teamon.app.utils.viewmodels.TaskViewModel
import com.teamon.app.tasks.TasksView
import com.teamon.app.tasks.attachments.TaskAttachmentInfo
import com.teamon.app.teams.TeamsView
import com.teamon.app.utils.classes.Attachment
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.graphics.LoadingOverlay
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.TabItem
import com.teamon.app.utils.viewmodels.Factory
import com.teamon.app.utils.viewmodels.NewAccountViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Main : Screen("main")
    data object SignUp: Screen("signup")
}

sealed class NavigationItem(
    var title: String,
    var focusedIcon: Int,
    var icon: Int,
    var badgeCounter: Int = 0
) {
    data object Board :
        NavigationItem(
            "board",
            focusedIcon = R.drawable.round_dashboard_24,
            icon = R.drawable.rounded_dashboard_24
        )

    data object MyTasks :
        NavigationItem(
            "tasks",
            focusedIcon = R.drawable.round_calendar_today_24,
            icon = R.drawable.outline_calendar_today_24,
        )

    data object MyTeams :
        NavigationItem(
            "teams",
            focusedIcon = R.drawable.round_groups_24,
            icon = R.drawable.outline_groups_24
        )

    data object Chats :
        NavigationItem(
            "chats",
            focusedIcon = R.drawable.baseline_chat_bubble_outline_24,
            icon = R.drawable.baseline_chat_bubble_24
        )

    data object Account :
        NavigationItem(
            "account",
            focusedIcon = R.drawable.avatar,
            icon = R.drawable.avatar
        )
}

class Actions(val navCont: NavHostController) {

    @SuppressLint("RestrictedApi")
    fun openTask(navigationItem: String, taskId: String) {
        when (navigationItem) {
            NavigationItem.Board.title -> {
                navCont.navigate("${navigationItem}/tasks/${taskId}") {
                    popUpTo("${navigationItem}/tasks/${taskId}") {
                        inclusive = true
                    }
                }
            }

            NavigationItem.MyTasks.title -> {
                navCont.navigate("${navigationItem}/${taskId}") {
                    popUpTo("${navigationItem}/${taskId}") { inclusive = true }
                }
            }
        }

        Log.d(
            "nav",
            printGraphPath(navCont.currentBackStackEntry?.destination?.parent) + "/" + navCont.currentBackStackEntry?.destination?.route
        )
    }

    fun editTask(navigationItem: String, taskId: String) {
        when (navigationItem) {
            NavigationItem.Board.title -> {
                navCont.navigate("${navigationItem}/tasks/${taskId}/edit") {
                    popUpTo("${navigationItem}/tasks/${taskId}/edit") {
                        inclusive = true
                    }
                }
            }

            NavigationItem.MyTasks.title -> {
                navCont.navigate("${navigationItem}/${taskId}/edit") {
                    popUpTo("${navigationItem}/${taskId}/edit") { inclusive = true }
                }
            }
        }

        Log.d(
            "nav",
            printGraphPath(navCont.currentBackStackEntry?.destination?.parent) + "/" + navCont.currentBackStackEntry?.destination?.route
        )
    }

    fun openTaskInfo(navigationItem: String, taskId: String) {
        openTask(navigationItem, taskId)
    }

    fun openTaskHistory(navigationItem: String, taskId: String) {
        when (navigationItem) {
            NavigationItem.Board.title -> {
                navCont.navigate("${navigationItem}/tasks/${taskId}/history") {
                    popUpTo("${navigationItem}/tasks/${taskId}/history") {
                        inclusive = true
                    }
                }
            }

            NavigationItem.MyTasks.title -> {
                navCont.navigate("${navigationItem}/${taskId}/history") {
                    popUpTo("${navigationItem}/${taskId}/history") { inclusive = true }
                }
            }
        }

        Log.d(
            "nav",
            printGraphPath(navCont.currentBackStackEntry?.destination?.parent) + "/" + navCont.currentBackStackEntry?.destination?.route
        )
    }

    fun openTaskComments(navigationItem: String, taskId: String) {
        when (navigationItem) {
            NavigationItem.Board.title -> {
                navCont.navigate("${navigationItem}/tasks/${taskId}/comments") {
                    popUpTo("${navigationItem}/tasks/${taskId}/comments") {
                        inclusive = true
                    }
                }
            }

            NavigationItem.MyTasks.title -> {
                navCont.navigate("${navigationItem}/${taskId}/comments") {
                    popUpTo("${navigationItem}/${taskId}/comments") { inclusive = true }
                }
            }
        }

        Log.d(
            "nav",
            printGraphPath(navCont.currentBackStackEntry?.destination?.parent) + "/" + navCont.currentBackStackEntry?.destination?.route
        )
    }

    fun openTaskAttachments(navigationItem: String, taskId: String) {
        when (navigationItem) {
            NavigationItem.Board.title -> {
                navCont.navigate("${navigationItem}/tasks/${taskId}/attachments") {
                    popUpTo("${navigationItem}/tasks/${taskId}/attachments") {
                        inclusive = true
                    }
                }
            }

            NavigationItem.MyTasks.title -> {
                navCont.navigate("${navigationItem}/${taskId}/attachments") {
                    popUpTo("${navigationItem}/${taskId}/attachments") { inclusive = true }
                }
            }
        }

        Log.d(
            "nav",
            printGraphPath(navCont.currentBackStackEntry?.destination?.parent) + "/" + navCont.currentBackStackEntry?.destination?.route
        )
    }

    fun openAttachmentInfo(currentNavItem: String, taskId: String, attachmentId: String) {
        when (currentNavItem) {
            NavigationItem.Board.title -> {
                navCont.navigate(currentNavItem + "/tasks/${taskId}/attachments/${attachmentId}/info")
            }

            NavigationItem.MyTasks.title -> {
                navCont.navigate(currentNavItem + "/${taskId}/attachments/${attachmentId}/info")
            }
        }
    }


    @SuppressLint("RestrictedApi")
    fun openProject(projectId: String) {
        navCont.navigate(NavigationItem.Board.title + "/${projectId}") {
            popUpTo(NavigationItem.Board.title + "/${projectId}") { inclusive = true }
        }

        Log.d(
            "nav",
            printGraphPath(navCont.currentBackStackEntry?.destination?.parent) + "/" + navCont.currentBackStackEntry?.destination?.route
        )

    }


    fun goToMyTeams() {
        navCont.navigate(NavigationItem.MyTeams.title) {
            popUpTo(NavigationItem.MyTeams.title) { inclusive = true }
        }


    }

    fun goToBoard() {
        navCont.navigate(NavigationItem.Board.title) {
            popUpTo(NavigationItem.Board.title) { inclusive = true }
        }


    }


    @SuppressLint("RestrictedApi")
    fun openTeam(teamId: String) {

        navCont.navigate(NavigationItem.MyTeams.title + "/${teamId}") {
            popUpTo(NavigationItem.MyTeams.title + "/${teamId}") { inclusive = true }
        }

        Log.d(
            "nav",
            printGraphPath(navCont.currentBackStackEntry?.destination?.parent) + "/" + navCont.currentBackStackEntry?.destination?.route
        )

    }

    fun openProjectInfo(projectId: String) {
        navCont.navigate(NavigationItem.Board.title + "/${projectId}/info") {
            popUpTo(NavigationItem.Board.title + "/${projectId}/info") { inclusive = true }
        }

        Log.d(
            "nav",
            printGraphPath(navCont.currentBackStackEntry?.destination?.parent) + "/" + navCont.currentBackStackEntry?.destination?.route
        )

    }

    fun openProjectTasks(projectId: String) {
        openProject(projectId)
    }

    fun openProjectTeams(projectId: String) {
        navCont.navigate(NavigationItem.Board.title + "/${projectId}/teams") {
            popUpTo(NavigationItem.Board.title + "/${projectId}/teams") { inclusive = true }
        }

        Log.d(
            "nav",
            printGraphPath(navCont.currentBackStackEntry?.destination?.parent) + "/" + navCont.currentBackStackEntry?.destination?.route
        )

    }

    fun openProjectFeedbacks(projectId: String) {
        navCont.navigate(NavigationItem.Board.title + "/${projectId}/feedbacks") {
            popUpTo(NavigationItem.Board.title + "/${projectId}/feedbacks") { inclusive = true }
        }

        Log.d(
            "nav",
            printGraphPath(navCont.currentBackStackEntry?.destination?.parent) + "/" + navCont.currentBackStackEntry?.destination?.route
        )

    }

    fun openProjectPerformance(projectId: String) {
        navCont.navigate(NavigationItem.Board.title + "/${projectId}/performance") {
            popUpTo(NavigationItem.Board.title + "/${projectId}/performance") { inclusive = true }
        }

        Log.d(
            "nav",
            printGraphPath(navCont.currentBackStackEntry?.destination?.parent) + "/" + navCont.currentBackStackEntry?.destination?.route
        )

    }

    fun openProfile(navigationItem: String, userId: String) {
        if (userId == profileViewModel!!.userId)
            navCont.navigate(NavigationItem.Account.title) {
                popUpTo(navCont.graph.findStartDestination().id) { saveState = true }
                restoreState = true
            }
        else
            navCont.navigate("${navigationItem}/users/${userId}") {
                popUpTo(navigationItem + "/users/${userId}") { inclusive = true }
                restoreState = true
            }

        Log.d(
            "nav",
            printGraphPath(navCont.currentBackStackEntry?.destination?.parent) + "/" + navCont.currentBackStackEntry?.destination?.route
        )
    }

    @SuppressLint("RestrictedApi")
    fun openTeamChat(teamId: String) {
        navCont.navigate(NavigationItem.MyTeams.title + "/${teamId}/chat") {
            popUpTo(NavigationItem.MyTeams.title + "/${teamId}/chat") {
                inclusive = true
            }
        }
    }

    fun openPersonalChat(userId: String, teamId: String) {
        navCont.navigate(NavigationItem.Chats.title + "/${teamId}" + "/chat" + "/${userId}") {
            popUpTo(NavigationItem.Chats.title + "/${teamId}" + "/chat" + "/${userId}") {
                inclusive = true
            }
        }
    }

    fun openTeamMembers(navigationItem: String, teamId: String) {
        navCont.navigate(navigationItem + "/teams/${teamId}/members") {
            popUpTo(navigationItem + "/teams/${teamId}/members") {
                inclusive = true
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun NavGraphBuilder.boardGraph(actions: Actions) {

    //Board with all projects the user is involved in (startDestination component)
    composable(NavigationItem.Board.title) {
        BoardView(actions = actions)
    }

    composable(NavigationItem.MyTeams.title + "/{teamId}/join",
        arguments = listOf(
            navArgument("teamId") { type = NavType.StringType }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "teamon.app/${NavigationItem.MyTeams.title}/{teamId}/join"
                action = Intent.ACTION_VIEW
            })
    ) {
        TeamsView(actions = actions, joinRequest = it.arguments?.getString("teamId"))

    }

    //Single project view
    composable(NavigationItem.Board.title + "/{projectId}",
        arguments = listOf(navArgument("projectId") { type = NavType.StringType }
        )) {


        val projectId = it.arguments?.getString("projectId")
        if (projectId != null) {
            ProjectView(
                actions = actions,
                projectVM = viewModel<ProjectViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        projectId = projectId
                    )
                ),
            )


        }
    }

    //Single project view - Info
    composable(NavigationItem.Board.title + "/{projectId}/info",
        arguments = listOf(navArgument("projectId") { type = NavType.StringType }
        )) {

        val projectId = it.arguments?.getString("projectId")
        if (projectId != null) {
            ProjectView(
                actions = actions,
                projectVM = viewModel<ProjectViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        projectId = projectId
                    )
                ),
                startingTab = TabItem.ProjectInfo.title
            )

        }
    }

    //Single project view - Tasks
    composable(NavigationItem.Board.title + "/{projectId}/tasks",
        arguments = listOf(navArgument("projectId") { type = NavType.StringType }
        )) {

        val projectId = it.arguments?.getString("projectId")
        if (projectId != null) {
            ProjectView(
                actions = actions,
                projectVM = viewModel<ProjectViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        projectId = projectId
                    )
                ),
                startingTab = TabItem.ProjectTasks.title
            )

        }
    }

    //Single project view - Teams
    composable(NavigationItem.Board.title + "/{projectId}/teams",
        arguments = listOf(navArgument("projectId") { type = NavType.StringType }
        )) {

        val projectId = it.arguments?.getString("projectId")
        if (projectId != null) {
            ProjectView(
                actions = actions,
                projectVM = viewModel<ProjectViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        projectId = projectId
                    )
                ),
                startingTab = TabItem.ProjectTeams.title
            )

        }
    }

    //Single project view - Feedbacks
    composable(NavigationItem.Board.title + "/{projectId}/feedbacks",
        arguments = listOf(navArgument("projectId") { type = NavType.StringType }
        )) {

        val projectId = it.arguments?.getString("projectId")
        if (projectId != null) {
            ProjectView(
                actions = actions,
                projectVM = viewModel<ProjectViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        projectId = projectId
                    )
                ),
                startingTab = TabItem.ProjectFeedbacks.title
            )

        }
    }

    //Single project view - Performance
    composable(NavigationItem.Board.title + "/{projectId}/performance",
        arguments = listOf(navArgument("projectId") { type = NavType.StringType }
        )) {

        val projectId = it.arguments?.getString("projectId")
        if (projectId != null) {
            ProjectView(
                actions = actions,
                projectVM = viewModel<ProjectViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        projectId = projectId
                    )
                ),
                startingTab = TabItem.ProjectPerformance.title
            )
        }
    }


    //Single task view (when opening it from projects)
    composable(NavigationItem.Board.title + "/tasks/{taskId}",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->

        val taskId = navBackStackEntry.arguments?.getString("taskId")

        if (taskId != null) {
            Log.d("task", "taskId: $taskId")
            // Display the task details
            TaskView(
                actions = actions,
                taskViewModel = viewModel<TaskViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        taskId = taskId
                    )
                ),
            )
        }
    }


    //Single task view - Info(when opening it from projects)
    composable(NavigationItem.Board.title + "/tasks/{taskId}/info",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val taskId = navBackStackEntry.arguments?.getString("taskId")
        // Display the task details
        TaskView(
            actions = actions,
            startingTab = TabItem.TaskInfo.title,
            taskViewModel = viewModel<TaskViewModel>(
                factory = Factory(
                    LocalContext.current.applicationContext,
                    taskId = taskId
                )
            ),
        )
    }

    //Single task view - History (when opening it from projects)
    composable(NavigationItem.Board.title + "/tasks/{taskId}/history",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val taskId = navBackStackEntry.arguments?.getString("taskId")
        //val task = teamOnViewModel!!.model.tasks[taskId]


        if (taskId != null)
        // Display the task details
            TaskView(
                actions = actions,
                startingTab = TabItem.TaskHistory.title,
                taskViewModel = viewModel<TaskViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        taskId = taskId
                    )
                ),
            )
    }

    //Single task view - Comments (when opening it from projects)
    composable(NavigationItem.Board.title + "/tasks/{taskId}/comments",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val taskId = navBackStackEntry.arguments?.getString("taskId")
        //val task = teamOnViewModel!!.model.tasks[taskId]


        if (taskId != null)
        // Display the task details
            TaskView(
                actions = actions,
                startingTab = TabItem.TaskComments.title,
                taskViewModel = viewModel<TaskViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        taskId = taskId
                    )
                ),
            )
    }

    //Single task view - Attachments (when opening it from projects)
    composable(NavigationItem.Board.title + "/tasks/{taskId}/attachments",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val taskId = navBackStackEntry.arguments?.getString("taskId")
        //val task = teamOnViewModel!!.model.tasks[taskId]


        if (taskId != null)
        // Display the task details
            TaskView(
                actions = actions,
                startingTab = TabItem.TaskAttachments.title,
                taskViewModel = viewModel<TaskViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        taskId = taskId
                    )
                ),
            )
    }

    //Single task view - Attachment info (when opening it from projects)
    dialog(
        NavigationItem.Board.title + "/tasks/{taskId}/attachments/{attachmentId}/info",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType },
            navArgument("attachmentId") { type = NavType.StringType },
        )
    ) { navBackStackEntry ->
        val taskId = navBackStackEntry.arguments?.getString("taskId")
        val attachmentId = navBackStackEntry.arguments?.getString("attachmentId")
        if (taskId != null && attachmentId != null) {

            val project by tasksViewModel!!.getTaskProject(taskId)
                .collectAsState(initial = Project())
            val attachment by attachmentsViewModel!!.getAttachment(attachmentId)
                .collectAsState(initial = Attachment())

            if (project.projectId.isNotBlank() && attachment.attachmentId.isNotBlank())
            // Display the task details
                Theme(color = project.projectColor, applyToStatusBar = false) {
                    TaskAttachmentInfo(attachment = attachment, actions = actions)
                }
        }
    }

    //Edit task (when opening it from projects)
    composable(NavigationItem.Board.title + "/tasks/{taskId}/edit",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->

        val taskId = navBackStackEntry.arguments?.getString("taskId")

        val taskViewModel = viewModel<TaskViewModel>(
            factory = Factory(
                LocalContext.current.applicationContext,
                taskId = taskId
            )
        )
        taskViewModel.edit()

        // Display the task details
        TaskView(
            actions = actions,
            startingTab = TabItem.TaskInfo.title,
            taskViewModel = taskViewModel,
        )
    }

    //Open user details (when opening it from projects)
    composable(NavigationItem.Board.title + "/users/{userId}",
        arguments = listOf(
            navArgument("userId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val userId = navBackStackEntry.arguments?.getString("userId")
        if ((userId != null)) {
            AccountView(
                actions = actions,
                userVm = viewModel<UserViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        userId = userId
                    )
                )
            )
        }
    }

    //Open team members from project
    composable(NavigationItem.Board.title + "/teams/{teamId}/members",
        arguments = listOf(
            navArgument("teamId") { type = NavType.StringType }
        )) {
        val teamId = it.arguments?.getString("teamId")

        TeamView(
            actions = actions, teamVM = viewModel<TeamViewModel>(
                factory = Factory(
                    LocalContext.current.applicationContext,
                    teamId = teamId,
                )
            ),
            startingTab = TabItem.TeamMembers.title
        )
    }

}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun NavGraphBuilder.tasksGraph(actions: Actions) {


    //User assigned tasks view
    composable(NavigationItem.MyTasks.title) {
        val myTasksViewModel =
            viewModel<TasksViewModel>(factory = Factory(LocalContext.current.applicationContext))
        TasksView(
            actions = actions,
            myTasksViewModel = myTasksViewModel
        )
    }

    //Single task view (when opening it from assigned tasks)
    composable(NavigationItem.MyTasks.title + "/{taskId}",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val taskId = navBackStackEntry.arguments?.getString("taskId")

        if (taskId != null)
        // Display the task details
            TaskView(
                actions = actions,
                taskViewModel = viewModel<TaskViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        taskId = taskId
                    )
                ),
            )
    }

    //Single task view - Info (when opening it from assigned tasks)
    composable(NavigationItem.MyTasks.title + "/{taskId}/info",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val taskId = navBackStackEntry.arguments?.getString("taskId")

        if (taskId != null)
        // Display the task details
            TaskView(
                actions = actions,
                startingTab = TabItem.TaskInfo.title,
                taskViewModel = viewModel<TaskViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        taskId = taskId
                    )
                ),
            )
    }

    //Single task view - History (when opening it from assigned tasks)
    composable(NavigationItem.MyTasks.title + "/{taskId}/history",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val taskId = navBackStackEntry.arguments?.getString("taskId")

        if (taskId != null)
        // Display the task details
            TaskView(
                actions = actions,
                startingTab = TabItem.TaskHistory.title,
                taskViewModel = viewModel<TaskViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        taskId = taskId
                    )
                ),
            )
    }

    //Single task view - Comments (when opening it from assigned tasks)
    composable(NavigationItem.MyTasks.title + "/{taskId}/comments",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val taskId = navBackStackEntry.arguments?.getString("taskId")?.let {
            val task by tasksViewModel.getTask(it).collectAsState(initial = Task())

            // Display the task details
            TaskView(
                actions = actions,
                startingTab = TabItem.TaskComments.title,
                taskViewModel = viewModel<TaskViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        taskId = it
                    )
                ),
            )
        }
    }

    //Single task view - Attachments (when opening it from assigned tasks)
    composable(NavigationItem.MyTasks.title + "/{taskId}/attachments",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val taskId = navBackStackEntry.arguments?.getString("taskId")

        if (taskId != null)
        // Display the task details
            TaskView(
                actions = actions,
                startingTab = TabItem.TaskAttachments.title,
                taskViewModel = viewModel<TaskViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        taskId = taskId
                    )
                ),
            )
    }

    //Single task view - Attachment info (when opening it from tasks)
    dialog(
        NavigationItem.MyTasks.title + "/{taskId}/attachments/{attachmentId}/info",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType },
            navArgument("attachmentId") { type = NavType.StringType },
        )
    ) { navBackStackEntry ->
        val taskId = navBackStackEntry.arguments?.getString("taskId")
        val attachmentId = navBackStackEntry.arguments?.getString("attachmentId")
        if (attachmentId != null && taskId != null) {
            val project by tasksViewModel!!.getTaskProject(taskId)
                .collectAsState(initial = Project())
            val attachment by attachmentsViewModel!!.getAttachment(attachmentId)
                .collectAsState(initial = Attachment())

            if (project.projectId.isNotBlank() && attachment.attachmentId.isNotBlank())
            // Display the task details
                Theme(color = project.projectColor, applyToStatusBar = false) {
                    TaskAttachmentInfo(attachment = attachment, actions = actions)
                }
        }
    }


    //Edit task view (when opening it from assigned tasks)
    composable(NavigationItem.MyTasks.title + "/{taskId}/edit",
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val taskId = navBackStackEntry.arguments?.getString("taskId")
        val taskViewModel = viewModel<TaskViewModel>(
            factory = Factory(
                LocalContext.current.applicationContext,
                taskId = taskId
            )
        )
        taskViewModel.edit()

        // Display the task details
        TaskView(
            actions = actions,
            startingTab = TabItem.TaskInfo.title,
            taskViewModel = taskViewModel,
        )

    }

    //Open user details (when opening it from tasks)
    composable(NavigationItem.MyTasks.title + "/users/{userId}",
        arguments = listOf(
            navArgument("userId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val userId = navBackStackEntry.arguments?.getString("userId")
        if ((userId != null)) {
            AccountView(
                actions = actions,
                userVm = viewModel<UserViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        userId = userId
                    )
                )
            )
        }
    }

    //Open team members from tasks
    composable(NavigationItem.MyTasks.title + "/teams/{teamId}/members",
        arguments = listOf(
            navArgument("teamId") { type = NavType.StringType }
        )) {
        val teamId = it.arguments?.getString("teamId")
        TeamView(
            actions = actions, teamVM = viewModel<TeamViewModel>(
                factory = Factory(
                    LocalContext.current.applicationContext,
                    teamId = teamId
                )
            ),
            startingTab = TabItem.TeamMembers.title
        )
    }
}


fun NavGraphBuilder.teamsGraph(actions: Actions) {

    //Team view
    composable(NavigationItem.MyTeams.title) {
        TeamsView(actions = actions)
    }

    //Single team view
    composable(NavigationItem.MyTeams.title + "/{teamId}",
        arguments = listOf(navArgument("teamId") { type = NavType.StringType }
        )) {

        val teamId = it.arguments?.getString("teamId")

        TeamView(
            actions = actions,
            teamVM = viewModel<TeamViewModel>(
                factory = Factory(
                    LocalContext.current.applicationContext,
                    teamId = teamId
                )
            )
        )

    }

    //open user profile when selected by members in team
    composable(NavigationItem.MyTeams.title + "/users/{userId}",
        arguments = listOf(
            navArgument("userId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val userId = navBackStackEntry.arguments?.getString("userId")
        if ((userId != null)) {
            AccountView(
                actions = actions,
                userVm = viewModel<UserViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        userId = userId
                    )
                )
            )
        }
    }

    //Open team chat
    composable(
        NavigationItem.MyTeams.title + "/{teamId}" + "/chat",
        arguments = listOf(
            navArgument("teamId") { type = NavType.StringType },
        )
    ) {

        val teamId = it.arguments?.getString("teamId")

        if (teamId != null) {
            TeamView(
                actions = actions,
                teamVM = viewModel<TeamViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        teamId = teamId
                    )
                ),
                startingTab = TabItem.TeamChat.title
            )
        }
    }

    //Open team members from teams
    composable(NavigationItem.MyTeams.title + "/teams/{teamId}/members",
        arguments = listOf(
            navArgument("teamId") { type = NavType.StringType }
        )) {
        val teamId = it.arguments?.getString("teamId")
        TeamView(
            actions = actions, teamVM = viewModel<TeamViewModel>(
                factory = Factory(
                    LocalContext.current.applicationContext,
                    teamId = teamId
                )
            ),
            startingTab = TabItem.TeamMembers.title
        )
    }

}

fun NavGraphBuilder.chatsGraph(actions: Actions) {

    //Started projects chats view
    composable(NavigationItem.Chats.title) {
        ChatsView(actions = actions)
    }

    //Personal chat
    composable(
        NavigationItem.Chats.title + "/{teamId}" + "/chat" + "/{userId}",
        arguments = listOf(
            navArgument("teamId") { type = NavType.StringType },
            navArgument("userId") { type = NavType.StringType },
        )
    ) {

        val teamId = it.arguments?.getString("teamId")
        val userId = it.arguments?.getString("userId")

        if (teamId != null && userId != null) {
            PersonalChatView(
                actions = actions,
                chatVm = viewModel<ChatViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        teamId = teamId,
                        userId = userId,
                    )
                )
            )
        }
    }
    ///users/${userId}
    //Personal chat
    composable(
        NavigationItem.Chats.title + "/users/{userId}",
        arguments = listOf(
            navArgument("userId") { type = NavType.StringType },
        )
    ) {

        val userId = it.arguments?.getString("userId")

        if (userId != null) {
            AccountView(
                actions = actions,
                userVm = viewModel<UserViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        userId = userId
                    )
                )
            )
        }
    }

}

fun NavGraphBuilder.accountGraph(actions: Actions) {

    //User account view
    composable(NavigationItem.Account.title) {
        AccountView(actions = actions)
    }

    //open user profile when selected by feedback in account
    composable(NavigationItem.Account.title + "/users/{userId}",
        arguments = listOf(
            navArgument("userId") { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val userId = navBackStackEntry.arguments?.getString("userId")
        if ((userId != null)) {
            AccountView(
                actions = actions,
                userVm = viewModel<UserViewModel>(
                    factory = Factory(
                        LocalContext.current.applicationContext,
                        userId = userId
                    )
                )
            )
        }
    }

}


fun NavGraphBuilder.loginGraph(actions: Actions) {
    navigation(startDestination = "start", route = Screen.Login.route) {
        composable("start") {
            val context = LocalContext.current.applicationContext
            val googleAuthUiClient =
                GoogleAuthUiClient(
                    context = context,
                    oneTapClient = Identity.getSignInClient(context)
                )
            val state by profileViewModel.state.collectAsState()
            var loading by remember { mutableStateOf(false) }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = { result ->
                    if (result.resultCode == RESULT_OK) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val signInResult = googleAuthUiClient.signInWithIntent(
                                intent = result.data ?: return@launch
                            )
                            profileViewModel.onSignInResult(signInResult)
                        }
                    }
                }
            )

            LaunchedEffect(key1 = state.isSignInSuccessful) {
                if (state.isSignInSuccessful != null) {
                    if (!usersViewModel.exists(profileViewModel.userId)) {
                        if(!state.isAnonymous)
                            actions.navCont.navigate(Screen.SignUp.route)
                        else {
                            actions.navCont.navigate(Screen.Main.route)
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Welcome back " + profileViewModel.nameValue + "!",
                            Toast.LENGTH_LONG
                        ).show()
                        actions.navCont.navigate(Screen.Main.route)
                    }
                    loading = false
                } else {
                    googleAuthUiClient.signOut()
                }
            }

            if(loading)
                LoadingOverlay(isLoading = loading)

            Login(state = state,
                onSignInClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        loading = true
                        val signInIntentSender = googleAuthUiClient.signIn()
                        launcher.launch(
                            IntentSenderRequest.Builder(
                                signInIntentSender ?: return@launch
                            ).build()
                        )
                    }
                },
                onAnonymousSignInClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        loading = true
                        val signInResult = googleAuthUiClient.signInAnonymously()
                        profileViewModel.onSignInResult(signInResult)
                    }
                })
        }
    }
}

fun NavGraphBuilder.signUpGraph(actions: Actions) {
    navigation(startDestination = "start", route = Screen.SignUp.route) {
    composable("start") {
        SignUpView(
            actions = actions,
            newUserVm = viewModel<NewAccountViewModel>(
                factory = Factory(
                    LocalContext.current.applicationContext,
                )
            )
        )
    }
        }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun NavGraphBuilder.mainGraph(actions: Actions) {

    navigation(startDestination = NavigationItem.Board.title, route = Screen.Main.route) {
        boardGraph(actions)
        tasksGraph(actions)
        teamsGraph(actions)
        chatsGraph(actions)
        accountGraph(actions)
    }
}

fun printGraphPath(entry: NavGraph?): String {
    if (entry?.route == null) return ""
    return printGraphPath(entry.parent) + "/" + entry.route
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun Navigator() {
    val auth = Firebase.auth
    val navController = rememberNavController()
    val actions = remember(navController) { Actions(navController) }

    var startDestination by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(auth.currentUser) {
        if (auth.currentUser != null)
            if (usersViewModel.exists(auth.currentUser!!.uid))
                startDestination = Screen.Main.route
            else startDestination = Screen.SignUp.route
        else startDestination = Screen.Login.route

    }

    startDestination?.let {
        NavHost(navController = navController, startDestination = it) {
            loginGraph(actions)
            signUpGraph(actions)
            mainGraph(actions)
        }
    }

}