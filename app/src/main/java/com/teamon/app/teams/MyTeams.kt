package com.teamon.app.teams

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.teamon.app.Actions
import com.teamon.app.utils.viewmodels.Factory
import com.teamon.app.NavigationItem
import com.teamon.app.R
import com.teamon.app.board.project.ProjectCard
import com.teamon.app.myteams.TeamCard
import com.teamon.app.profileViewModel
import com.teamon.app.teamOnViewModel
import com.teamon.app.utils.viewmodels.NewTeamViewModel
import com.teamon.app.teamsViewModel
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.graphics.AnimatedGrid
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.SearchBar
import com.teamon.app.utils.graphics.TeamsFilteringOptionsDropdownMenu
import com.teamon.app.utils.graphics.TeamsSortingOption
import com.teamon.app.utils.graphics.TeamsSortingOptionsDropdownMenu
import com.teamon.app.utils.graphics.TeamsViewDropdownMenu
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.prepare
import com.teamon.app.utils.themes.teamon.TeamOnTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.map


enum class JoinRequest { Accepted, Rejected, AlreadyPartecipating, NotFound, Requesting, Error }

@Composable
fun TeamsView(actions: Actions, joinRequest: String? = null) {

    Theme(color = profileViewModel.color, applyToStatusBar = true) {

        var snackbarHostState = remember { SnackbarHostState() }

        var sortingOrder by rememberSaveable {
            mutableStateOf(false)
        }
        var sortingOption by rememberSaveable {
            mutableStateOf(TeamsSortingOption.CreationDate.title)
        }
        var categoryQuery by rememberSaveable {
            mutableStateOf("")
        }
        var memberQuery by rememberSaveable {
            mutableStateOf("")
        }
        var adminQuery by rememberSaveable {
            mutableStateOf("")
        }
        var searchActive by rememberSaveable {
            mutableStateOf(false)
        }
        var query by rememberSaveable {
            mutableStateOf("")
        }

        val onSortingOrderChange: (Boolean) -> Unit = { sortingOrder = !it }
        val onSortingOptionChange: (TeamsSortingOption) -> Unit =
            { sortingOption = it.title }
        val onSearchActiveChange: (Boolean) -> Unit =
            { searchActive = it }
        val onQueryChange: (String) -> Unit =
            { query = it }
        val onCategoryQueryChange: (String) -> Unit =
            { categoryQuery = it }
        val onAdminQueryChange: (String) -> Unit =
            { adminQuery = it }
        val onMemberQueryChange: (String) -> Unit =
            { memberQuery = it }


        var landscape by remember { mutableStateOf(false) }
        landscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        val newTeamViewModel = viewModel<NewTeamViewModel>(
            factory = Factory(
                LocalContext.current.applicationContext,
                userId = profileViewModel!!.userId
            )
        )


        if (landscape) LandscapeView(
            snackbarHostState = snackbarHostState,
            actions = actions,
            newTeamVM = newTeamViewModel,
            query = query,
            onQueryChange = onQueryChange,
            searchActive = searchActive,
            onSearchActiveChange = onSearchActiveChange,
            sortingOrder = sortingOrder,
            onSortingOrderChange = onSortingOrderChange,
            sortingOption = sortingOption,
            onSortingOptionChange = onSortingOptionChange,
            memberQuery = memberQuery,
            onMemberQueryChange = onMemberQueryChange,
            adminQuery = adminQuery,
            onAdminQueryChange = onAdminQueryChange,
            categoryQuery = categoryQuery,
            onCategoryQueryChange = onCategoryQueryChange
        )
        else PortraitView(
            snackbarHostState = snackbarHostState,
            actions = actions,
            newTeamVM = newTeamViewModel,
            query = query,
            onQueryChange = onQueryChange,
            searchActive = searchActive,
            onSearchActiveChange = onSearchActiveChange,
            sortingOrder = sortingOrder,
            onSortingOrderChange = onSortingOrderChange,
            sortingOption = sortingOption,
            onSortingOptionChange = onSortingOptionChange,
            memberQuery = memberQuery,
            onMemberQueryChange = onMemberQueryChange,
            adminQuery = adminQuery,
            onAdminQueryChange = onAdminQueryChange,
            categoryQuery = categoryQuery,
            onCategoryQueryChange = onCategoryQueryChange
        )

        var joinStatus by rememberSaveable { mutableStateOf(if (joinRequest != null) JoinRequest.Requesting else null) }
        val team by if (joinRequest != null) teamsViewModel.getTeam(joinRequest).collectAsState(initial = null) else remember { mutableStateOf(null) }

        if (joinStatus == JoinRequest.Requesting) {

            val isAlreadyPartecipating = team?.users?.contains(profileViewModel!!.userId)
            Box(modifier = Modifier.fillMaxSize()) {
                if(team != null) {
                    if (joinRequest == null || team == Team()) {
                        joinStatus = JoinRequest.NotFound
                    } else if (isAlreadyPartecipating == false) {
                        AlertDialog(
                            modifier = Modifier.zIndex(100f),
                            onDismissRequest = { },
                            icon = {
                                Image(
                                    modifier = Modifier.size(30.dp),
                                    painter = painterResource(R.drawable.ic_action_name),
                                    colorFilter = ColorFilter.tint(
                                        MaterialTheme.colorScheme.primary
                                    ),
                                    contentDescription = "TeamOn logo"
                                )
                            },
                            title = { Text("Join Request", textAlign = TextAlign.Center) },
                            text = {
                                Text(
                                    "Do you want to join \"" + team!!.name + "\"?\nBy continuing, you will be automatically involved in all the projects  \"" + team!!.name + "\" is participating.",
                                    textAlign = TextAlign.Center
                                )
                            },
                            confirmButton = {
                                Button(onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        if (
                                            teamsViewModel!!.addTeamMember(
                                                profileViewModel!!.userId,
                                                joinRequest
                                            )
                                        ) joinStatus = JoinRequest.Accepted
                                        else joinStatus = JoinRequest.Error
                                    }
                                }) {
                                    Text("Join", textAlign = TextAlign.Center)
                                }
                            },
                            dismissButton = {
                                FilledTonalButton(onClick = { joinStatus = JoinRequest.Rejected }) {
                                    Text("Reject", textAlign = TextAlign.Center)
                                }
                            })
                    } else joinStatus = JoinRequest.AlreadyPartecipating
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp), contentAlignment = Alignment.BottomCenter
        ) {
            when (joinStatus) {
                JoinRequest.NotFound -> {
                    LaunchedEffect(Unit) {
                        snackbarHostState.showSnackbar(message = "Invalid invitation. The team does not exist.")
                        }
                }

                JoinRequest.Accepted -> {
                    LaunchedEffect(Unit) {
                        when(snackbarHostState.showSnackbar(message = "Request to join \"" + team!!.name + "\" accepted!", actionLabel = "Open", withDismissAction = true)) {
                            SnackbarResult.ActionPerformed -> {
                                actions.openTeamMembers(
                                    NavigationItem.MyTeams.title, teamId = team!!.teamId
                                )
                            }
                            SnackbarResult.Dismissed -> {}
                        }

                    }
                }

                JoinRequest.Rejected -> {
                    LaunchedEffect(Unit) {
                        snackbarHostState.showSnackbar(message = "Request to join \"" + team!!.name + "\" rejected.")
                    }
                }

                JoinRequest.AlreadyPartecipating -> {
                    LaunchedEffect(Unit) {
                        when(snackbarHostState.showSnackbar(message = "Everything's ready! You're already part of this team.", actionLabel = "Open", withDismissAction = true)) {
                            SnackbarResult.ActionPerformed -> {
                                actions.openTeamMembers(
                                    NavigationItem.MyTeams.title, teamId = team!!.teamId
                                )
                            }
                            SnackbarResult.Dismissed -> {}
                        }

                    }
                }

                JoinRequest.Error -> {
                    LaunchedEffect(Unit) {
                        snackbarHostState.showSnackbar(message = "An error occurred. Please try again.", withDismissAction = true)
                    }
                }

                else -> {

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandscapeView(
    snackbarHostState: SnackbarHostState,
    actions: Actions,
    newTeamVM: NewTeamViewModel,
    query: String,
    onQueryChange: (String) -> Unit,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    sortingOrder: Boolean,
    onSortingOrderChange: (Boolean) -> Unit,
    sortingOption: String,
    onSortingOptionChange: (TeamsSortingOption) -> Unit,
    memberQuery: String,
    onMemberQueryChange: (String) -> Unit,
    adminQuery: String,
    onAdminQueryChange: (String) -> Unit,
    categoryQuery: String,
    onCategoryQueryChange: (String) -> Unit
) {

    val teams by teamsViewModel!!.getTeams().collectAsState(initial = emptyMap())


    val sheetState = rememberModalBottomSheetState()

    AppSurface(
        orientation = Orientation.LANDSCAPE,
        actions = actions,
        snackbarHostState = snackbarHostState,
        title = "My Teams",
        trailingTopBarActions = {
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
                Icon(Icons.Rounded.MoreVert, contentDescription = "More tasks options")
            }

            TeamsViewDropdownMenu(
                mainExpanded = mainExpanded,
                onMainExpandedChange = { mainExpanded = it },
                sortExpanded = sortExpanded,
                onSortExpandedChange = { sortExpanded = it },
                filterExpanded = filterExpanded,
                onFilterExpandedChange = { filterExpanded = it }
            )

            TeamsSortingOptionsDropdownMenu(
                sortExpanded = sortExpanded,
                sortingOrder = sortingOrder,
                onSortingOrderChange = onSortingOrderChange,
                onSortExpandedChange = { sortExpanded = it },
                onMainExpandedChange = { mainExpanded = it },
                sortingOption = sortingOption,
                onSortingOptionChange = onSortingOptionChange
            )

            TeamsFilteringOptionsDropdownMenu(
                filterExpanded = filterExpanded,
                onFilterExpandedChange = { filterExpanded = it },
                categoryQuery = categoryQuery,
                onCategoryQueryChange = onCategoryQueryChange,
                memberQuery = memberQuery,
                onMemberQueryChange = onMemberQueryChange,
                adminQuery = adminQuery,
                onAdminQueryChange = onAdminQueryChange
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { newTeamVM.toggleShow() },
                content = {
                    Image(
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        painter = painterResource(R.drawable.round_group_add_24),
                        contentDescription = null
                    )

                },
                modifier = Modifier
                    .padding(end = 10.dp)
            )

        }
    ) {

        if (searchActive) {
            val teams = teams.values.toList().prepare(
                sortingOrder = sortingOrder,
                sortingOption = sortingOption,
                memberQuery = memberQuery,
                categoryQuery = categoryQuery,
                adminQuery = adminQuery,
                query = query,
            )
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(20f)
                    .padding(10.dp),
                query = query,
                placeholder = "Search Teams...",
                onQueryChange = onQueryChange,
                searchActive = searchActive,
                onSearchActiveChange = onSearchActiveChange
            ) {
                if (teams.isEmpty())
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Available Teams.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic
                        )
                    }
                else {
                    AnimatedGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        items = teams
                    ) { it, index ->
                        TeamCard(team = it as Team, actions = actions)
                    }
                }
            }
        } else {
            val teams = teams.values.toList().prepare(
                sortingOrder = sortingOrder,
                sortingOption = sortingOption,
                memberQuery = memberQuery,
                categoryQuery = categoryQuery,
                adminQuery = adminQuery,
                query = "",
            )
            if (teams.isEmpty())
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Available Teams.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic
                    )
                }
            else {
                AnimatedGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    items = teams
                ) { it, index ->
                    TeamCard(team = it as Team, actions = actions)
                }
            }
        }

    }

    if (newTeamVM.isShowing) {
        ModalBottomSheet(
            modifier = Modifier,
            onDismissRequest = {
                newTeamVM.toggleShow()
            },
            sheetState = sheetState
        ) {
            // Sheet content
            ModalBottomSheetContentTeam(
                actions = actions,
                newTeamVM = newTeamVM
            )
        }
    }

}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortraitView(
    snackbarHostState: SnackbarHostState,
    actions: Actions,
    newTeamVM: NewTeamViewModel,
    query: String,
    onQueryChange: (String) -> Unit,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    sortingOrder: Boolean,
    onSortingOrderChange: (Boolean) -> Unit,
    sortingOption: String,
    onSortingOptionChange: (TeamsSortingOption) -> Unit,
    memberQuery: String,
    onMemberQueryChange: (String) -> Unit,
    adminQuery: String,
    onAdminQueryChange: (String) -> Unit,
    categoryQuery: String,
    onCategoryQueryChange: (String) -> Unit

) {

    val teams by teamsViewModel!!.getTeams().collectAsState(initial = emptyMap())

    val sheetState = rememberModalBottomSheetState()

    AppSurface(
        orientation = Orientation.PORTRAIT,
        actions = actions,
        snackbarHostState = snackbarHostState,
        title = "My Teams",
        trailingTopBarActions = {
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
                Icon(Icons.Rounded.MoreVert, contentDescription = "More tasks options")
            }

            TeamsViewDropdownMenu(
                mainExpanded = mainExpanded,
                onMainExpandedChange = { mainExpanded = it },
                sortExpanded = sortExpanded,
                onSortExpandedChange = { sortExpanded = it },
                filterExpanded = filterExpanded,
                onFilterExpandedChange = { filterExpanded = it }
            )

            TeamsSortingOptionsDropdownMenu(
                sortExpanded = sortExpanded,
                sortingOrder = sortingOrder,
                onSortingOrderChange = onSortingOrderChange,
                onSortExpandedChange = { sortExpanded = it },
                onMainExpandedChange = { mainExpanded = it },
                sortingOption = sortingOption,
                onSortingOptionChange = onSortingOptionChange
            )

            TeamsFilteringOptionsDropdownMenu(
                filterExpanded = filterExpanded,
                onFilterExpandedChange = { filterExpanded = it },
                categoryQuery = categoryQuery,
                onCategoryQueryChange = onCategoryQueryChange,
                memberQuery = memberQuery,
                onMemberQueryChange = onMemberQueryChange,
                adminQuery = adminQuery,
                onAdminQueryChange = onAdminQueryChange
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { newTeamVM.toggleShow() },
                content = {
                    Image(
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        painter = painterResource(R.drawable.round_group_add_24),
                        contentDescription = null
                    )
                },
                modifier = Modifier
            )

        }
    ) {
        if (searchActive) {
            val teams = teams.values.toList().prepare(
                sortingOrder = sortingOrder,
                sortingOption = sortingOption,
                memberQuery = memberQuery,
                categoryQuery = categoryQuery,
                adminQuery = adminQuery,
                query = query,
            )
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(20f)
                    .padding(10.dp),
                query = query,
                placeholder = "Search Teams...",
                onQueryChange = onQueryChange,
                searchActive = searchActive,
                onSearchActiveChange = onSearchActiveChange
            ) {
                if (teams.isEmpty())
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Available Teams.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic
                        )
                    }
                else {
                    AnimatedGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        items = teams
                    ) { it, index ->
                        TeamCard(team = it as Team, actions = actions)
                    }
                }
            }
        } else {
            val teams = teams.values.toList().prepare(
                sortingOrder = sortingOrder,
                sortingOption = sortingOption,
                memberQuery = memberQuery,
                categoryQuery = categoryQuery,
                adminQuery = adminQuery,
                query = "",
            )
            if (teams.isEmpty())
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Available Teams.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic
                    )
                }
            else {
                AnimatedGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    items = teams
                ) { it, index ->
                    TeamCard(team = it as Team, actions = actions)
                }
            }
        }

    }
    if (newTeamVM.isShowing) {
        ModalBottomSheet(
            modifier = Modifier,
            onDismissRequest = {
                newTeamVM.toggleShow()
            },
            sheetState = sheetState
        ) {
            // Sheet content
            ModalBottomSheetContentTeam(
                actions = actions,
                newTeamVM = newTeamVM
            )
        }
    }
}