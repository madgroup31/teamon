@file:Suppress("KotlinConstantConditions")

package com.teamon.app.board.project.teams


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.myteams.AddTeamCard
import com.teamon.app.myteams.TeamCard
import com.teamon.app.teamsViewModel
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.graphics.AnimatedGrid
import com.teamon.app.utils.graphics.SearchBar
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.TeamsFilteringOptionsDropdownMenu
import com.teamon.app.utils.graphics.TeamsSortingOption
import com.teamon.app.utils.graphics.TeamsSortingOptionsDropdownMenu
import com.teamon.app.utils.graphics.TeamsViewDropdownMenu
import com.teamon.app.utils.graphics.prepare
import com.teamon.app.utils.viewmodels.ProjectViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun TeamsActions(
    mainExpanded: Boolean,
    onMainExpandedChange: (Boolean) -> Unit,
    sortExpanded: Boolean,
    onSortExpandedChange: (Boolean) -> Unit,
    filterExpanded: Boolean,
    onFilterExpandedChange: (Boolean) -> Unit,
    sortingOrder: Boolean,
    onSortingOrderChange: (Boolean) -> Unit,
    sortingOption: String,
    onSortingOptionChange: (TeamsSortingOption) -> Unit,
    memberQuery: String,
    onMemberQueryChange: (String) -> Unit,
    categoryQuery: String,
    onCategoryQueryChange: (String) -> Unit,
    adminQuery: String,
    onAdminQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
) {

    IconButton(onClick = { onSearchActiveChange(true) }) {
        Icon(Icons.Rounded.Search, contentDescription = "Search tasks")
    }
    IconButton(onClick = { onMainExpandedChange(!mainExpanded) }) {
        Icon(Icons.Rounded.MoreVert, contentDescription = "More tasks options")
    }

    TeamsViewDropdownMenu(
        filterBadge = categoryQuery.isNotBlank() || memberQuery.isNotBlank() || adminQuery.isNotBlank(),
        mainExpanded = mainExpanded,
        onMainExpandedChange = onMainExpandedChange,
        onSortExpandedChange = onSortExpandedChange,
        onFilterExpandedChange = onFilterExpandedChange
    )

    TeamsSortingOptionsDropdownMenu(
        sortExpanded = sortExpanded,
        sortingOrder = sortingOrder,
        onSortingOrderChange = onSortingOrderChange,
        onSortExpandedChange = onSortExpandedChange,
        onMainExpandedChange = onMainExpandedChange,
        sortingOption = sortingOption,
        onSortingOptionChange = onSortingOptionChange
    )

    TeamsFilteringOptionsDropdownMenu(
        filterExpanded = filterExpanded,
        onFilterExpandedChange = onFilterExpandedChange,
        categoryQuery = categoryQuery,
        onCategoryQueryChange = onCategoryQueryChange,
        memberQuery = memberQuery,
        onMemberQueryChange = onMemberQueryChange,
        adminQuery = adminQuery,
        onAdminQueryChange = onAdminQueryChange
    )

}

