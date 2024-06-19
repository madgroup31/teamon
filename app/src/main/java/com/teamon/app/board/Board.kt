package com.teamon.app.board

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
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
import com.teamon.app.MainActivity
import com.teamon.app.utils.viewmodels.Factory
import com.teamon.app.R
import com.teamon.app.board.project.NewProjectBottomSheetContent
import com.teamon.app.utils.viewmodels.NewProjectViewModel
import com.teamon.app.board.project.ProjectCard
import com.teamon.app.profileViewModel
import com.teamon.app.projectsViewModel
import com.teamon.app.teamOnViewModel
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.graphics.AnimatedGrid
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.themes.teamon.TeamOnTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter


@Composable
fun BoardView(actions: Actions) {
    Theme(color = profileViewModel.color, applyToStatusBar = true) {

        val newProjectViewModel =
            viewModel<NewProjectViewModel>(factory = Factory(LocalContext.current.applicationContext))

        var landscape by remember { mutableStateOf(false) }
        landscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (landscape) LandscapeView(
            actions = actions,
            newProjectVM = newProjectViewModel,
        )
        else PortraitView(
            actions = actions,
            newProjectVM = newProjectViewModel,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LandscapeView(
    actions: Actions,
    newProjectVM: NewProjectViewModel,
) {


    val projects by projectsViewModel!!.getProjects().collectAsState(initial = emptyMap())
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
        Row {
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
                    columns = GridCells.Adaptive(minSize = 350.dp),
                    items = projects.values
                ) { it, index ->
                    ProjectCard(
                        orientation = Orientation.PORTRAIT,
                        actions = actions,
                        project = it as Project,
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
}

    @SuppressLint("SuspiciousIndentation")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun PortraitView(
        actions: Actions,
        newProjectVM: NewProjectViewModel,
    ) {



        val projects by projectsViewModel.getProjects().collectAsState(initial = emptyMap())
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
                    columns = GridCells.Adaptive(minSize = 350.dp),
                    items = projects.values.sortedByDescending { profileViewModel!!.favoritesProjects.contains(it.projectId) }
                ) { it, _ ->
                    ProjectCard(
                        orientation = Orientation.PORTRAIT,
                        actions = actions,
                        project = it as Project,
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


