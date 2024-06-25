package com.teamon.app.board.project

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.prefs
import com.teamon.app.profileViewModel
import com.teamon.app.projectsViewModel
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.graphics.ProjectCardDropdownMenu
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.asDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun ProjectCard(
    actions: Actions,
    project: Project,
    snackbarHostState: SnackbarHostState = SnackbarHostState()
) {

    val overdue by projectsViewModel.getProjectOverdueTasks(project.projectId)
        .collectAsState(initial = emptyMap())
    val overdueSize = overdue.size
    val completedTasks by projectsViewModel.getProjectCompletedTasks(project.projectId)
        .collectAsState(initial = emptyMap())
    val tasks by projectsViewModel.getProjectTasks(project.projectId)
        .collectAsState(initial = emptyMap())
    var percentage by rememberSaveable { mutableStateOf(false) }
    val animate = prefs.getBoolean("animate", true)
    var progressAnimation by remember { mutableStateOf(percentage) }
    var initialAnimation by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(2000)
        initialAnimation = false
    }

    val progressValue = (completedTasks.size.toFloat() / tasks.size.toFloat()).takeIf { tasks.size.toFloat() != 0f } ?: 0f
    val progress by animateFloatAsState(
        targetValue =
            if (progressAnimation || initialAnimation) progressValue
            else 0f,
        label = "Progress value animation",
        animationSpec = if(animate) tween(durationMillis = 1000) else snap())


    val teams by projectsViewModel.getProjectTeams(project.projectId)
        .collectAsState(initial = emptyMap())

    var showingSnackbar by rememberSaveable {
        mutableStateOf(false)
    }


    var expanded by rememberSaveable { mutableStateOf(false) }
    val animation = rememberInfiniteTransition(label = "animation").animateColor(
        initialValue = MaterialTheme.colorScheme.error,
        targetValue = MaterialTheme.colorScheme.error.copy(alpha = 0f),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "animation"
    )
    var alreadyShowed by rememberSaveable { mutableStateOf(false) }

    val onProjectDelete = { projectId: String ->
        CoroutineScope(Dispatchers.IO).launch {
            if(projectsViewModel.deleteProject(projectId))
                snackbarHostState.showSnackbar(project.projectName + " has been deleted.")
            else
                snackbarHostState.showSnackbar("An error occurred while deleting " + project.projectName + ". Please try again.")
        }
    }

    Theme(color = project.projectColor, applyToStatusBar = false) {

        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .height(240.dp)
                .padding(10.dp)
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            actions.openProjectInfo(project.projectId)
                        },
                        onLongPress = {
                            expanded = true
                        }
                    )
                },
            onClick = { actions.openProjectInfo(project.projectId) },

            ) {

            Column(modifier = Modifier.padding(15.dp)) {
                Row(
                    modifier = Modifier.weight(3f),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(modifier = Modifier.weight(3f)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .fillMaxSize()
                                        .padding(10.dp), contentAlignment = Alignment.Center
                                ) {
                                    if(animate)
                                        AnimatedContent(
                                        modifier = Modifier,
                                        contentAlignment = Alignment.Center,
                                        targetState = percentage, label = "Percentage animation"
                                    ) {
                                        FilledTonalIconButton(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .align(Alignment.Center),
                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary,
                                            ),
                                            onClick = {
                                                percentage = !percentage; progressAnimation =
                                                percentage
                                            }) {
                                            if (it) {
                                                if (progress == 1f)
                                                    Icon(
                                                        painter = painterResource(R.drawable.round_check_circle_outline_24),
                                                        contentDescription = "Project completed"
                                                    )
                                                else
                                                    Text(
                                                        text = (progress * 100).toInt()
                                                            .toString() + "%",
                                                        color = MaterialTheme.colorScheme.primary,
                                                        style = MaterialTheme.typography.labelMedium,
                                                    )
                                            } else Icon(
                                                project.projectImage.toImage(),
                                                contentDescription = null
                                            )
                                        }

                                    }
                                    else {
                                        FilledTonalIconButton(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .align(Alignment.Center),
                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary,
                                            ),
                                            onClick = {
                                                percentage = !percentage; progressAnimation =
                                                percentage
                                            }) {
                                            if(percentage)
                                                if (progress == 1f)
                                                    Icon(
                                                        painter = painterResource(R.drawable.round_check_circle_outline_24),
                                                        contentDescription = "Project completed"
                                                    )
                                                else
                                                    Text(
                                                        text = (progress * 100).toInt()
                                                            .toString() + "%",
                                                        color = MaterialTheme.colorScheme.primary,
                                                        style = MaterialTheme.typography.labelMedium,
                                                    )
                                             else Icon(
                                                project.projectImage.toImage(),
                                                contentDescription = null
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .align(Alignment.Center),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .align(Alignment.Center),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.secondaryContainer,
                                            progress = {
                                                    if (initialAnimation) progress
                                                    else
                                                        if (percentage) progress
                                                        else progressValue
                                            }
                                        )
                                    }

                                }

                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            Column(
                                modifier = Modifier
                                    .weight(2.7f)
                                    .fillMaxWidth(), verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = project.projectName,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "End Date: ",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = tasks.values.maxByOrNull { it.endDate }?.endDate?.asDate()
                                            ?: "No tasks",
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                    }
                    Column(
                        modifier = Modifier.weight(1.3f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceAround) {

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (overdue.isNotEmpty()) {
                                    IconButton(
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        onClick = {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                alreadyShowed = true
                                                snackbarHostState.showSnackbar(project.projectName + " has " + if (overdueSize == 1) "an expired task." else "$overdueSize expired tasks.")
                                                delay(3000)
                                                showingSnackbar = false

                                            }
                                        }) {
                                        Image(
                                            modifier = Modifier.size(22.dp),
                                            colorFilter = ColorFilter.tint(if (!alreadyShowed && animate) animation.value else MaterialTheme.colorScheme.error),
                                            painter = painterResource(R.drawable.round_error_24),
                                            contentDescription = "Project with overdue task icon"
                                        )
                                    }
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                if(animate)
                                AnimatedContent(
                                    targetState = !profileViewModel.isFavorite(projectId = project.projectId),
                                    contentAlignment = Alignment.Center,
                                    label = "Favorite animation"
                                ) {

                                    IconButton(
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                        onClick = { profileViewModel.setFavorite(projectId = project.projectId) }) {
                                        Icon(
                                            if (it) Icons.Rounded.FavoriteBorder else Icons.Filled.Favorite,
                                            contentDescription = null
                                        )
                                    }
                                }
                                else
                                    IconButton(
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                        onClick = { profileViewModel.setFavorite(projectId = project.projectId) }) {
                                        Icon(
                                            if (profileViewModel.isFavorite(projectId = project.projectId)) Icons.Rounded.FavoriteBorder else Icons.Filled.Favorite,
                                            contentDescription = null
                                        )
                                    }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                IconButton(
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                    onClick = {
                                        expanded = !expanded
                                    }) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = null)
                                }
                                ProjectCardDropdownMenu(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it },
                                    projectId = project.projectId,
                                    onProjectDelete = { projectId ->
                                        onProjectDelete(projectId); expanded = false
                                    }
                                )
                            }
                        }

                    }
                }
                Row(
                    modifier = Modifier
                        .weight(3f)
                        .fillMaxWidth()
                        .padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.description,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                }
                Row(
                    modifier = Modifier
                        .weight(3f)
                        .padding(10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(2f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {

                        Row(
                            modifier = Modifier.clickable { actions.openProjectTeams(project.projectId) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val size = 2
                            teams.values.sortedBy { it.name }.take(size).forEach { team ->
                                Spacer(modifier = Modifier.width(size.dp))
                                TeamOnImage(
                                    modifier = Modifier.size(24.dp).clip(CircleShape),
                                    source = team.imageSource,
                                    name = team.name,
                                    color = team.color,
                                    surname = "",
                                    uri = team.image.toUri(),
                                    description = team.name + " profile image",
                                )
                            }
                            if (teams.size > size) {
                                Spacer(modifier = Modifier.width(size.dp))
                                Text(
                                    text = "+" + (teams.size - size).toString(),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp
                                )
                            }

                        }

                    }
                    Column(
                        modifier = Modifier.weight(3f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ElevatedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { actions.openProjectPerformance(project.projectId) },
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 6.dp
                                )
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.baseline_query_stats_24),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                    contentDescription = null
                                )
                            }
                            Spacer(modifier = Modifier.weight(0.1f))
                            Button(
                                modifier = Modifier.weight(1f),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 6.dp
                                ), onClick = { actions.openProjectTasks(project.projectId) }) {
                                Image(
                                    painter = painterResource(R.drawable.round_calendar_today_24),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                                    contentDescription = null
                                )
                            }
                        }
                    }


                }
            }
        }


    }


}
