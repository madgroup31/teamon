package com.teamon.app.myteams

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.profileViewModel
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.SearchBar
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.prepare
import com.teamon.app.utils.viewmodels.TeamViewModel


@Composable
fun ErrorDialog(onDismissDialog: () -> Unit) {
    AlertDialog(
        title = { Text("You are the only admin of the team") },
        text = { Text("The team must contain at least another admin if you want to exit") },
        onDismissRequest = { onDismissDialog() },
        confirmButton = {
            Button(onClick = { onDismissDialog() }) {
                Text("Ok")
            }
        }
    )
}

@Composable
fun ConfirmRemoveDialog(
    userIdToRemove: String,
    userNameToRemove: String,
    userSurnameToRemove: String,
    onDismissDialog: () -> Unit,
    onConfirmDialog: () -> Unit
) {
    val title: String
    val text: String
    if (userIdToRemove == profileViewModel.userId) {
        title = "Are you sure you want to exit from the team?"
        text = "You will be also excluded by all projects the team is partecipating."
    } else {
        title = "Are you sure you want to remove ${userNameToRemove} ${userSurnameToRemove}?"
        text =
            "${userNameToRemove} ${userSurnameToRemove} will be also eliminated by all projects of the team in which he/she belongs to"
    }


    AlertDialog(
        title = { Text(text = title) },
        text = { Text(text = text) },
        onDismissRequest = { onDismissDialog() },
        confirmButton = {
            Button(
                onClick = { onConfirmDialog() },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.background,
                    containerColor = MaterialTheme.colorScheme.error
                ),
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismissDialog() }) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun ConfirmPromoteDialog(
    userNameToPromote: String,
    userSurnameToPromote: String,
    onDismissDialog: () -> Unit,
    onConfirmDialog: () -> Unit
) {
    val title = "Confirm promotion?"
    val text = "${userNameToPromote} ${userSurnameToPromote} will be promoted to Admin"

    AlertDialog(
        title = { Text(text = title) },
        text = { Text(text = text) },
        onDismissRequest = { onDismissDialog() },
        confirmButton = {
            Button(onClick = { onConfirmDialog() }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismissDialog() }) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun DeleteTeamDialog(
    onDismissDialog: () -> Unit,
    onConfirmDialog: () -> Unit
) {
    var title = "Are you sure to delete the team?"
    var text = "The team will be removed from active projects and all members will be removed."

    AlertDialog(
        title = { Text(title) },
        text = { Text(text) },
        containerColor = MaterialTheme.colorScheme.errorContainer,
        onDismissRequest = { onDismissDialog() },
        confirmButton = {
            Button(
                onClick = { onConfirmDialog() },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismissDialog() }) {
                Text("Cancel")
            }
        }

    )
}


@Composable
fun TeamMembers(actions: Actions, teamVM: TeamViewModel) {
    val listMemberScreen by teamVM.getMembersTeam().collectAsState(initial = emptyList())
    var admins = teamVM.admin
    var userId = profileViewModel!!.userId


    var expandedMembers by remember { mutableStateOf(false) }


    var searchActive by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val onSearchActiveChange: (Boolean) -> Unit = { searchActive = it }
    val onQueryChange: (String) -> Unit = { query = it }


    var dialogRemoveShow by remember { mutableStateOf(false) }
    var dialogPromoteShow by remember { mutableStateOf(false) }
    var dialogError by remember { mutableStateOf(false) }
    var dialogDeleteTeamShow by remember { mutableStateOf(false) }


    var userIdToRemove by remember { mutableStateOf("") }
    var userNameToRemove by remember { mutableStateOf("") }
    var userSurnameToRemove by remember { mutableStateOf("") }


    var userIdToPromote by remember { mutableStateOf("") }
    var userNameToPromote by remember { mutableStateOf("") }
    var userSurnameToPromote by remember { mutableStateOf("") }


    val sortedMembers = mutableListOf<User>()
    sortedMembers += listMemberScreen.sortedByDescending { teamVM.admin.contains(it.userId) }
        .toList()


    val numberMembersToShow = when {
        listMemberScreen.size <= 5 -> listMemberScreen.size
        expandedMembers -> listMemberScreen.size
        else -> 5
    }



    if (!searchActive) {
        if (listMemberScreen.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            {
                AnimatedItem(index = 1) {
                    Text("No members in this team")
                }

            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                //HEADER
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        //NUMBER MEMBERS
                        Text(
                            "${listMemberScreen.size} members",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Start
                        )

                        //SEARCH ICON
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onSearchActiveChange(true) }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }


                //MEMBERS
                itemsIndexed(sortedMembers.take(numberMembersToShow)) { index, member ->
                    AnimatedItem(index = index) {
                        Column {
                            val me = userId == member.userId


                            val selectedNavItem =
                                actions.navCont.currentBackStackEntry?.destination?.route?.split("/")
                                    ?.first().toString()
                            val isAdmin = teamVM.admin.contains(member.userId)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        actions.openProfile(
                                            selectedNavItem,
                                            userId = member.userId
                                        )
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(0.2f)
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

                                            val image = member.profileImage
                                            if (me) {
                                                TeamOnImage(
                                                    modifier = if (isAdmin) Modifier
                                                        .size(100.dp)
                                                        .border(
                                                            2.dp,
                                                            MaterialTheme.colorScheme.primary,
                                                            CircleShape
                                                        )
                                                    else Modifier.size(100.dp),
                                                    source = profileViewModel!!.profileImageSource,
                                                    uri = profileViewModel!!.profileImageUri,
                                                    name = profileViewModel!!.nameValue,
                                                    surname = profileViewModel!!.surnameValue,
                                                    description = "My Profile picture"
                                                )

                                            } else {
                                                TeamOnImage(
                                                    modifier = if (isAdmin) Modifier
                                                        .size(100.dp)
                                                        .border(
                                                            2.dp,
                                                            MaterialTheme.colorScheme.primary,
                                                            CircleShape
                                                        )
                                                    else Modifier.size(100.dp),
                                                    source = member.profileImageSource,
                                                    uri = member.profileImage?.toUri(),
                                                    name = member.name,
                                                    surname = member.surname,
                                                    description = member.name + " " + member.surname + " profile image",
                                                )

                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(
                                    modifier = Modifier.weight(0.7f),
                                    verticalArrangement = Arrangement.Center
                                )
                                {
                                    Column(modifier = Modifier)
                                    {
                                        if (isAdmin)
                                            Text(
                                                "Admin",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                        var text = "${member.name} ${member.surname}"
                                        if (me)
                                            text = text + " (Me)"

                                        Text(
                                            text = text,
                                            fontWeight = if (me) FontWeight.Medium else FontWeight.Normal,
                                        )

                                    }
                                }

                                Spacer(modifier = Modifier.width(5.dp))

                                //OPTIONS
                                Column(
                                    modifier = Modifier.weight(0.1f),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (!me) {
                                        if ((admins.contains(userId)) && (!admins.contains(member.userId))) {

                                            var expandedOptions by remember { mutableStateOf(false) }
                                            IconButton(
                                                modifier = Modifier.size(25.dp),
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.primary
                                                ),
                                                onClick = { expandedOptions = !expandedOptions }) {
                                                Icon(
                                                    Icons.Rounded.MoreVert,
                                                    contentDescription = "Open task dialog"
                                                )
                                            }


                                            if (expandedOptions) {
                                                DropdownMenu(
                                                    modifier = Modifier,
                                                    expanded = expandedOptions,
                                                    onDismissRequest = { expandedOptions = false })
                                                {
                                                    DropdownMenuItem(
                                                        leadingIcon = {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.round_keyboard_double_arrow_up_24),
                                                                colorFilter = ColorFilter.tint(
                                                                    MaterialTheme.colorScheme.onSurface
                                                                ),
                                                                contentDescription = "Promote Member"
                                                            )
                                                        },
                                                        text = { Text("Promote member") },
                                                        onClick = {
                                                            userIdToPromote = member.userId
                                                            userNameToPromote = member.name
                                                            userSurnameToPromote = member.surname

                                                            expandedOptions = false
                                                            dialogPromoteShow = true
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        leadingIcon = {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.round_delete_24),
                                                                colorFilter = ColorFilter.tint(
                                                                    MaterialTheme.colorScheme.error
                                                                ),
                                                                contentDescription = "Remove member"
                                                            )
                                                        },
                                                        text = {
                                                            Text(
                                                                "Remove member",
                                                                color = MaterialTheme.colorScheme.error
                                                            )
                                                        },
                                                        onClick = {

                                                            userIdToRemove = member.userId
                                                            userNameToRemove = member.name
                                                            userSurnameToRemove = member.surname

                                                            expandedOptions = false
                                                            dialogRemoveShow = true

                                                        }
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


                //EXPANDED TEXT
                if (listMemberScreen.size > 5) {
                    if (!expandedMembers) {
                        item {
                            TextButton(onClick = { expandedMembers = true }) {
                                Text(text = "Show others ${listMemberScreen.size - 5} members")
                            }
                        }
                    }
                    if (expandedMembers) {
                        item {
                            TextButton(onClick = { expandedMembers = false }) {
                                Text(text = "Show fewer members")
                            }
                        }
                    }
                }


                //EXIT DELETE BUTTON
                item {

                    AnimatedItem(index = sortedMembers.size)
                    {

                        //EXIT BUTTON
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(20.dp))

                            if (teamVM.users.size > 1)
                                TextButton(onClick = {
                                    userIdToRemove = profileViewModel.userId
                                    userNameToRemove = profileViewModel.nameValue
                                    userSurnameToRemove = profileViewModel.surnameValue

                                    dialogRemoveShow = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Leave the team",
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Leave the team",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }


                            //DELETE BUTTON
                            if (teamVM.admin.contains(profileViewModel.userId))
                                TextButton(
                                    onClick = {
                                        userIdToRemove = profileViewModel!!.userId
                                        userNameToRemove = profileViewModel!!.nameValue
                                        userSurnameToRemove = profileViewModel!!.surnameValue

                                        dialogDeleteTeamShow = true
                                    }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete the team",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Delete team",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }


                        }

                    }
                }
            }
        }
    }

    else {

        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(20f)
                .padding(10.dp),
            query = query,
            placeholder = "Search Members...",
            onQueryChange = onQueryChange,
            searchActive = searchActive,
            onSearchActiveChange = onSearchActiveChange
        ) {

            val filteredMembers = listMemberScreen.filter { member ->
                member.name.contains(query, ignoreCase = true)
                        || member.surname.contains(query, ignoreCase = true)
            }

            if (filteredMembers.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
                {
                    AnimatedItem(index = 1) {
                        Text("No Member Found")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    //MEMBERS
                    itemsIndexed(filteredMembers) { index, member ->
                        AnimatedItem(index = index) {

                            Column {
                                val me = userId == member.userId

                                val selectedNavItem =
                                    actions.navCont.currentBackStackEntry?.destination?.route?.split(
                                        "/"
                                    )
                                        ?.first().toString()
                                val isAdmin = teamVM.admin.contains(member.userId)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            actions.openProfile(
                                                selectedNavItem,
                                                userId = member.userId
                                            )
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(0.2f)
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

                                                val image = member.profileImage
                                                if (me) {
                                                    TeamOnImage(
                                                        modifier = if (isAdmin) Modifier
                                                            .size(100.dp)
                                                            .border(
                                                                2.dp,
                                                                MaterialTheme.colorScheme.primary,
                                                                CircleShape
                                                            )
                                                        else Modifier.size(100.dp),
                                                        source = profileViewModel!!.profileImageSource,
                                                        uri = profileViewModel!!.profileImageUri,
                                                        name = profileViewModel!!.nameValue,
                                                        surname = profileViewModel!!.surnameValue,
                                                        description = "My Profile picture"
                                                    )

                                                } else {
                                                    TeamOnImage(
                                                        modifier = if (isAdmin) Modifier
                                                            .size(100.dp)
                                                            .border(
                                                                2.dp,
                                                                MaterialTheme.colorScheme.primary,
                                                                CircleShape
                                                            )
                                                        else Modifier.size(100.dp),
                                                        source = member.profileImageSource,
                                                        uri = member.profileImage?.toUri(),
                                                        name = member.name,
                                                        surname = member.surname,
                                                        description = member.name + " " + member.surname + " profile image",
                                                    )

                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(
                                        modifier = Modifier.weight(0.7f),
                                        verticalArrangement = Arrangement.Center
                                    )
                                    {
                                        Column(modifier = Modifier)
                                        {
                                            if (isAdmin)
                                                Text(
                                                    "Admin",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )

                                            var text = "${member.name} ${member.surname}"
                                            if (me)
                                                text = text + " (Me)"

                                            Text(
                                                text = text,
                                                fontWeight = if (me) FontWeight.Medium else FontWeight.Normal,
                                            )

                                        }
                                    }

                                    Spacer(modifier = Modifier.width(5.dp))

                                    //OPTIONS
                                    Column(
                                        modifier = Modifier.weight(0.1f),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (!me) {
                                            if ((admins.contains(userId)) && (!admins.contains(
                                                    member.userId
                                                ))
                                            ) {

                                                var expandedOptions by remember {
                                                    mutableStateOf(
                                                        false
                                                    )
                                                }
                                                IconButton(
                                                    modifier = Modifier.size(25.dp),
                                                    colors = IconButtonDefaults.iconButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    onClick = {
                                                        expandedOptions = !expandedOptions
                                                    }) {
                                                    Icon(
                                                        Icons.Rounded.MoreVert,
                                                        contentDescription = "Open task dialog"
                                                    )
                                                }


                                                if (expandedOptions) {
                                                    DropdownMenu(
                                                        modifier = Modifier,
                                                        expanded = expandedOptions,
                                                        onDismissRequest = {
                                                            expandedOptions = false
                                                        })
                                                    {
                                                        DropdownMenuItem(
                                                            leadingIcon = {
                                                                Image(
                                                                    painter = painterResource(id = R.drawable.round_keyboard_double_arrow_up_24),
                                                                    colorFilter = ColorFilter.tint(
                                                                        MaterialTheme.colorScheme.onSurface
                                                                    ),
                                                                    contentDescription = "Promote Member"
                                                                )
                                                            },
                                                            text = { Text("Promote member") },
                                                            onClick = {
                                                                userIdToPromote = member.userId
                                                                userNameToPromote = member.name
                                                                userSurnameToPromote =
                                                                    member.surname

                                                                expandedOptions = false
                                                                dialogPromoteShow = true
                                                            }
                                                        )
                                                        DropdownMenuItem(
                                                            leadingIcon = {
                                                                Image(
                                                                    painter = painterResource(id = R.drawable.round_delete_24),
                                                                    colorFilter = ColorFilter.tint(
                                                                        MaterialTheme.colorScheme.error
                                                                    ),
                                                                    contentDescription = "Remove member"
                                                                )
                                                            },
                                                            text = {
                                                                Text(
                                                                    "Remove member",
                                                                    color = MaterialTheme.colorScheme.error
                                                                )
                                                            },
                                                            onClick = {

                                                                userIdToRemove = member.userId
                                                                userNameToRemove = member.name
                                                                userSurnameToRemove = member.surname

                                                                expandedOptions = false
                                                                dialogRemoveShow = true

                                                            }
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
    }


    //DIALOG DELETE TEAM
    if (dialogDeleteTeamShow) {
        DeleteTeamDialog(
            onDismissDialog = { dialogDeleteTeamShow = false },
            onConfirmDialog = {
                teamVM.deleteTeam()
                dialogDeleteTeamShow = false
                actions.goToMyTeams()
            })
    }

    //DIALOG PROMOTE
    if (dialogPromoteShow) {
        ConfirmPromoteDialog(
            userNameToPromote = userNameToPromote,
            userSurnameToPromote = userSurnameToPromote,
            onDismissDialog = { dialogPromoteShow = false },
            onConfirmDialog = {
                teamVM.promoteToAdmin(userIdToPromote)
                dialogPromoteShow = false
            }
        )
    }


    //DIALOG REMOVE
    if (dialogRemoveShow) {
        ConfirmRemoveDialog(
            userIdToRemove = userIdToRemove,
            userNameToRemove = userNameToRemove,
            userSurnameToRemove = userSurnameToRemove,
            onDismissDialog = { dialogRemoveShow = false },
            onConfirmDialog = {

                //if user is not admin there is no problem
                if (!admins.contains(userId)) {
                    //check if have to go to myTeams
                    if (userIdToRemove == profileViewModel.userId) {
                        actions.goToMyTeams()

                    }
                    teamVM.removeMember(userIdToRemove)
                    dialogRemoveShow = false
                } else {
                    //if user is admin, check if there is at least another admin in the team
                    if (teamVM.checkOthersAdmins(userIdToRemove)) {

                        //check if have to go to myTeams
                        if (userIdToRemove == profileViewModel.userId) {
                            actions.goToMyTeams()
                        }
                        teamVM.removeMember(userIdToRemove)
                        dialogRemoveShow = false

                    } else {
                        dialogRemoveShow = false
                        dialogError = true
                    }
                }
            }
        )
    }

    //DIALOG ERROR IF THERE IS ONE SINGLE ADMIN THAT WANT TO EXIT FROM THE TEAM
    if (dialogError) {
        ErrorDialog(onDismissDialog = { dialogError = false })
    }

}