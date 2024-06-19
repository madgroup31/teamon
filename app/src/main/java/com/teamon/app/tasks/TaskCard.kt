package com.teamon.app.tasks

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import com.google.firebase.Timestamp
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.prefs
import com.teamon.app.profileViewModel
import com.teamon.app.projectsViewModel
import com.teamon.app.tasksViewModel
import com.teamon.app.teamOnViewModel
import com.teamon.app.teamsViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.TaskCardDropdownMenu
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.asCompactFutureRelativeDate
import com.teamon.app.utils.graphics.asDate
import com.teamon.app.utils.graphics.asFutureRelativeDate
import com.teamon.app.utils.graphics.currentTimeSeconds
import com.teamon.app.utils.graphics.toTimestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class TaskPriority {
    Low, Medium, High;

    fun toFloat(): Float {
        return when (this) {
            Low -> 0.0f
            Medium -> 1.0f
            High -> 2.0f
        }
    }

    companion object {
        fun from(it: Float): TaskPriority {
            return when (it) {
                0.0f -> Low
                1.0f -> Medium
                2.0f -> High
                else -> throw IllegalArgumentException("Bad priority value")
            }
        }
    }
}

enum class TaskStatus { Completed, Progress, Pending, Hold, Overdue }
enum class RecurringType { Fixed, Recursive }

