package com.teamon.app.board.project

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.google.firebase.Timestamp
import com.teamon.app.R
import com.teamon.app.projectsViewModel
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.teamOnViewModel
import com.teamon.app.teamsViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.ProjectImages
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.diffColors
import com.teamon.app.utils.graphics.toTimestamp
import com.teamon.app.utils.graphics.toInt
import com.teamon.app.utils.viewmodels.NewProjectViewModel
import java.util.UUID

@Composable
fun NewProjectBottomSheetContent(
    newProjectVM: NewProjectViewModel,
) {


    val teams by teamsViewModel.getTeams().collectAsState(initial = emptyMap())

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 40.dp)
        //.verticalScroll(scrollState),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Create New Project",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontStyle = MaterialTheme.typography.titleLarge.fontStyle,
                )
            }
        }
        Row(
            modifier = Modifier
                .wrapContentSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .padding(end = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = false,
                        enabled = true,
                        value = newProjectVM.projectName,
                        onValueChange = { name -> newProjectVM.updateProjectName(name) },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.round_text_fields_24),
                                contentDescription = "New Project name"
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        label = { Text("Project Name") },
                        isError = newProjectVM.projectNameError.isNotBlank()
                    )
                }
                    if (newProjectVM.projectNameError.isNotBlank()) {
                        Row {
                        Text(
                            newProjectVM.projectNameError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = false,
                        enabled = true,
                        value = newProjectVM.projectDescription,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.round_text_fields_24),
                                contentDescription = "New Project description"
                            )
                        },
                        singleLine = false,
                        minLines = 2,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        label = { Text("Description") },
                        onValueChange = { description ->
                            newProjectVM.updateProjectDescription(
                                description
                            )
                        },
                        isError = newProjectVM.projectDescriptionError.isNotBlank()
                    )
                }
                if (newProjectVM.projectDescriptionError.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            newProjectVM.projectDescriptionError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Icon",
                        modifier = Modifier
                            .padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        fontStyle = MaterialTheme.typography.titleMedium.fontStyle,
                    )
                }
                Row(
                    modifier = Modifier
                        .size(64.dp)
                ) {
                    ImageOption(
                        image = newProjectVM.projectImage,
                        isSelected = true,
                        onSelected = { newProjectVM.togglePickImage() }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Color",
                        modifier = Modifier
                            .padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        fontStyle = MaterialTheme.typography.titleMedium.fontStyle,
                    )
                }
                Row(
                    modifier = Modifier
                        .size(64.dp)
                ) {
                    ColorOption(
                        color = newProjectVM.projectColor,
                        isSelected = true,
                        onSelected = { newProjectVM.togglePickColor() }
                    )
                }
            }

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                //horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = true,
                    value = teams.values.filter { it.teamId in newProjectVM.listTeams }
                        .joinToString(", ") { it.name },
                    singleLine = false,
                    minLines = 1,
                    trailingIcon = {
                        IconButton(enabled = true,
                            onClick = { newProjectVM.toggleAssignProject() }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Teams")
                        }
                    },
                    label = { Text("Teams") },
                    onValueChange = { /*collaborators -> newTaskCollaborators = collaborators*/ },
                    //isError = profileVm.birthdateError.isNotBlank()
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
                        if (newProjectVM.canCreateNewProject()) {
                            val project = Project(
                                projectName = newProjectVM.projectName,
                                projectColor = newProjectVM.projectColor,
                                projectImage = newProjectVM.projectImage,
                                description = newProjectVM.projectDescription,
                                endDate = Timestamp.now(),
                                teams = newProjectVM.listTeams.toList(),
                                progress = 0.0f,
                                chatBadge = false,
                                favorite = false,
                                tasks = listOf(),
                            )
                            projectsViewModel.addProject(project)
                            newProjectVM.toggleShow() //if added, hide bottom sheet
                            newProjectVM.reset()
                        }


                    },
                ) {
                    Text(text = "Save")
                }
            }
        }

        if (newProjectVM.isPickingColor) {
            ColorPickerDialog(
                previouslySelected = newProjectVM.projectColor,
                onDismissRequest = { newProjectVM.togglePickColor() }
            ) {
                newProjectVM.updateProjectColor(it)
                newProjectVM.togglePickColor()
            }
        }

        if (newProjectVM.isPickingImage) {
            ImagePickerDialog(
                previouslySelected = newProjectVM.projectImage,
                onDismissRequest = { newProjectVM.togglePickImage() }
            ) {
                newProjectVM.updateProjectImage(it)
                newProjectVM.togglePickImage()
            }
        }

        if (newProjectVM.isAssigningProject) {

            AssignProjectDialog(
                onDismissRequest = { newProjectVM.toggleAssignProject() },
                selectedTeams = newProjectVM.listTeams,
                teams = teams.values.toList(),
                modifyCollaborators = { teamId, selected ->
                    newProjectVM.updateTeam(teamId, selected)
                })
        }

        //AlertDialog(onDismissRequest = { newProjectVM.toggleAssignProject() }, confirmButton = { /*TODO*/ })
    }
}

