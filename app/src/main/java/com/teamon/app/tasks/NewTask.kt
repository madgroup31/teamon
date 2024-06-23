package com.teamon.app.tasks

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.teamon.app.R
import com.teamon.app.profileViewModel
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.convertMillisToDate
import com.teamon.app.utils.viewmodels.NewTaskViewModel
import com.teamon.app.utils.viewmodels.ProjectViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun NewTaskBottomSheetContent(
    newTaskVM: NewTaskViewModel,
    projectVM: ProjectViewModel?,
    snackbarHostState: SnackbarHostState
) {

    val scrollState = rememberScrollState()

    var unitOptions by remember { mutableStateOf(false) }
    var numberOptions by remember { mutableStateOf(false) }
    var projectsOptions by remember { mutableStateOf(false) }
    profileViewModel.userId
    val projects by newTaskVM.projects.collectAsState(initial = emptyMap())

    LaunchedEffect(newTaskVM.taskUploadingError) {
        if (newTaskVM.taskUploadingError) snackbarHostState.showSnackbar("An error occurred. Please try again.")
        newTaskVM.setUploadingError(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 40.dp)
            .verticalScroll(scrollState),
    ) {
        Row {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Create New Task",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontStyle = MaterialTheme.typography.titleLarge.fontStyle,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = false,
                    enabled = true,
                    value = newTaskVM.taskName,
                    onValueChange = { name -> newTaskVM.updateTaskName(name) },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.round_text_fields_24),
                            contentDescription = "New Task name"
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("Task Name") },
                    isError = newTaskVM.taskNameError.isNotBlank()
                )
                if (newTaskVM.taskNameError.isNotBlank()) {
                    Text(
                        newTaskVM.taskNameError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = false,
                    enabled = true,
                    value = newTaskVM.taskDescription,
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.round_text_fields_24),
                            contentDescription = "New Task name"
                        )
                    },
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("Description") },
                    onValueChange = { description -> newTaskVM.updateTaskDescription(description) },
                    isError = newTaskVM.taskDescriptionError.isNotBlank()
                )
                if (newTaskVM.taskDescriptionError.isNotBlank()) {
                    Text(
                        newTaskVM.taskDescriptionError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (projectVM == null) {
            if (projects.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = false,
                            enabled = true,
                            value = newTaskVM.projectName,
                            trailingIcon = {
                                IconButton(onClick = { projectsOptions = !projectsOptions }) {
                                    Icon(
                                        Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Project"
                                    )
                                }
                            },
                            singleLine = true,
                            label = { Text("Project Name") },
                            onValueChange = { },
                            isError = newTaskVM.taskDescriptionError.isNotBlank()
                        )

                        if (newTaskVM.taskDescriptionError.isNotBlank()) {
                            Text(
                                newTaskVM.taskDescriptionError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        DropdownMenu(
                            expanded = projectsOptions,
                            onDismissRequest = { projectsOptions = false }
                        ) {
                            projects.entries.sortedBy { it.key }.forEach {
                                DropdownMenuItem(
                                    text = { Text(text = it.value.projectName) },
                                    onClick = {
                                        newTaskVM.updateProjectName(it.value.projectName)
                                        newTaskVM.updateProjectMembers(it.key)
                                        projectsOptions = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, start = 0.dp, end = 0.dp, bottom = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    val iconStatus = when (newTaskVM.taskStatus) {
                        TaskStatus.Pending -> R.drawable.rounded_pending_24
                        TaskStatus.Progress -> R.drawable.outline_pending_24
                        TaskStatus.Hold -> R.drawable.round_on_hold_24
                        TaskStatus.Completed -> R.drawable.round_check_circle_outline_24
                        TaskStatus.Overdue -> R.drawable.round_error_outline_24
                    }
                    OutlinedTextField(
                        readOnly = true,
                        enabled = true,
                        value = newTaskVM.taskStatus.toString(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = iconStatus),
                                contentDescription = "Status picker",

                                )
                        },
                        trailingIcon = {
                            IconButton(enabled = true,
                                onClick = { newTaskVM.toggleEditingStatusOptions() }) {
                                Icon(
                                    if (newTaskVM.isEditingStatusOptions) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "Expand/contract repeat options"
                                )
                            }
                        },
                        label = { Text("Status") },
                        onValueChange = { },
                    )
                    DropdownMenu(
                        expanded = newTaskVM.isEditingStatusOptions,
                        onDismissRequest = { newTaskVM.toggleEditingStatusOptions() }
                    ) {
                        TaskStatus.entries.forEach {
                            DropdownMenuItem(
                                text = { Text(text = it.toString()) },
                                onClick = {
                                    newTaskVM.updateTaskStatus(it)
                                    newTaskVM.toggleEditingStatusOptions()
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(0.1f))
            Column(
                modifier = Modifier
                    .weight(0.8f)
            ) {

                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val typo = when (newTaskVM.taskPriority) {
                        TaskPriority.Low -> MaterialTheme.typography.labelSmall
                        TaskPriority.Medium -> MaterialTheme.typography.labelMedium
                        TaskPriority.High -> MaterialTheme.typography.labelLarge
                    }
                    Text(
                        text = "Priority: ",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
                    )
                    Text(
                        text = newTaskVM.taskPriority.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = typo.fontSize,
                        fontWeight = typo.fontWeight,
                        fontStyle = typo.fontStyle
                    )
                }
                Slider(
                    value = newTaskVM.taskPriority.toFloat(),
                    onValueChange = { newTaskVM.updateTaskPriority(TaskPriority.from(it)) },
                    valueRange = 0f..2f,
                    steps = 1
                )
            }
        }
        if (newTaskVM.taskRecurringType == RecurringType.Recursive) {
            Row(modifier = Modifier.padding(top = 10.dp)) {
                Column(modifier = Modifier.weight(0.8f)) {
                    Text(
                        "Lifetime",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
                    )
                }

                Column(
                    modifier = Modifier.weight(0.2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Recursive",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (newTaskVM.taskRecurringType == RecurringType.Recursive) 0.dp else 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier
                    .weight(0.8f)
            ) {
                if (newTaskVM.taskRecurringType != RecurringType.Recursive) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = true,
                            value = newTaskVM.taskEndDate,
                            singleLine = true,
                            trailingIcon = {
                                IconButton(enabled = true,
                                    onClick = { newTaskVM.toggleEditEndDate() }) {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource(id = R.drawable.round_calendar_today_24),
                                        contentDescription = "Date picker"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("End Date") },
                            onValueChange = { endDate -> newTaskVM.updateTaskEndDate(endDate) },
                            isError = newTaskVM.taskEndDateError.isNotBlank()
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {

                        Row(verticalAlignment = Alignment.Bottom) {
                            Column(
                                modifier = Modifier
                                    .weight(0.4f)
                            ) {
                                OutlinedTextField(
                                    value = newTaskVM.lifetimeUnit,
                                    readOnly = true,
                                    enabled = true,
                                    onValueChange = {},
                                    label = { },
                                    trailingIcon = {
                                        IconButton(enabled = true,
                                            onClick = { unitOptions = true }) {
                                            Icon(
                                                if (unitOptions) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                                contentDescription = "Expand/contract lifetime unit options"
                                            )
                                        }
                                    })

                                    DropdownMenu(
                                        expanded = unitOptions,
                                        onDismissRequest = { unitOptions = false }
                                    ) {
                                        DropdownMenuItem(onClick = {
                                            newTaskVM.updateLifetimeUnit("Days")
                                            newTaskVM.updateLifetimeNumbers("1")
                                            unitOptions = false
                                        }, text = { Text(text = "Days") })
                                        DropdownMenuItem(onClick = {
                                            newTaskVM.updateLifetimeUnit("Months")
                                            newTaskVM.updateLifetimeNumbers("1")
                                            unitOptions = false
                                        }, text = { Text(text = "Months") })
                                        DropdownMenuItem(onClick = {
                                            newTaskVM.updateLifetimeUnit("Years")
                                            newTaskVM.updateLifetimeNumbers("1")
                                            unitOptions = false
                                        }, text = { Text(text = "Years") })
                                    }

                            }
                            Spacer(modifier = Modifier.weight(0.05f))
                            Column(
                                modifier = Modifier
                                    .weight(0.3f)
                            ) {

                                OutlinedTextField(
                                    value = newTaskVM.lifetimeNumbers,
                                    readOnly = true,
                                    enabled = true,
                                    onValueChange = {},
                                    trailingIcon = {
                                        IconButton(enabled = true,
                                            onClick = { numberOptions = true }) {
                                            Icon(
                                                if (numberOptions) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                                contentDescription = "Expand/contract lifetime number options"
                                            )
                                        }
                                    })
                                if (numberOptions) {
                                    val numbers = when (newTaskVM.lifetimeUnit) {
                                        "Days" -> (1..30).toList()
                                        "Months" -> (1..11).toList()
                                        "Years" -> (1..10).toList()
                                        else -> emptyList()
                                    }
                                    DropdownMenu(
                                        expanded = numberOptions,
                                        onDismissRequest = { numberOptions = false }
                                    ) {
                                        numbers.forEach { num ->
                                            DropdownMenuItem(
                                                text = { Text(text = num.toString()) },
                                                onClick = {
                                                    newTaskVM.updateLifetimeNumbers(num.toString())
                                                    numberOptions = false
                                                })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (newTaskVM.taskRecurringType != RecurringType.Recursive && newTaskVM.taskEndDateError.isNotBlank()) {
                    Text(
                        newTaskVM.taskEndDateError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                } else {
                    if (newTaskVM.lifetimeError.isNotBlank()) {
                        Text(
                            newTaskVM.lifetimeError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(0.05f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (newTaskVM.taskRecurringType != RecurringType.Recursive) {
                    Text(
                        text = "Recursive",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
                    )
                }
                if (newTaskVM.taskRecurringType == RecurringType.Recursive) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Switch(
                    checked = newTaskVM.taskRecurringType == RecurringType.Recursive,
                    onCheckedChange = { newTaskVM.toggleRecurringType(it) }
                )
            }
        }
        if (newTaskVM.taskRecurringType == RecurringType.Recursive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.TopStart)
                    ) {
                        OutlinedTextField(
                            //modifier = Modifier.fillMaxSize(),
                            readOnly = true,
                            enabled = true,
                            value = newTaskVM.taskRepeat.toString(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(enabled = true,
                                    onClick = { newTaskVM.toggleEditRepeatOptions() }) {
                                    Icon(
                                        if (newTaskVM.isEditingRepeatOptions) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Expand/contract repeat options"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            label = { Text("Repeat") },
                            onValueChange = { /*newTaskVM.updateTaskRepeat(Repeat.fromString(it)) */ },
                            isError = newTaskVM.taskRepeatError.isNotBlank()
                        )
                        DropdownMenu(
                            expanded = newTaskVM.isEditingRepeatOptions,
                            onDismissRequest = { newTaskVM.toggleEditRepeatOptions() }
                        ) {
                            Repeat.entries.forEach {
                                DropdownMenuItem(
                                    text = { Text(text = it.toString()) },
                                    onClick = {
                                        newTaskVM.updateTaskRepeat(it)
                                        newTaskVM.toggleEditRepeatOptions()
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(0.05f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp)
                        .weight(0.5f)
                ) {
                    OutlinedTextField(
                        //modifier = Modifier.fillMaxSize(),
                        readOnly = true,
                        enabled = true,
                        value = newTaskVM.taskEndRepeat,
                        singleLine = true,
                        trailingIcon = {
                            IconButton(enabled = true,
                                onClick = { newTaskVM.toggleEditEndRepeat() }) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(id = R.drawable.round_calendar_today_24),
                                    contentDescription = "Date picker"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Until") },
                        onValueChange = { newTaskVM.updateTaskEndRepeat(it) },
                        isError = newTaskVM.taskEndRepeatError.isNotBlank()
                    )
                    if (newTaskVM.taskEndRepeatError.isNotBlank()) {
                        Text(
                            newTaskVM.taskEndRepeatError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = false,
                    enabled = true,
                    value = newTaskVM.taskTag,
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.round_tag_24),
                            contentDescription = "New Task tag"
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("Tag") },
                    onValueChange = { tag -> newTaskVM.updateTaskTag(tag) },
                    isError = newTaskVM.taskTagError.isNotBlank()
                )
                if (newTaskVM.taskTagError.isNotBlank()) {
                    Text(
                        newTaskVM.taskTagError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = true,
                    value = newTaskVM.selectedCollaborators.filter { it.value }.keys.joinToString("\n") { it.name + " " + it.surname },
                    singleLine = false,
                    trailingIcon = {
                        IconButton(enabled = true,
                            onClick = { newTaskVM.toggleAssignTask() }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Collaborators")
                        }
                    },
                    label = { Text("Collaborators") },
                    onValueChange = { },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (newTaskVM.canCreateNewTask()) {
                            Log.d("NewTaskBottomSheetContent", "New Task")

                        }


                    },
                ) {
                    Text(text = "Save")
                }
            }
        }



        if (newTaskVM.isAssigningTask) {
            AssignTaskDialog(
                onDismissRequest = { newTaskVM.toggleAssignTask() },
                selectedCollaborators = newTaskVM.selectedCollaborators,
                modifyCollaborators = { user, selected ->
                    newTaskVM.modifyCollaborators(
                        user,
                        selected
                    )
                },
                resetCollaborators = { newTaskVM.resetCollaborators() },
                myId = newTaskVM.myID
            )
        }

        if (newTaskVM.isEditingEndDate) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                MyDatePickerDialog(
                    onDateSelected = { endDate ->
                        newTaskVM.updateTaskEndDate(
                            endDate.replace(
                                "/",
                                "-"
                            )
                        )
                    },
                    onDismiss = { newTaskVM.toggleEditEndDate() },
                    actualBirthdate = newTaskVM.taskEndDate
                )
            }
        }

        if (newTaskVM.isEditingEndRepeat) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                MyDatePickerDialog(
                    onDateSelected = { endDate ->
                        newTaskVM.updateTaskEndRepeat(endDate.replace("/", "-"))
                    },
                    onDismiss = { newTaskVM.toggleEditEndRepeat() },
                    actualBirthdate = newTaskVM.taskEndRepeat
                )
            }
        }
    }
}

@Composable
fun AssignTaskDialog(
    onDismissRequest: () -> Unit,
    selectedCollaborators: Map<User, Boolean>,
    modifyCollaborators: (User, Boolean) -> Unit,
    resetCollaborators: () -> Unit,
    myId: String
) {

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
                                text = if (selectedCollaborators.filter { it.value }
                                        .isNotEmpty()) "Collaborators (" + selectedCollaborators.filter { it.value }.size + ")" else "Collaborators",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                selectedCollaborators.forEach {
                    item {
                        Collaborator(
                            user = it.key,
                            selected = it.value,
                            onSelected = { user, selected -> modifyCollaborators(user, selected) },
                            myId = myId
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        ElevatedButton(onClick = { resetCollaborators(); onDismissRequest() }) {
                            Text("Reset")
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Button(onClick = { onDismissRequest() }) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    actualBirthdate: String
) {
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {

        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            val calendar = Calendar.getInstance()

            val date = Date(utcTimeMillis)  //TODO: Create a Task w/ end date = day of creation
            return date.after(calendar.time)
        }

    })

    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: actualBirthdate

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

@Composable
fun Collaborator(
    user: User,
    selected: Boolean,
    onSelected: (User, Boolean) -> Unit,
    myId: String
) {

    val me = myId == user.userId
    val fullName = if (me) {
        user.name + " " + user.surname + " (Me)"
    } else {
        user.name + " " + user.surname
    }

    ListItem(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp),
        headlineContent = { Text(fullName) },
        leadingContent = {
            TeamOnImage(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                source = user.profileImageSource,
                uri = user.profileImage?.toUri(),
                name = user.name,
                surname = user.surname,
                color = user.color,
                description = user.name + " " + user.surname + " profile image"
            )
        },
        trailingContent = {
            if (me) {
                Checkbox(checked = true,
                    enabled = false,
                    onCheckedChange = {}
                )
            } else {
                Checkbox(
                    checked = selected,
                    onCheckedChange = {
                        onSelected(user, it)
                    }
                )
            }

        }
    )
}