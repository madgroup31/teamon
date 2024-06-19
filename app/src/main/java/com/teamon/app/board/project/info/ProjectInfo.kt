package com.teamon.app.board.project.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.prefs
import com.teamon.app.utils.viewmodels.ProjectViewModel
import com.teamon.app.profileViewModel
import com.teamon.app.tasks.info.MyDatePickerDialog
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.SearchBar
import kotlin.math.ceil
import kotlin.math.log

@Composable
fun PortraitProjectInfoView(
    projectVM: ProjectViewModel,
    actions: Actions
) {
    val teams = projectVM.teams
    val users = projectVM.members.values.sortedBy { it.name + " " + it.surname }.toList()
    var listExpanded by remember { mutableStateOf(false) }

    var searchActive by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val onSearchActiveChange: (Boolean) -> Unit = { searchActive = it }
    val onQueryChange: (String) -> Unit = { query = it }

    LazyColumn(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //TODO: Project Image
        //NAME
        item {
            AnimatedItem(index = 1) {
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
                                value = projectVM.projectName,
                                onValueChange = { projectVM.updateProjectName(it) },
                                readOnly = !projectVM.isEditing,
                                label = { Text("Project Name") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = projectVM.projectNameError.isNotBlank()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            if (projectVM.projectNameError.isNotBlank()) {
                                Text(
                                    projectVM.projectNameError,
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
                                value = projectVM.projectDescription,
                                onValueChange = { projectVM.updateProjectDescription(it) },
                                readOnly = !projectVM.isEditing,
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = projectVM.projectDescriptionError.isNotBlank()
                            )
                            if (projectVM.projectDescriptionError.isNotBlank()) {
                                Text(
                                    projectVM.projectDescriptionError,
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
                                value = projectVM.projectEndDate,
                                onValueChange = { projectVM.updateProjectEndDate(it) },
                                enabled = true,
                                readOnly = !projectVM.isEditing,
                                label = { Text("End Date") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = projectVM.projectEndDateError.isNotBlank(),
                                trailingIcon = {
                                    IconButton(enabled = projectVM.isEditing,
                                        onClick = { projectVM.toggleEditEndDate() }) {
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
                            if (projectVM.projectEndDateError.isNotBlank()) {
                                Text(
                                    projectVM.projectEndDateError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (projectVM.isEditingEndDate) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    //val latestTaskDate = projectVM.tasks.maxByOrNull { LocalDate.parse(it.endDate, DateTimeFormatter.ofPattern("dd-MM-yyyy")) }?.endDate
                    MyDatePickerDialog(
                        onDateSelected = {
                            projectVM.updateProjectEndDate(
                                it.replace(
                                    "/",
                                    "-"
                                )
                            )
                        },
                        onDismiss = { projectVM.toggleEditEndDate() },
                        actualDate = projectVM.projectEndDate,
                        //firstSelectable = latestTaskDate
                    )
                }
            }
        }

        // TODO: Project Tag as union of project task tags
        item {
            AnimatedItem(index = 1) {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        item {
            AnimatedItem(index = 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                )
                {

                    Text(
                        text = "Project Collaborators",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(
                        onClick = { listExpanded = !listExpanded },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            if (listExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Expand/contract users list"
                        )
                    }
                }
            }
        }

        var size = if (listExpanded) users.size
        else ceil(log(users.size.toDouble(), 2.0)).toInt()

        if (users.size < 1) {
            item {
                AnimatedItem(index = 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text("No users partecipating yet.", textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            if (!searchActive)
            {
                //HEADER
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }


                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //NUMBER MEMBERS
                        Text(
                            "${users.size} collaborators",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Start
                        )


                        Row(horizontalArrangement = Arrangement.End) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { onSearchActiveChange(true) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }


                //COLLABORATORS
                users.take(size).forEachIndexed { index, user ->

                    val isAdmin = projectVM.teams.filter { it.users.contains(user.userId) }
                        .any { it.admin.contains(user.userId) }

                    val me = profileViewModel!!.userId == user.userId
                    val selectedNavItem =
                        actions.navCont.currentBackStackEntry?.destination?.route?.split("/")
                            ?.first()
                            .toString()
                    item {
                        AnimatedItem(index = index + 3) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        actions.openProfile(
                                            selectedNavItem,
                                            userId = user.userId
                                        )
                                    }
                                    .padding(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(2f)
                                )
                                {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(100.dp)
                                                .height(70.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val image = user.profileImage
                                            if (me) {
                                                TeamOnImage(
                                                    modifier = if (isAdmin) Modifier
                                                        .size(50.dp)
                                                        .clip(CircleShape)
                                                        .border(
                                                            2.dp,
                                                            MaterialTheme.colorScheme.primary,
                                                            CircleShape
                                                        )
                                                    else Modifier
                                                        .size(50.dp)
                                                        .clip(CircleShape),
                                                    source = profileViewModel!!.profileImageSource,
                                                    uri = profileViewModel!!.profileImageUri,
                                                    name = profileViewModel!!.nameValue,
                                                    surname = profileViewModel!!.surnameValue,
                                                    color = profileViewModel.color,
                                                    description = "My Profile Picture"
                                                )
                                            } else {
                                                TeamOnImage(
                                                    modifier = if (isAdmin) Modifier
                                                        .size(50.dp)
                                                        .clip(CircleShape)
                                                        .border(
                                                            2.dp,
                                                            MaterialTheme.colorScheme.primary,
                                                            CircleShape
                                                        )
                                                    else Modifier
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
                                Spacer(modifier = Modifier.width(10.dp))

                                Column(
                                    modifier = Modifier.weight(5f),
                                    verticalArrangement = Arrangement.Center
                                )
                                {
                                    if (isAdmin)
                                        Row {
                                            Text(
                                                text = "Admin",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary

                                            )
                                        }
                                    Row(modifier = Modifier)
                                    {

                                        Text(
                                            text = if (me) "${user.name} ${user.surname} (Me)"
                                            else "${user.name} ${user.surname}",
                                            fontWeight = if (me) FontWeight.Medium else FontWeight.Normal

                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(2f),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                )
                                {

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val t =
                                            projectVM.teams.filter { it.users.contains(user.userId) }
                                        t.take(2).forEach {
                                            TeamOnImage(
                                                modifier = Modifier.size(24.dp).clip(CircleShape),
                                                source = it.imageSource,
                                                name = it.name,
                                                surname = "",
                                                color = it.color,
                                                uri = it.image.toUri(),
                                                description = it.name + " profile image",
                                            )
                                        }
                                        if (t.size > 2) {
                                            Text(
                                                text = " +${t.size - 2}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                item {
                    SearchBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(20f),
                        query = query,
                        placeholder = "Search Collaborators...",
                        onQueryChange = onQueryChange,
                        searchActive = searchActive,
                        onSearchActiveChange = onSearchActiveChange
                    ) {

                        val filteredCollaborators = users.filter { user ->
                            user.name.contains(query, ignoreCase = true) ||
                                    user.surname.contains(query, ignoreCase = true)
                        }

                        if (filteredCollaborators.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            )
                            {
                                AnimatedItem(index = 1) {
                                    Text("No Collaborators Found")
                                }
                            }
                        } else
                            LazyColumn(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            )
                            {
                                // COLLABORATORS
                                itemsIndexed(filteredCollaborators) { index, user ->
                                    val isAdmin = projectVM.teams.any { team ->
                                        team.users.contains(user.userId) && team.admin.contains(user.userId)
                                    }

                                    val me = profileViewModel!!.userId == user.userId
                                    val selectedNavItem =
                                        actions.navCont.currentBackStackEntry?.destination?.route?.split(
                                            "/"
                                        )?.first().toString()

                                    AnimatedItem(index = index + 3) {
                                        Row(
                                            modifier = Modifier
                                                .clickable {
                                                    actions.openProfile(
                                                        selectedNavItem,
                                                        userId = user.userId
                                                    )
                                                }
                                                .padding(5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(2f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxSize(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(100.dp)
                                                            .height(70.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (me) {
                                                            TeamOnImage(
                                                                modifier = if (isAdmin) Modifier
                                                                    .size(50.dp)
                                                                    .clip(CircleShape)
                                                                    .border(
                                                                        2.dp,
                                                                        MaterialTheme.colorScheme.primary,
                                                                        CircleShape
                                                                    )
                                                                else Modifier
                                                                    .size(50.dp)
                                                                    .clip(CircleShape),
                                                                source = profileViewModel!!.profileImageSource,
                                                                color = profileViewModel.color,
                                                                uri = profileViewModel!!.profileImageUri,
                                                                name = profileViewModel!!.nameValue,
                                                                surname = profileViewModel!!.surnameValue,
                                                                description = "My Profile Picture"
                                                            )
                                                        } else {
                                                            TeamOnImage(
                                                                modifier = if (isAdmin) Modifier
                                                                    .size(50.dp)
                                                                    .clip(CircleShape)
                                                                    .border(
                                                                        2.dp,
                                                                        MaterialTheme.colorScheme.primary,
                                                                        CircleShape
                                                                    )
                                                                else Modifier
                                                                    .size(50.dp)
                                                                    .clip(CircleShape),
                                                                source = user.profileImageSource,
                                                                color = user.color,
                                                                uri = user.profileImage?.toUri(),
                                                                name = user.name,
                                                                surname = user.surname,
                                                                description = "${user.name} ${user.surname} profile image"
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(
                                                modifier = Modifier.weight(5f),
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                if (isAdmin) {
                                                    Row {
                                                        Text(
                                                            text = "Admin",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                                Row {
                                                    Text(
                                                        text = if (me) "${user.name} ${user.surname} (Me)"
                                                        else "${user.name} ${user.surname}",
                                                        fontWeight = if (me) FontWeight.Medium else FontWeight.Normal
                                                    )
                                                }
                                            }
                                            Column(
                                                modifier = Modifier.weight(2f),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val teamsWithUser = projectVM.teams.filter {
                                                        it.users.contains(user.userId)
                                                    }
                                                    teamsWithUser.take(2).forEach { team ->
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
                                                    if (teamsWithUser.size > 2) {
                                                        Text(
                                                            text = " +${teamsWithUser.size - 2}",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = MaterialTheme.colorScheme.primary
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

    }

    if (projectVM.isConfirmDialogShow) {
        AlertDialog(
            modifier = Modifier.wrapContentSize(),
            onDismissRequest = { projectVM.toggleConfirmDialog() },
            title = { Text(text = "Confirm changes") },
            text = {
                Text(text = "Are you sure to save the changes ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        projectVM.validate()
                        projectVM.toggleConfirmDialog()
                    }
                ) {
                    Text(text = "Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        projectVM.toggleConfirmDialog()
                    }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}


    @Composable
    fun LandscapeProjectInfoView(
        projectVM: ProjectViewModel,
        actions: Actions,
    ) {

        val userId = profileViewModel.userId
        val teams = projectVM.teams
        val users = projectVM.members.values.sortedBy { it.name + " " + it.surname }.toList()

        var searchActive by rememberSaveable { mutableStateOf(false) }
        var query by rememberSaveable { mutableStateOf("") }
        val onSearchActiveChange: (Boolean) -> Unit = { searchActive = it }
        val onQueryChange: (String) -> Unit = { query = it }

        // Initialize listExpanded with the same size as teamUsers, all set to false
        var listExpanded by rememberSaveable { mutableStateOf(false) }


        LazyColumn(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //TODO: Project Image

            //NAME
            item {
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
                            value = projectVM.projectName,
                            onValueChange = { projectVM.updateProjectName(it) },
                            readOnly = !projectVM.isEditing,
                            label = { Text("Project Name") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = projectVM.projectNameError.isNotBlank()
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (projectVM.projectNameError.isNotBlank()) {
                            Text(
                                projectVM.projectNameError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            //DESCRIPTION
            item {
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
                            value = projectVM.projectDescription,
                            onValueChange = { projectVM.updateProjectDescription(it) },
                            readOnly = !projectVM.isEditing,
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = projectVM.projectDescriptionError.isNotBlank()
                        )
                        if (projectVM.projectDescriptionError.isNotBlank()) {
                            Text(
                                projectVM.projectDescriptionError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            //END DATE
            item {
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
                            value = projectVM.projectEndDate,
                            onValueChange = { projectVM.updateProjectEndDate(it) },
                            enabled = !projectVM.isEditing,
                            readOnly = !projectVM.isEditing,
                            label = { Text("End Date") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = projectVM.projectEndDateError.isNotBlank(),
                            trailingIcon = {
                                IconButton(enabled = projectVM.isEditing,
                                    onClick = { projectVM.toggleEditEndDate() }) {
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
                        if (projectVM.projectEndDateError.isNotBlank()) {
                            Text(
                                projectVM.projectEndDateError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (projectVM.isEditingEndDate) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        MyDatePickerDialog(
                            onDateSelected = {
                                projectVM.updateProjectEndDate(
                                    it.replace(
                                        "/",
                                        "-"
                                    )
                                )
                            },
                            onDismiss = { projectVM.toggleEditEndDate() },
                            actualDate = projectVM.projectEndDate
                        )
                    }
                }
            }

            // TODO: Project Tag as union of project task tags
            item { Spacer(modifier = Modifier.height(30.dp)) }

            item {
                AnimatedItem(index = 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {

                        Text(
                            text = "Project Collaborators",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(
                            onClick = { listExpanded = !listExpanded },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                if (listExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Expand/contract users list"
                            )
                        }
                    }
                }
            }

            var size = if (listExpanded) users.size
            else ceil(log(users.size.toDouble(), 2.0)).toInt()

            if (users.size < 1) {
                item {
                    AnimatedItem(index = 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text("No users partecipating yet.", textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                if (!searchActive)
                {
                    //HEADER
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }


                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            //NUMBER MEMBERS
                            Text(
                                "${users.size} collaborators",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Start
                            )


                            Row(horizontalArrangement = Arrangement.End) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { onSearchActiveChange(true) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }


                    //COLLABORATORS
                    users.take(size).forEachIndexed { index, user ->

                        val isAdmin = projectVM.teams.filter { it.users.contains(user.userId) }
                            .any { it.admin.contains(user.userId) }

                        val me = profileViewModel!!.userId == user.userId
                        val selectedNavItem =
                            actions.navCont.currentBackStackEntry?.destination?.route?.split("/")
                                ?.first()
                                .toString()
                        item {
                            AnimatedItem(index = index + 3) {
                                Row(
                                    modifier = Modifier
                                        .clickable {
                                            actions.openProfile(
                                                selectedNavItem,
                                                userId = user.userId
                                            )
                                        }
                                        .padding(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(2f)
                                    )
                                    {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .height(70.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val image = user.profileImage
                                                if (me) {
                                                    TeamOnImage(
                                                        modifier = if (isAdmin) Modifier
                                                            .size(50.dp)
                                                            .clip(CircleShape)
                                                            .border(
                                                                2.dp,
                                                                MaterialTheme.colorScheme.primary,
                                                                CircleShape
                                                            )
                                                        else Modifier
                                                            .size(50.dp)
                                                            .clip(CircleShape),
                                                        source = profileViewModel!!.profileImageSource,
                                                        color = profileViewModel.color,
                                                        uri = profileViewModel!!.profileImageUri,
                                                        name = profileViewModel!!.nameValue,
                                                        surname = profileViewModel!!.surnameValue,
                                                        description = "My Profile Picture"
                                                    )
                                                } else {
                                                    TeamOnImage(
                                                        modifier = if (isAdmin) Modifier
                                                            .size(50.dp)
                                                            .clip(CircleShape)
                                                            .border(
                                                                2.dp,
                                                                MaterialTheme.colorScheme.primary,
                                                                CircleShape
                                                            )
                                                        else Modifier
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
                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(
                                        modifier = Modifier.weight(5f),
                                        verticalArrangement = Arrangement.Center
                                    )
                                    {
                                        if (isAdmin)
                                            Row {
                                                Text(
                                                    text = "Admin",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary

                                                )
                                            }
                                        Row(modifier = Modifier)
                                        {

                                            Text(
                                                text = if (me) "${user.name} ${user.surname} (Me)"
                                                else "${user.name} ${user.surname}",
                                                fontWeight = if (me) FontWeight.Medium else FontWeight.Normal

                                            )
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.weight(2f),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    )
                                    {

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val t =
                                                projectVM.teams.filter { it.users.contains(user.userId) }
                                            t.take(2).forEach {
                                                TeamOnImage(
                                                    modifier = Modifier.size(24.dp).clip(CircleShape),
                                                    source = it.imageSource,
                                                    name = it.name,
                                                    color = it.color,
                                                    surname = "",
                                                    uri = it.image.toUri(),
                                                    description = it.name + " profile image",
                                                )
                                            }
                                            if (t.size > 2) {
                                                Text(
                                                    text = " +${t.size - 2}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    item {
                        SearchBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(20f),
                            query = query,
                            placeholder = "Search Collaborators...",
                            onQueryChange = onQueryChange,
                            searchActive = searchActive,
                            onSearchActiveChange = onSearchActiveChange
                        ) {

                            val filteredCollaborators = users.filter { user ->
                                user.name.contains(query, ignoreCase = true) ||
                                        user.surname.contains(query, ignoreCase = true)
                            }

                            if (filteredCollaborators.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                )
                                {
                                    AnimatedItem(index = 1) {
                                        Text("No Collaborators Found")
                                    }
                                }
                            } else
                                LazyColumn(
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                )
                                {
                                    // COLLABORATORS
                                    itemsIndexed(filteredCollaborators) { index, user ->
                                        val isAdmin = projectVM.teams.any { team ->
                                            team.users.contains(user.userId) && team.admin.contains(user.userId)
                                        }

                                        val me = profileViewModel!!.userId == user.userId
                                        val selectedNavItem =
                                            actions.navCont.currentBackStackEntry?.destination?.route?.split(
                                                "/"
                                            )?.first().toString()

                                        AnimatedItem(index = index + 3) {
                                            Row(
                                                modifier = Modifier
                                                    .clickable {
                                                        actions.openProfile(
                                                            selectedNavItem,
                                                            userId = user.userId
                                                        )
                                                    }
                                                    .padding(5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(2f)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxSize(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .width(100.dp)
                                                                .height(70.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            if (me) {
                                                                TeamOnImage(
                                                                    modifier = if (isAdmin) Modifier
                                                                        .size(50.dp)
                                                                        .clip(CircleShape)
                                                                        .border(
                                                                            2.dp,
                                                                            MaterialTheme.colorScheme.primary,
                                                                            CircleShape
                                                                        )
                                                                    else Modifier
                                                                        .size(50.dp)
                                                                        .clip(CircleShape),
                                                                    source = profileViewModel!!.profileImageSource,
                                                                    uri = profileViewModel!!.profileImageUri,
                                                                    color = profileViewModel.color,
                                                                    name = profileViewModel!!.nameValue,
                                                                    surname = profileViewModel!!.surnameValue,
                                                                    description = "My Profile Picture"
                                                                )
                                                            } else {
                                                                TeamOnImage(
                                                                    modifier = if (isAdmin) Modifier
                                                                        .size(50.dp)
                                                                        .clip(CircleShape)
                                                                        .border(
                                                                            2.dp,
                                                                            MaterialTheme.colorScheme.primary,
                                                                            CircleShape
                                                                        )
                                                                    else Modifier
                                                                        .size(50.dp)
                                                                        .clip(CircleShape),
                                                                    source = user.profileImageSource,
                                                                    uri = user.profileImage?.toUri(),
                                                                    color = user.color,
                                                                    name = user.name,
                                                                    surname = user.surname,
                                                                    description = "${user.name} ${user.surname} profile image"
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(
                                                    modifier = Modifier.weight(5f),
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    if (isAdmin) {
                                                        Row {
                                                            Text(
                                                                text = "Admin",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    }
                                                    Row {
                                                        Text(
                                                            text = if (me) "${user.name} ${user.surname} (Me)"
                                                            else "${user.name} ${user.surname}",
                                                            fontWeight = if (me) FontWeight.Medium else FontWeight.Normal
                                                        )
                                                    }
                                                }
                                                Column(
                                                    modifier = Modifier.weight(2f),
                                                    verticalArrangement = Arrangement.Center,
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        val teamsWithUser = projectVM.teams.filter {
                                                            it.users.contains(user.userId)
                                                        }
                                                        teamsWithUser.take(2).forEach { team ->
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
                                                        if (teamsWithUser.size > 2) {
                                                            Text(
                                                                text = " +${teamsWithUser.size - 2}",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = MaterialTheme.colorScheme.primary
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


            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (projectVM.isConfirmDialogShow) {
            AlertDialog(
                modifier = Modifier.wrapContentSize(),
                onDismissRequest = { projectVM.toggleConfirmDialog() },
                title = { Text(text = "Confirm changes") },
                text = {
                    Text(text = "Are you sure to save the changes ?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            projectVM.validate()
                            projectVM.toggleConfirmDialog()
                        }
                    ) {
                        Text(text = "Confirm")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            projectVM.toggleConfirmDialog()
                        }
                    ) {
                        Text(text = "Cancel")
                    }
                }
            )
        }

    }