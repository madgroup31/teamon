package com.teamon.app.board.project.teams


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teamon.app.Actions
import com.teamon.app.utils.viewmodels.ProjectViewModel
import com.teamon.app.myteams.TeamCard
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.graphics.AnimatedGrid
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.SearchBar
import com.teamon.app.utils.graphics.TeamsFilteringOptionsDropdownMenu
import com.teamon.app.utils.graphics.TeamsSortingOption
import com.teamon.app.utils.graphics.TeamsSortingOptionsDropdownMenu
import com.teamon.app.utils.graphics.TeamsViewDropdownMenu
import com.teamon.app.utils.graphics.prepare


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
        mainExpanded = mainExpanded,
        onMainExpandedChange = onMainExpandedChange,
        sortExpanded = sortExpanded,
        onSortExpandedChange = onSortExpandedChange,
        filterExpanded = filterExpanded,
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
    teamSortingOrder: Boolean,
    teamSortingOption: String,
    teamMemberQuery: String,
    teamCategoryQuery: String,
    teamAdminQuery: String,
    teamQuery: String,
    onTeamQueryChange: (String) -> Unit,
    searchActive: Boolean,
    onTeamSearchActiveChange: (Boolean) -> Unit) {

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
                    AnimatedGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        items = listOf("No Available Teams.")
                    ) { it, index ->
                        Text(
                            text = it as String,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic
                        )
                    }
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
    else {
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
                AnimatedGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    items = listOf("No Available Teams.")
                ) { it, index ->
                    Text(
                        text = it as String,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic
                    )
                }
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

@Composable
fun LandscapeTeamsView(
    actions: Actions,
    projectVM: ProjectViewModel,
    teamSortingOrder: Boolean,
    teamSortingOption: String,
    teamMemberQuery: String,
    teamCategoryQuery: String,
    teamAdminQuery: String,
    teamQuery: String,
    onTeamQueryChange: (String) -> Unit,
    searchActive: Boolean,
    onTeamSearchActiveChange: (Boolean) -> Unit
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
                    AnimatedGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        items = listOf("No Available Teams.")
                    ) { it, index ->
                        Text(
                            text = it as String,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic
                        )
                    }
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
    else {
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
                AnimatedGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    items = listOf("No Available Teams.")
                ) { it, index ->
                    Text(
                        text = it as String,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic
                    )
                }
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