@Composable
fun AssignProjectDialog(
    onDismissRequest: () -> Unit,
    selectedTeams: List<String>,
    teams: List<Team>,
    modifyCollaborators: (String, Boolean) -> Unit,
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
                                text = if (selectedTeams.isNotEmpty()) "Teams (" + selectedTeams.size + ")" else "Teams",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                teams.forEach {
                    item {
                        Collaborator(
                            team = it,
                            selected = it.teamId in selectedTeams,
                            onSelected = { teamId, selected ->
                                modifyCollaborators(
                                    teamId,
                                    selected
                                )
                            }
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
                        ElevatedButton(onClick = { onDismissRequest() }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Button(onClick = {
                            onDismissRequest()
                        }) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun Collaborator(
    team: Team,
    selected: Boolean,
    onSelected: (String, Boolean) -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    ListItem(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp),
        headlineContent = { Text(team.name) },
        leadingContent = {
            TeamOnImage(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                uri = team.image.toUri(),
                name = team.name,
                description = team.name + " profile image",
                source = team.imageSource
            )
        },
        trailingContent = {
            Checkbox(
                checked = selected,
                onCheckedChange = {
                    onSelected(team.teamId, it)
                }
            )
        }
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun ColorPickerDialog(
    previouslySelected: ProjectColors,
    onDismissRequest: () -> Unit,
    onConfirm: (ProjectColors) -> Unit,
) {
    var selected by rememberSaveable { mutableStateOf(previouslySelected) }
    val colors = ProjectColors.entries.toTypedArray()
    colors.sortBy { diffColors(it.toInt().toHexString(), "0xFFFFFFFF") }
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
    ) {
        OutlinedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(10.dp)
            //border = BorderStroke(1.dp, Color.DarkGray),
        )
        {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                    ){
                        Text(
                            text = "Color",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontStyle = MaterialTheme.typography.titleMedium.fontStyle
                        )
                    }
                    Row {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(80.dp),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxHeight(0.4f)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(20.dp),
                                ),
                            content = {
                                items(colors.size) { index ->
                                    val color = colors[index]
                                    val isSelected = color == selected
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .padding(6.dp)
                                    ) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.Center,
                                            ) {
                                                Text(
                                                    text = color.toString(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 7.sp
                                                )
                                            }
                                            Row {
                                                ColorOption(
                                                    color = color,
                                                    isSelected = isSelected,
                                                    onSelected = { selected = it }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                ElevatedButton(onClick = { onDismissRequest() }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(5.dp))
                Button(onClick = {
                    onConfirm(selected)
                }) {
                    Text("Confirm")
                }
            }
        }
    }
}

@Composable
fun ColorOption(
    modifier: Modifier = Modifier,
    color: ProjectColors,
    isSelected: Boolean,
    onSelected: (ProjectColors) -> Unit
) {
    Column(
        modifier = Modifier
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                //colors = CardDefaults.elevatedCardColors(color.toColor()),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 8.dp else 2.dp
                ),
                content = {
                    Box(
                        modifier = modifier
                            //.size(32.dp)
                            .fillMaxSize()
                            //.background(color = Color.Red)
                            .background(
                                brush = Brush.radialGradient(colors = color.toGradient()),
                                shape = RectangleShape,
                                alpha = if (isSelected) 0.8f else 0.4f,
                            ) // convert ProjectColors to actual color
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(20.dp),
                            ),
                    ) {
                        IconButton(
                            modifier = Modifier
                                .fillMaxSize(),
                            onClick = { onSelected(color) })
                        {

                        }
                    }

                },
                onClick = { onSelected(color) },
            )
        }
    }

}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun ImagePickerDialog(
    previouslySelected: ProjectImages,
    onDismissRequest: () -> Unit,
    onConfirm: (ProjectImages) -> Unit,
) {
    var selected by rememberSaveable { mutableStateOf(previouslySelected) }
    val images = ProjectImages.entries.toTypedArray()
    images.sortBy { it.toString() }

    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
    ) {
        OutlinedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(10.dp)
        )
        {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                    ){
                        Text(
                            text = "Icon",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontStyle = MaterialTheme.typography.titleMedium.fontStyle,
                        )
                    }
                    Row {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(80.dp),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxHeight(0.4f)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(20.dp),
                                ),
                            content = {
                                items(images.size) { index ->
                                    val image = images[index]
                                    val isSelected = image == selected
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .padding(6.dp)
                                    ) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.Center,
                                            ) {
                                                Text(
                                                    text = image.toString(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 7.sp
                                                )
                                            }
                                            Row {
                                                ImageOption(
                                                    image = image,
                                                    isSelected = isSelected,
                                                    onSelected = { selected = it }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                ElevatedButton(onClick = { onDismissRequest() }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(5.dp))
                Button(onClick = {
                    onConfirm(selected)
                }) {
                    Text("Confirm")
                }
            }
        }
    }
}

@Composable
fun ImageOption(
    modifier: Modifier = Modifier,
    image: ProjectImages,
    isSelected: Boolean,
    onSelected: (ProjectImages) -> Unit
) {
    Column(
        modifier = Modifier
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                //colors = CardDefaults.elevatedCardColors(color.toColor()),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 8.dp else 2.dp
                ),
                content = {
                    Box(
                        modifier = modifier
                            //.size(32.dp)
                            .fillMaxSize()
                            //.background(color = Color.Red)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(20.dp),
                            ),
                    ) {
                        IconButton(
                            modifier = Modifier
                                .fillMaxSize(),
                            onClick = { onSelected(image) })
                        {
                            Icon(
                                image.toImage(),
                                contentDescription = "New Project name"
                            )
                        }
                    }

                },
                onClick = { onSelected(image) },
            )
        }
    }

}
