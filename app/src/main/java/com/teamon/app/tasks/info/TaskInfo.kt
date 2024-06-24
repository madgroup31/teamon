package com.teamon.app.tasks.info

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import com.google.firebase.Timestamp
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.prefs
import com.teamon.app.profileViewModel
import com.teamon.app.tasks.RecurringType
import com.teamon.app.tasks.Repeat
import com.teamon.app.tasks.TaskPriority
import com.teamon.app.tasks.TaskStatus
import com.teamon.app.tasksViewModel
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.asFutureRelativeDate
import com.teamon.app.utils.graphics.asPastRelativeDate
import com.teamon.app.utils.graphics.convertMillisToDate
import com.teamon.app.utils.graphics.toTimestamp
import com.teamon.app.utils.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.log

@Composable
fun PartecipantCard(
    taskViewModel: TaskViewModel,
    user: User,
    taskAssignedTo: List<String>,
    isEditing: Boolean,
    actions: Actions
) {
    var assigned = false
    if (taskAssignedTo.contains(user.userId)) assigned = true

    val admins by tasksViewModel.getTaskProjectAdmins(taskViewModel.taskId)
        .collectAsState(initial = emptyMap())
    val isAdmin = admins.keys.contains(user.userId)


    var me = false
    if (user.userId == profileViewModel.userId) me = true
    val selectedNavItem =
        actions.navCont.currentBackStackEntry?.destination?.route?.split("/")?.first().toString()
    Row(
        modifier = Modifier
            .clickable { actions.openProfile(selectedNavItem, userId = user.userId) }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column(
            modifier = (if (assigned) Modifier.weight(2f)
            else Modifier
                .weight(2f)
                .graphicsLayer { alpha = 0.2f })
        )
        {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {

                    if (me) {
                        TeamOnImage(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape),
                            source = profileViewModel.profileImageSource,
                            uri = profileViewModel.profileImageUri,
                            name = profileViewModel.nameValue,
                            surname = profileViewModel.surnameValue,
                            color = profileViewModel.color,
                            description = "My profile picture"
                        )
                    } else {
                        TeamOnImage(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape),
                            source = user.profileImageSource,
                            uri = user.profileImage?.toUri(),
                            name = user.name,
                            surname = user.surname,
                            color = user.color,
                            description = user.name + " " + user.surname + " profile image"
                        )
                    }
                }
            }
        }

        Column(
            modifier = (if (assigned) Modifier.weight(4f)
            else Modifier
                .weight(4f)
                .graphicsLayer { alpha = 0.2f }),
            verticalArrangement = Arrangement.Center
        )
        {
            Row(modifier = Modifier)
            {

                Column {

                    if (isAdmin)
                        Row {
                            Text(
                                text = "Admin",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary

                            )
                            Image(modifier = Modifier.size(14.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                painter = painterResource(id = R.drawable.ic_admin),
                                contentDescription = "Admin badge")

                        }

                    Text(
                        text = if (me) "${user.name} ${user.surname} (Me)"
                        else "${user.name} ${user.surname}",
                        fontWeight = if (me) FontWeight.Medium else FontWeight.Normal

                    )
                }


            }
        }


        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (admins.contains(profileViewModel.userId) ) {
                if (isEditing) {
                    if (!assigned)
                        Button(
                            shape = MaterialTheme.shapes.medium,
                            onClick = { taskViewModel.updateListUser(user) }) {

                            Image(
                                painter = painterResource(id = R.drawable.round_person_add_alt_1_24),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surfaceContainerLowest),
                                contentDescription = "Add member"
                            )

                        }
                    else
                        FilledTonalButton(
                            shape = MaterialTheme.shapes.medium,
                            onClick = { taskViewModel.updateListUser(user) }) {

                            Image(
                                painter = painterResource(id = R.drawable.round_person_remove_24),
                                contentDescription = "Remove member"
                            )

                        }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskInfo(actions: Actions, taskViewModel: TaskViewModel) {

    var expandedPartecipants by remember { mutableStateOf(false) }
    var landscape by remember { mutableStateOf(false) }
    landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    var isStatusExpanded by remember { mutableStateOf(false) }
    var isRepeatExpanded by remember { mutableStateOf(false) }
    var datePickerDialog by remember { mutableIntStateOf(0) }
    val creationDate = taskViewModel.taskCreationDate.toTimestamp().toInstant().epochSecond
    val endDate = taskViewModel.taskEndDate.toTimestamp().toInstant().epochSecond
    val now = Timestamp.now().toInstant().epochSecond
    val progressValue = ((now - creationDate).toFloat() / (endDate - creationDate).toFloat()).takeIf{ (endDate - creationDate).toFloat() != 0f}?:0f
    val animate = prefs.getBoolean("animate", true)
    var animation by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animate && !progressValue.isNaN() && animation) progressValue else 0f,
        label = "Progress value animation",
        animationSpec = tween(durationMillis = 1000)
    )

    LazyColumn(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            AnimatedItem(index = 1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {

                    val deadline = taskViewModel.taskEndDate
                    val creation = taskViewModel.taskCreationDate

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(0.4f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(if (progress > 0.8f && taskViewModel.taskStatus != TaskStatus.Completed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                                painter = painterResource(R.drawable.outline_not_started_24),
                                contentDescription = "Creation date"
                            )
                            Text(
                                modifier = Modifier,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (progress > 0.8f && taskViewModel.taskStatus != TaskStatus.Completed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                text = if (creation.toTimestamp() < Timestamp.now()) creation.toTimestamp()
                                    .asPastRelativeDate() else creation.toTimestamp()
                                    .asFutureRelativeDate()
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1.5f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Slider(modifier = Modifier.fillMaxWidth(),
                                value = progress,
                                onValueChange = {},
                                colors = SliderDefaults.colors(activeTrackColor = if (progress > 0.8f && taskViewModel.taskStatus != TaskStatus.Completed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                                thumb = {
                                    if (creation.toTimestamp() < Timestamp.now())
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .zIndex(200f)
                                                .offset(x = 0.dp, y = (-14).dp),
                                            contentAlignment = Alignment.TopEnd
                                        ) {
                                            val icon = when (taskViewModel.taskStatus) {
                                                TaskStatus.Pending -> R.drawable.rounded_pending_24
                                                TaskStatus.Progress -> R.drawable.outline_pending_24
                                                TaskStatus.Hold -> R.drawable.round_on_hold_24
                                                TaskStatus.Completed -> R.drawable.round_check_circle_outline_24
                                                TaskStatus.Overdue -> R.drawable.round_error_outline_24
                                            }
                                            Image(
                                                modifier = Modifier.size(24.dp),
                                                painter = painterResource(icon),
                                                colorFilter = ColorFilter.tint(if (progress > 0.9f && taskViewModel.taskStatus != TaskStatus.Completed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                                                contentDescription = null
                                            )
                                        }

                                })
                            LaunchedEffect(Unit) {
                                animation = true
                            }
                        }
                        Column(
                            modifier = Modifier.weight(0.4f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(if (progress > 0.8f && taskViewModel.taskStatus != TaskStatus.Completed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                                painter = painterResource(R.drawable.round_outlined_flag_24),
                                contentDescription = "Deadline"
                            )
                            Text(
                                modifier = Modifier,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (progress > 0.8f && taskViewModel.taskStatus != TaskStatus.Completed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                text = deadline.toTimestamp().asFutureRelativeDate()
                            )
                        }
                    }

                }
            }
        }
        //TASK NAME
        item {
            AnimatedItem(index = 2) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        )
                        {
                            OutlinedTextField(
                                value = taskViewModel.taskName,
                                onValueChange = { taskViewModel.updateTaskName(it) },
                                readOnly = !taskViewModel.isEditing,
                                label = { Text("Task Name") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = taskViewModel.taskNameError.isNotBlank()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            if (taskViewModel.taskNameError.isNotBlank()) {
                                Text(
                                    taskViewModel.taskNameError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        )
                        {
                            OutlinedTextField(
                                value = taskViewModel.taskDescription,
                                onValueChange = { taskViewModel.updateTaskDescription(it) },
                                readOnly = !taskViewModel.isEditing,
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = taskViewModel.taskDescriptionError.isNotBlank()
                            )
                            if (taskViewModel.taskDescriptionError.isNotBlank()) {
                                Text(
                                    taskViewModel.taskDescriptionError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(3f),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        )
                        {
                            OutlinedTextField(
                                value = taskViewModel.taskEndDate,
                                onValueChange = { taskViewModel.updateTaskEndDate(it) },
                                enabled = !taskViewModel.isEditing || taskViewModel.taskStatus != TaskStatus.Overdue,
                                readOnly = true,
                                label = { Text("End Date") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = taskViewModel.taskEndDateError.isNotBlank(),
                                trailingIcon = {
                                    IconButton(enabled = taskViewModel.isEditing,
                                        onClick = { datePickerDialog = 1 }) {
                                        Image(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource(id = R.drawable.round_calendar_today_24),
                                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                            contentDescription = "Date picker"
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                            if (taskViewModel.taskEndDateError.isNotBlank()) {
                                Text(
                                    taskViewModel.taskEndDateError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Column(
                            modifier = Modifier
                                .weight(2f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        )
                        {
                            Text(
                                text = "Recurring type",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Icon(
                                painter = painterResource(if (taskViewModel.taskRecurringType == RecurringType.Recursive) R.drawable.baseline_loop_24 else R.drawable.round_1x_mobiledata_24),
                                contentDescription = "Repeat type",
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (taskViewModel.taskRecurringType == RecurringType.Recursive) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(7f)
                                    .padding(2.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                DropdownMenu(
                                    expanded = isRepeatExpanded,
                                    onDismissRequest = { isRepeatExpanded = false },
                                    modifier = Modifier
                                        .padding(8.dp)
                                ) {
                                    Repeat.entries.forEach { repeating ->
                                        DropdownMenuItem(
                                            text = { Text(text = repeating.toString()) },
                                            onClick = {
                                                isRepeatExpanded = false
                                                taskViewModel.updateTaskRepeat(repeating)
                                            }
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = taskViewModel.taskRepeat.toString(),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Repeat") },
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            if (taskViewModel.isEditing) {
                                                isRepeatExpanded = true
                                            }
                                        }) {
                                            Icon(
                                                if (!isRepeatExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                                                contentDescription = "Expand Repeat options"
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    isError = taskViewModel.taskRepeatError.isNotBlank()
                                )
                                if (taskViewModel.taskRepeatError.isNotBlank()) {
                                    Text(
                                        taskViewModel.taskRepeatError,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .weight(10f)
                                    .padding(2.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                OutlinedTextField(
                                    value = taskViewModel.taskEndRepeat,
                                    onValueChange = { taskViewModel.updateTaskEndRepeat(it) },
                                    readOnly = true,
                                    label = { Text("End Repeat") },
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = taskViewModel.taskEndRepeatError.isNotBlank(),
                                    trailingIcon = {
                                        IconButton(enabled = taskViewModel.isEditing,
                                            onClick = { datePickerDialog = 2 }) {
                                            Image(
                                                modifier = Modifier.size(24.dp),
                                                painter = painterResource(id = R.drawable.round_calendar_today_24),
                                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                                contentDescription = "Date picker"
                                            )
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                )
                                if (taskViewModel.taskEndRepeatError.isNotBlank()) {
                                    Text(
                                        taskViewModel.taskEndRepeatError,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (datePickerDialog > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                        ) {
                            if (datePickerDialog == 1) {
                                MyDatePickerDialog(
                                    onDateSelected = {
                                        taskViewModel.updateTaskEndDate(
                                            it.replace(
                                                "/",
                                                "-"
                                            )
                                        )
                                    },
                                    onDismiss = { datePickerDialog = 0 },
                                    actualDate = taskViewModel.taskEndDate
                                )
                            } else {
                                MyDatePickerDialog(
                                    onDateSelected = {
                                        taskViewModel.updateTaskEndRepeat(
                                            it.replace(
                                                "/",
                                                "-"
                                            )
                                        )
                                    },
                                    onDismiss = { datePickerDialog = 0 },
                                    actualDate = taskViewModel.taskEndRepeat
                                )
                            }
                        }

                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconStatus = when (taskViewModel.taskStatus) {
                            TaskStatus.Pending -> R.drawable.rounded_pending_24
                            TaskStatus.Progress -> R.drawable.outline_pending_24
                            TaskStatus.Hold -> R.drawable.round_on_hold_24
                            TaskStatus.Completed -> R.drawable.round_check_circle_outline_24
                            TaskStatus.Overdue -> R.drawable.round_error_outline_24
                        }

                        Column(
                            modifier = Modifier
                                .weight(10f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DropdownMenu(
                                expanded = isStatusExpanded,
                                onDismissRequest = { isStatusExpanded = false },
                                modifier = Modifier
                                    .padding(8.dp)
                            ) {
                                TaskStatus.entries.filter { it != TaskStatus.Overdue }
                                    .forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(text = status.toString()) },
                                            onClick = {
                                                //selectedStatus = status
                                                isStatusExpanded = false
                                                taskViewModel.updateTaskStatus(status)
                                            }
                                        )
                                    }
                            }

                            OutlinedTextField(
                                value = taskViewModel.taskStatus.toString(),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        if (taskViewModel.isEditing) isStatusExpanded = true
                                    }) {
                                        Icon(
                                            if (!isStatusExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                                            contentDescription = "Expand Repeat options"
                                        )
                                    }
                                },
                                label = { Text("Status") },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                leadingIcon = {
                                    Image(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource(id = iconStatus),
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                        contentDescription = "Status picker",

                                        )
                                })
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier
                                .weight(7f)
                                .padding(2.dp),
                            horizontalAlignment = Alignment.Start
                        ) {

                            Row(
                                modifier = Modifier.padding(top = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = "Priority: ",
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
                                )
                                Text(
                                    text = taskViewModel.taskPriority.toString(),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = when (taskViewModel.taskPriority.toString()) {
                                        "High" -> MaterialTheme.typography.titleSmall
                                        "Low" -> MaterialTheme.typography.bodySmall
                                        else -> MaterialTheme.typography.bodyMedium
                                    },
                                    fontWeight = when (taskViewModel.taskPriority.toString()) {
                                        "High" -> FontWeight.Bold
                                        "Low" -> FontWeight.Light
                                        else -> FontWeight.Normal
                                    }
                                )
                            }
                            Slider( //How to add title on top?
                                enabled = taskViewModel.isEditing,
                                value = when (taskViewModel.taskPriority) {
                                    TaskPriority.Low -> 0f
                                    TaskPriority.Medium -> 1f
                                    TaskPriority.High -> 2f
                                },
                                onValueChange = {
                                    when (it) {
                                        0f -> taskViewModel.updateTaskPriority(TaskPriority.Low)
                                        1f -> taskViewModel.updateTaskPriority(TaskPriority.Medium)
                                        2f -> taskViewModel.updateTaskPriority(TaskPriority.High)
                                    }
                                },
                                valueRange = 0f..2f,
                                steps = 1
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        )
                        {
                            OutlinedTextField(
                                value = taskViewModel.taskTag,
                                onValueChange = { taskViewModel.updateTaskTag(it) },
                                readOnly = !taskViewModel.isEditing,
                                label = { Text("Tag") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = taskViewModel.taskTagError.isNotBlank()
                            )
                            if (taskViewModel.taskTagError.isNotBlank()) {
                                Text(
                                    taskViewModel.taskTagError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Task Collaborators (" + taskViewModel.listUser.size + ") ",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge
                        )

                        IconButton(
                            onClick = { expandedPartecipants = !expandedPartecipants },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (expandedPartecipants) Icon(
                                imageVector = Icons.Rounded.KeyboardArrowUp,
                                contentDescription = "See less"
                            )
                            else Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "See all"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
        
        if (taskViewModel.projectMembers.isNotEmpty()) {
            val size = if (expandedPartecipants) taskViewModel.projectMembers.size
            else log(taskViewModel.listUser.size.toDouble(), 2.0).toInt()
            val members =
                taskViewModel.projectMembers.values.asSequence()
                    .sortedByDescending { it.userId in taskViewModel.listUser }
                    .take(size.takeIf { it > 0 } ?: 1)
                    .toSet().toList()

            itemsIndexed(members) { index, member ->
                AnimatedItem(index = index + 2) {
                    PartecipantCard(
                        taskViewModel = taskViewModel,
                        user = member,
                        taskAssignedTo = taskViewModel.listUser,
                        isEditing = taskViewModel.isEditing,
                        actions = actions
                    )
                }
            }
        }

    }

    if (taskViewModel.isConfirmDialogShow) {
        AlertDialog(
            modifier = Modifier.wrapContentSize(),
            onDismissRequest = { taskViewModel.toggleConfirmDialog() },
            title = { Text(text = "Confirm changes") },
            text = {
                Text(text = "Are you sure to save the changes ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        taskViewModel.validate()
                        taskViewModel.toggleConfirmDialog()
                    }
                ) {
                    Text(text = "Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        taskViewModel.toggleConfirmDialog()
                    }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    actualDate: String,
    firstSelectable: String? = null
) {
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {

        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            val firstSelectableDate = if (firstSelectable != null) {
                Date(dateFormat.parse(firstSelectable)!!.time)
            } else {
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).time
            }

            val date = Date(utcTimeMillis)
            return date.after(firstSelectableDate)
        }

    })

    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: actualDate

    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = {
                onDateSelected(selectedDate)
                onDismiss()
            }

            ) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            Button(onClick = {
                onDismiss()
            }) {
                Text(text = "Cancel")
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(enabled = true, state = rememberScrollState())
    ) {
        DatePicker(
            state = datePickerState
        )
    }
}