@Composable
fun PortraitTeamsView(
    actions: Actions,
    projectVM: ProjectViewModel,
    snackbarHostState: SnackbarHostState,
    teamSortingOrder: Boolean,
    teamSortingOption: String,
    teamMemberQuery: String,
    teamCategoryQuery: String,
    teamAdminQuery: String,
    teamQuery: String,
    onTeamQueryChange: (String) -> Unit,
    searchActive: Boolean,
    onTeamSearchActiveChange: (Boolean) -> Unit,
    isAddingTeam: Boolean,
    onAddTeamClick: () -> Unit
) {

    val data by projectVM.getProjectTeams().collectAsState(initial = emptyMap())

    if (searchActive) {
        val teams = data.values.toList().prepare(
            sortingOrder = teamSortingOrder,
            sortingOption = teamSortingOption,
            memberQuery = teamMemberQuery,
            categoryQuery = teamCategoryQuery,
            adminQuery = teamAdminQuery,
            query = teamQuery,
        )
        SearchBar(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            query = teamQuery,
            placeholder = "Search Teams...",
            onQueryChange = onTeamQueryChange,
            searchActive = searchActive,
            onSearchActiveChange = onTeamSearchActiveChange
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
                    columns = StaggeredGridCells.FixedSize(150.dp),
                    items = teams
                ) { it, _ ->
                    TeamCard(team = it as Team, actions = actions)
                }
            }
        }
    } else {
        val teams = data.values.toList().prepare(
            sortingOrder = teamSortingOrder,
            sortingOption = teamSortingOption,
            memberQuery = teamMemberQuery,
            categoryQuery = teamCategoryQuery,
            adminQuery = teamAdminQuery,
            query = ""
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
            val displayItems = if (projectVM.isEditingTeams) {
                listOf("AddTeam") + teams
            } else {
                teams
            }
            AnimatedGrid(
                modifier = Modifier.fillMaxSize(),
                columns = StaggeredGridCells.FixedSize(185.dp),
                items = displayItems
            ) { it, _ ->
                if (it is String) {
                    AddTeamCard(
                        onAddTeamClick = {
                            onAddTeamClick()
                        }
                    )
                } else {
                    TeamCard(
                        team = it as Team,
                        actions = actions,
                        isEditing = projectVM.isEditingTeams && data.size > 1,
                        onRemoveTeamClick = if (projectVM.isEditingTeams) {
                            { teamId ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    if (projectVM.removeTeam(teamId)) {
                                        snackbarHostState.showSnackbar("Team removed successfully")
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to remove team")
                                    }
                                }
                            }

                        } else
                            null
                    )
                }
            }
            if (isAddingTeam) {
                AddTeamDialog(
                    onDismissRequest = { onAddTeamClick() },
                    selectedTeams = projectVM.teams.map { it.teamId },
                    onSelected = {
                            teamId ->
                        CoroutineScope(Dispatchers.IO).launch {
                            if (projectVM.addTeam(teamId)) {
                                onAddTeamClick()
                                snackbarHostState.showSnackbar("Team added successfully")
                            } else {
                                snackbarHostState.showSnackbar("Failed to add team")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LandscapeTeamsView(
    actions: Actions,
    projectVM: ProjectViewModel,
    snackbarHostState: SnackbarHostState,
    teamSortingOrder: Boolean,
    teamSortingOption: String,
    teamMemberQuery: String,
    teamCategoryQuery: String,
    teamAdminQuery: String,
    teamQuery: String,
    onTeamQueryChange: (String) -> Unit,
    searchActive: Boolean,
    onTeamSearchActiveChange: (Boolean) -> Unit,
    isAddingTeam: Boolean,
    onAddTeamClick: () -> Unit
) {
    val data = projectVM.teams.toList()


    if (searchActive) {
        val teams = data.prepare(
            sortingOrder = teamSortingOrder,
            sortingOption = teamSortingOption,
            memberQuery = teamMemberQuery,
            categoryQuery = teamCategoryQuery,
            adminQuery = teamAdminQuery,
            query = teamQuery,
        )
        SearchBar(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            query = teamQuery,
            placeholder = "Search Teams...",
            onQueryChange = onTeamQueryChange,
            searchActive = searchActive,
            onSearchActiveChange = onTeamSearchActiveChange
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
                    columns = StaggeredGridCells.FixedSize(185.dp),
                    items = teams
                ) { it, _ ->
                    TeamCard(team = it as Team, actions = actions)
                }
            }
        }
    } else {
        val teams = data.prepare(
            sortingOrder = teamSortingOrder,
            sortingOption = teamSortingOption,
            memberQuery = teamMemberQuery,
            categoryQuery = teamCategoryQuery,
            adminQuery = teamAdminQuery,
            query = ""
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
            val displayItems = if (projectVM.isEditingTeams) {
                listOf("AddTeam") + teams
            } else {
                teams
            }
            AnimatedGrid(
                modifier = Modifier.fillMaxSize(),
                columns = StaggeredGridCells.FixedSize(185.dp),
                items = displayItems
            ) { it, _ ->
                if (it is String) {
                    AddTeamCard(
                        onAddTeamClick = {
                            onAddTeamClick()
                        }
                    )
                } else {
                    TeamCard(
                        team = it as Team,
                        actions = actions,
                        isEditing = projectVM.isEditingTeams && data.size > 1,
                        onRemoveTeamClick = if (projectVM.isEditingTeams) {
                            { teamId ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    if (projectVM.removeTeam(teamId)) {
                                        snackbarHostState.showSnackbar("Team removed successfully")
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to remove team")
                                    }
                                }
                            }

                        } else
                            null
                    )
                }
            }
            if (isAddingTeam) {
                AddTeamDialog(
                    onDismissRequest = { onAddTeamClick() },
                    selectedTeams = projectVM.teams.map { it.teamId },
                    onSelected = {
                            teamId ->
                        CoroutineScope(Dispatchers.IO).launch {
                            if (projectVM.addTeam(teamId)) {
                                onAddTeamClick()
                                snackbarHostState.showSnackbar("Team added successfully")
                            } else {
                                snackbarHostState.showSnackbar("Failed to add team")
                            }
                        }
                    }
                )
            }
        }
    }

}


@Composable
fun TeamOption(
    team: Team,
    onSelected: (String) -> Unit
) {
    ListItem(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
            .clickable { onSelected(team.teamId) },
        headlineContent = { Text(team.name) },
        supportingContent = { Text(team.description) },
        leadingContent = {
            TeamOnImage(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                uri = team.image.toUri(),
                name = team.name,
                description = team.name + " profile image",
                color = team.color,
                source = team.imageSource
            )
        },
    )
}

@Composable
fun AddTeamDialog(
    selectedTeams: List<String>,
    onSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val teams by teamsViewModel.getTeams().collectAsState(initial = emptyMap())

    Dialog(onDismissRequest = { onDismissRequest() }) {
        OutlinedCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp, bottom = 30.dp)
        ) {
            LazyColumn {
                item {
                    Row {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Select Team to add to the Project",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                val selectableTeams = teams.values
                    .filter{!selectedTeams.contains(it.teamId)}

                if (selectableTeams.isNotEmpty()) {
                    selectableTeams
                        .forEach {
                            item {
                                TeamOption(
                                    team = it,
                                    onSelected = { teamId ->
                                        onSelected(teamId)
                                    }
                                )
                            }
                        }
                } else {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 20.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text("No teams available")
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        ElevatedButton(onClick = { onDismissRequest() }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

}
