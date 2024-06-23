package com.teamon.app.myteams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teamon.app.Actions
import com.teamon.app.board.project.ProjectCard
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.graphics.AnimatedGrid
import com.teamon.app.utils.viewmodels.TeamViewModel

@Composable
fun TeamProjects(actions: Actions, teamVM: TeamViewModel)
{
    val projects by teamVM.getProjectsTeam().collectAsState(initial= emptyList())

    val snackbarHostState = remember { SnackbarHostState() }

    if (projects.isEmpty())
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No Available Projects.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic
            )
        }
    else {
        AnimatedGrid(
            modifier = Modifier.fillMaxSize(),
            columns = StaggeredGridCells.Adaptive(250.dp),
            items = projects
        ) { it, _ ->
            ProjectCard(
                actions = actions,
                project = it as Project,
                snackbarHostState = snackbarHostState
            )
        }
    }

}