enum class Repeat { Daily, Weekly, Monthly, Yearly }


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun TaskCard(
    orientation: Orientation,
    taskId: String,
    actions: Actions,
    setView: ((Boolean) -> Unit)? = null,
    snackbarHostState: SnackbarHostState,
    onTaskDelete: (String) -> Unit,
) {
    val project by tasksViewModel.getTaskProject(taskId).collectAsState(initial = Project())
    val task by tasksViewModel.getTask(taskId).collectAsState(initial = Task())
    val admins by tasksViewModel.getTaskProjectAdmins(taskId).collectAsState(initial = emptyMap())

    val creationDate = task.creationDate.asDate().toTimestamp().toInstant().epochSecond
    val endDate = task.endDate.asDate().toTimestamp().toInstant().epochSecond
    val now = Timestamp.now().toInstant().epochSecond
    val progressValue =
        ((now - creationDate).toFloat() / (endDate - creationDate).toFloat()).takeIf { (endDate - creationDate).toFloat() != 0f }
            ?: 0f

    val animate = prefs.getBoolean("animate", true)


    val initialAnimation by remember { mutableStateOf(true) }
    val progress by animateFloatAsState(
        targetValue = if (initialAnimation) progressValue else 0f,
        label = "Progress value animation",
        animationSpec = if (animate) tween(durationMillis = 1000) else snap()
    )


    var alreadyShowed by rememberSaveable {
        mutableStateOf(false)
    }
    val animation = rememberInfiniteTransition(label = "animation").animateColor(
        initialValue = MaterialTheme.colorScheme.error,
        targetValue = MaterialTheme.colorScheme.error.copy(alpha = 0f),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "animation"
    )

    Theme(color = project.projectColor, applyToStatusBar = false) {
        Box(
            modifier = Modifier
                .zIndex(1f)
                .height(155.dp)
                .padding(10.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
        ) {
            val overdue = task.endDate < Timestamp.now() && task.status != TaskStatus.Completed

            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                shape = RoundedCornerShape(20.dp),
                onClick = {
                    if (setView == null) {
                        val selectedNavItem =
                            actions.navCont.currentBackStackEntry?.destination?.route?.split("/")
                                ?.first().toString()
                        actions.openTask(selectedNavItem, taskId)
                    } else setView(true)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(20.dp)
                    )

            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(15.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.5f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    project.projectImage.toImage(),
                                    modifier = Modifier.size(18.dp),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = project.projectName,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.weight(1.5f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {

                                Text(
                                    text = "Priority: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = task.priority.toString(),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = when (task.priority.toString()) {
                                        "High" -> MaterialTheme.typography.titleSmall
                                        "Low" -> MaterialTheme.typography.bodySmall
                                        else -> MaterialTheme.typography.bodyMedium
                                    },
                                    fontWeight = when (task.priority.toString()) {
                                        "High" -> FontWeight.Bold
                                        "Low" -> FontWeight.Light
                                        else -> FontWeight.Normal
                                    }
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {


                                    if (true) {
                                        IconButton(
                                            modifier = Modifier.size(24.dp),
                                            colors = IconButtonDefaults.iconButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                            ),
                                            onClick = {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    snackbarHostState.showSnackbar("This task is expired!")
                                                    alreadyShowed = true
                                                }
                                            }) {
                                            Image(
                                                modifier = Modifier.size(22.dp),
                                                colorFilter = ColorFilter.tint(if (!alreadyShowed && animate) animation.value else MaterialTheme.colorScheme.error),
                                                painter = painterResource(R.drawable.round_error_24),
                                                contentDescription = "Overdue task icon"
                                            )
                                        }
                                    }



                                    if (task.recurringType == RecurringType.Recursive)
                                        IconButton(
                                            modifier = Modifier.size(24.dp),
                                            colors = IconButtonDefaults.iconButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                            ),
                                            onClick = {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    snackbarHostState.showSnackbar("This task is part of a recurring tasks set.")
                                                }
                                            }) {
                                            Image(
                                                modifier = Modifier.size(24.dp),
                                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                                painter = painterResource(R.drawable.baseline_loop_24),
                                                contentDescription = "Recursive task icon"
                                            )
                                        }



                                    if (task.listUser.contains(profileViewModel.userId) || admins.contains(
                                            profileViewModel.userId
                                        )
                                    ) {
                                        var expanded by remember { mutableStateOf(false) }
                                        IconButton(
                                            modifier = Modifier.size(24.dp),
                                            colors = IconButtonDefaults.iconButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                            ),
                                            onClick = { expanded = !expanded }) {
                                            Icon(
                                                Icons.Rounded.MoreVert,
                                                contentDescription = "Open task dialog"
                                            )
                                        }
                                        val selectedNavItem =
                                            actions.navCont.currentBackStackEntry?.destination?.route?.split(
                                                "/"
                                            )?.first().toString()
                                        TaskCardDropdownMenu(
                                            selectedTabItem = selectedNavItem,
                                            expanded = expanded,
                                            onExpandedChange = { expanded = it },
                                            actions = actions,
                                            taskId = taskId,
                                            projectId = project.projectId,
                                            admins = admins.keys.toList(),
                                            onTaskDelete = { taskId ->
                                                //onTaskDelete(project.projectId,taskId);
                                                expanded = false
                                            }
                                        )
                                }

                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(0.3f))
                    Row(
                        modifier = Modifier.weight(2f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.taskName,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.weight(0.5f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(2f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(0.3f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.End
                        ) {
                            Image(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.End),
                                colorFilter = ColorFilter.tint(if (progress > 0.9f && task.status != TaskStatus.Completed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                                painter = painterResource(R.drawable.outline_not_started_24),
                                contentDescription = "Creation date"
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1.5f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {

                            Slider(modifier = Modifier.fillMaxWidth(),
                                value = progress,
                                onValueChange = {},
                                colors = SliderDefaults.colors(activeTrackColor = if (progress > 0.9f && task.status != TaskStatus.Completed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                                thumb = {
                                    if (task.creationDate < Timestamp.now())
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .zIndex(200f)
                                                .offset(x = 0.dp, y = (-12).dp),
                                            contentAlignment = Alignment.TopEnd
                                        ) {
                                            val icon = when (task.status) {
                                                TaskStatus.Pending -> R.drawable.rounded_pending_24
                                                TaskStatus.Progress -> R.drawable.outline_pending_24
                                                TaskStatus.Hold -> R.drawable.round_on_hold_24
                                                TaskStatus.Completed -> R.drawable.round_check_circle_outline_24
                                                TaskStatus.Overdue -> R.drawable.round_error_outline_24
                                            }
                                            Image(
                                                modifier = Modifier.size(24.dp),
                                                painter = painterResource(icon),
                                                colorFilter = ColorFilter.tint(if (progress > 0.9f && task.status != TaskStatus.Completed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                                                contentDescription = null
                                            )
                                        }

                                })


                        }
                        Column(
                            modifier = Modifier.weight(0.3f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.End
                        ) {
                            Image(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.End),
                                colorFilter = ColorFilter.tint(if (progress > 0.9f && task.status != TaskStatus.Completed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                                painter = painterResource(R.drawable.round_outlined_flag_24),
                                contentDescription = "Deadline"
                            )
                        }

                        Spacer(modifier = Modifier.weight(0.1f))

                        Column(modifier = Modifier.weight(1.5f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    task.endDate.asCompactFutureRelativeDate(),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.weight(0.1f))
                                Column(
                                    modifier = Modifier.weight(1.2f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clickable {},
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (task.listUser.contains(profileViewModel!!.userId)) {
                                            TeamOnImage(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape),
                                                source = profileViewModel!!.profileImageSource,
                                                uri = profileViewModel!!.profileImageUri,
                                                name = profileViewModel!!.nameValue,
                                                surname = profileViewModel!!.surnameValue,
                                                color = profileViewModel.color,
                                                description = "Profile picture"
                                            )
                                        } else {
                                            val userId = task.listUser.firstOrNull()
                                            if (userId != null) {

                                                TeamOnImage(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape),
                                                    source = profileViewModel!!.profileImageSource,
                                                    uri = profileViewModel!!.profileImageUri,
                                                    name = profileViewModel!!.nameValue,
                                                    surname = profileViewModel!!.surnameValue,
                                                    color = profileViewModel.color,
                                                    description = "Profile picture"
                                                )
                                            } else {
                                                val userId = task.listUser.firstOrNull()
                                                if (userId != null) {
                                                    val user by usersViewModel!!.getUser(userId!!)
                                                        .collectAsState(initial = User())

                                                    TeamOnImage(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(CircleShape),
                                                        source = user.profileImageSource,
                                                        uri = user.profileImage?.toUri(),
                                                        name = user.name,
                                                        color = user.color,
                                                        surname = user.surname,
                                                        description = user.name + " " + user.surname + "profile picture"
                                                    )
                                                }
                                            }
                                        }
                                        if (task.listUser.size > 1) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "+" + (task.listUser.size - 1).toString(),
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                }
                            }
                        }
                    }

                }

            }
        }


    }

}
