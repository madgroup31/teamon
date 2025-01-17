package com.teamon.app.board

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamon.app.Actions
import com.teamon.app.MessagingService
import com.teamon.app.R
import com.teamon.app.board.project.NewProjectBottomSheetContent
import com.teamon.app.board.project.ProjectCard
import com.teamon.app.profileViewModel
import com.teamon.app.projectsViewModel
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.graphics.AnimatedGrid
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.LoadingOverlay
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.viewmodels.Factory
import com.teamon.app.utils.viewmodels.NewProjectViewModel


@Composable
fun BoardView(actions: Actions) {
    MessagingService.initialize(LocalContext.current, LocalContext.current as Activity)

    Theme(color = profileViewModel.color, applyToStatusBar = true) {

        val newProjectViewModel =
            viewModel<NewProjectViewModel>(factory = Factory(LocalContext.current.applicationContext))

        val projects by projectsViewModel.getProjects().collectAsState(initial = null)

        var landscape by remember { mutableStateOf(false) }
        landscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (landscape) LandscapeView(
            actions = actions,
            newProjectVM = newProjectViewModel,
            data = projects?.values?.toList()
        )
        else PortraitView(
            actions = actions,
            newProjectVM = newProjectViewModel,
            data = projects?.values?.toList()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandscapeView(
    actions: Actions,
    newProjectVM: NewProjectViewModel,
    data: List<Project>?
) {



    val sheetState = rememberModalBottomSheetState()
    val snackbarHostState = remember { SnackbarHostState() }

    AppSurface(
        orientation = Orientation.LANDSCAPE,
        title = "Board",
        actions = actions,
        floatingActionButton = {
            FloatingActionButton(modifier = Modifier
                .padding(end = 10.dp),
                onClick = {
                    newProjectVM.toggleShow()
                }) {
                Image(
                    painter = painterResource(id = R.drawable.round_add_circle_outline_24),
                    contentDescription = "Create a project",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            }
        }
    ) {
        if(data == null)
            LoadingOverlay(isLoading = true)
        else
            if (data.isEmpty())
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
                    columns = StaggeredGridCells.FixedSize(400.dp),
                    items = data
                ) { project, _ ->
                    ProjectCard(
                        actions = actions,
                        project = project as Project,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
            if (newProjectVM.isShowing) {
                ModalBottomSheet(
                    modifier = Modifier,
                    onDismissRequest = {
                        newProjectVM.toggleShow()
                    },
                    sheetState = sheetState
                ) {
                    // Sheet content
                    NewProjectBottomSheetContent(newProjectVM = newProjectVM)
                }
            }

    }
}

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortraitView(
    actions: Actions,
    newProjectVM: NewProjectViewModel,
    data: List<Project>?
) {

    val sheetState = rememberModalBottomSheetState()
    val snackbarHostState = remember { SnackbarHostState() }


    AppSurface(
        orientation = Orientation.PORTRAIT,
        title = "Board",
        snackbarHostState = snackbarHostState,
        actions = actions,
        floatingActionButton = {
            FloatingActionButton(modifier = Modifier,
                onClick = {
                    newProjectVM.toggleShow()
                }) {
                Image(
                    painter = painterResource(id = R.drawable.round_add_circle_outline_24),
                    contentDescription = "Create a project",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            }
        }
    ) {
        if(data == null)
            LoadingOverlay(isLoading = true)
        else
        if (data.isEmpty())
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
                columns = StaggeredGridCells.FixedSize(400.dp),
                items = data.sortedByDescending {
                    profileViewModel.favoritesProjects.contains(
                        it.projectId
                    )
                }
            ) { project, _ ->
                ProjectCard(
                    actions = actions,
                    project = project as Project,
                    snackbarHostState = snackbarHostState
                )
            }
        }
        if (newProjectVM.isShowing) {
            ModalBottomSheet(
                modifier = Modifier,
                onDismissRequest = {
                    newProjectVM.toggleShow()
                },
                sheetState = sheetState
            ) {
                // Sheet content
                NewProjectBottomSheetContent(newProjectVM = newProjectVM)
            }
        }
    }
}


