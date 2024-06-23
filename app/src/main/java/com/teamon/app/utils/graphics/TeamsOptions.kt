package com.teamon.app.utils.graphics

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.teamon.app.R
import com.teamon.app.projectsViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Team
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun TeamsViewDropdownMenu(
    mainExpanded: Boolean,
    onMainExpandedChange: (Boolean) -> Unit,
    onSortExpandedChange: (Boolean) -> Unit,
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

fun List<Team>.prepare(
    sortingOrder: Boolean,
    sortingOption: String,
    categoryQuery: String,
    memberQuery: String,
    adminQuery: String,
    query: String
): List<Team> {
    var teams = this.asSequence()

    if (sortingOrder)
        teams = when (sortingOption) {
            TeamsSortingOption.TeamName.title -> teams.sortedByDescending { it.name.replaceFirstChar { char ->
                char.lowercase(
                    Locale.getDefault()
                )
            } }
            TeamsSortingOption.Members.title -> teams.sortedByDescending { it.users.size }
            TeamsSortingOption.CreationDate.title -> teams.sortedByDescending { it.creationDate }
            TeamsSortingOption.Category.title -> teams.sortedByDescending { it.category.replaceFirstChar { char ->
                char.lowercase(
                    Locale.getDefault()
                )
            } }
            TeamsSortingOption.CompletedTasks.title -> teams.sortedByDescending { team ->
                var completed by mutableIntStateOf(0)
                CoroutineScope(Dispatchers.IO).launch {
                    projectsViewModel.getTeamProjects(team.teamId).collect {projects ->
                        completed = 0
                        projects.map { project ->
                            launch {
                                projectsViewModel.getProjectCompletedTasks(project.projectId).collect {
                                    completed += it.size
                                }
                            }
                        }
                    }
                }
                completed
            }
            else -> { teams }
        }
    else
        teams = when (sortingOption) {
            TeamsSortingOption.TeamName.title -> teams.sortedBy { it.name.replaceFirstChar { char ->
                char.lowercase(
                    Locale.getDefault()
                )
            } }
            TeamsSortingOption.Members.title -> teams.sortedBy { it.users.size }
            TeamsSortingOption.CreationDate.title -> teams.sortedBy { it.creationDate }
            TeamsSortingOption.Category.title -> teams.sortedBy { it.category.replaceFirstChar { char ->
                char.lowercase(
                    Locale.getDefault()
                )
            } }
            TeamsSortingOption.CompletedTasks.title -> teams.sortedBy { team ->
                var completed by mutableIntStateOf(0)
                CoroutineScope(Dispatchers.IO).launch {
                    projectsViewModel.getTeamProjects(team.teamId).collect {projects ->
                        completed = 0
                        projects.map { project ->
                            launch {
                                projectsViewModel.getProjectCompletedTasks(project.projectId).collect {
                                    completed += it.size
                                }
                            }
                        }
                    }
                }
                completed
            }
            else -> { teams }
        }


    if(query.isNotBlank()) teams = teams.filter { Regex(query, RegexOption.IGNORE_CASE).containsMatchIn(it.name) }
    if(categoryQuery.isNotBlank()) teams = teams.filter { Regex(categoryQuery, RegexOption.IGNORE_CASE).containsMatchIn(it.category) }
    if(memberQuery.isNotBlank()) teams = teams.filter {
        it.users.any { userId ->
            val user = usersViewModel.users.value[userId]!!
            Regex(memberQuery.replace(" ", ""), RegexOption.IGNORE_CASE).containsMatchIn("${user.name}${user.surname}") ||
                    Regex(memberQuery.replace(" ", ""), RegexOption.IGNORE_CASE).containsMatchIn("${user.surname}${user.name}")
        }
    }
    if(adminQuery.isNotBlank()) teams = teams.filter {
        it.admin.any { userId ->
            val user = usersViewModel.users.value[userId]!!
            Regex(adminQuery.replace(" ", ""), RegexOption.IGNORE_CASE).containsMatchIn("${user.name}${user.surname}") ||
                    Regex(adminQuery.replace(" ", ""), RegexOption.IGNORE_CASE).containsMatchIn("${user.surname}${user.name}")
        }
    }

    return teams.toList()
}
