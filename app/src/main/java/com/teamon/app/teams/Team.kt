package com.teamon.app.myteams

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.teams.chat.TeamChat
import com.teamon.app.profileViewModel
import com.teamon.app.teamOnViewModel
import com.teamon.app.teamsViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.ScrollableTab
import com.teamon.app.utils.graphics.TabItem
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.UploadStatus
import com.teamon.app.utils.graphics.generateQRCode
import com.teamon.app.utils.viewmodels.TeamViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TeamActions(teamId: String) {
    val team by teamsViewModel.getTeam(teamId).collectAsState(initial = Team())
    val uri = "https://teamon.app/teams/${teamId}/join".toUri()
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newRawUri("TeamOn Invitation Link", uri)
    var qrCode: Bitmap? by remember { mutableStateOf(null) }
    var showQRCode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            generateQRCode(
                uri.toString(),
                size = 1024
            )?.let {
                qrCode = it
            }
        }
    }

    IconButton(onClick = { clipboardManager.setPrimaryClip(clip) }) {
        Image(
            painter = painterResource(id = R.drawable.round_link_24),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            contentDescription = "Invite via link"
        )
    }
    IconButton(onClick = { showQRCode = true }) {
        Image(
            painter = painterResource(id = R.drawable.round_qr_code_24),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            contentDescription = "Invite via QR code"
        )
    }
    if (showQRCode && qrCode != null) {
        AlertDialog(
            modifier = Modifier.size(400.dp),
            onDismissRequest = { showQRCode = false },
            text = {
                Surface(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceAround,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Invite via QR Code",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            modifier = Modifier.weight(2f),
                            text = "Let the invited user scan the QR code to join \"" + team.name + "\".",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Image(
                            modifier = Modifier
                                .weight(5f)
                                .clip(RoundedCornerShape(20.dp)),
                            bitmap = qrCode!!.asImageBitmap(),
                            contentDescription = "QR Code Invitation",
                        )
                    }

                }
            },
            confirmButton = {}
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TeamView(
    actions: Actions,
    teamVM: TeamViewModel,
    startingTab: String = TabItem.TeamInfo.title
) {


    var messageText by rememberSaveable { mutableStateOf("") }
    var search by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    val onMessageTextChange: (String) -> Unit = { messageText = it }
    val onSearchChange: (Boolean) -> Unit = { search = it }
    val isQuerying: () -> Boolean = { query.isNotBlank() && search }
    val onQueryChange: (String) -> Unit = { query = it }


    val items = listOf(
        TabItem.TeamInfo,
        TabItem.TeamProject,
        TabItem.TeamChat,
        TabItem.TeamMembers,
        TabItem.TeamFeedbacks,
        TabItem.TeamAchievement
    )

    val pagerState = rememberPagerState(pageCount = { items.size },
        initialPage = items.indexOfFirst { it.title == startingTab })

    var landscape by remember { mutableStateOf(false) }
    landscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE


    if (landscape) LandscapeViewTeam(
        actions = actions,
        teamVM = teamVM,
        search = search,
        onSearchChange = onSearchChange,
        query = query,
        pagerState = pagerState,
        isQuerying = isQuerying,
        onQueryChange = onQueryChange,
        messageText = messageText,
        onMessageTextChange = onMessageTextChange,
    )
    else PortraitViewTeam(
        actions = actions,
        teamVM = teamVM,
        search = search,
        pagerState = pagerState,
        onSearchChange = onSearchChange,
        query = query,
        isQuerying = isQuerying,
        onQueryChange = onQueryChange,
        messageText = messageText,
        onMessageTextChange = onMessageTextChange,
    )

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LandscapeViewTeam(
    actions: Actions,
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
    isQuerying: () -> Boolean,
    onQueryChange: (String) -> Unit,
    teamVM: TeamViewModel,
    pagerState: PagerState,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    Theme(
        applyToStatusBar = true,
        color = teamVM.teamColor
    ) {
        AppSurface(
            orientation = Orientation.LANDSCAPE,
            actions = actions,
            title = teamVM.teamName,
            snackbarHostState = snackbarHostState,
            tabActions = {
                val items = listOf(
                    TabItem.TeamInfo,
                    TabItem.TeamProject,
                    TabItem.TeamChat,
                    TabItem.TeamMembers,
                    TabItem.TeamFeedbacks,
                    TabItem.TeamAchievement
                )
                ScrollableTab(
                    orientation = Orientation.LANDSCAPE,
                    items = items,
                    pagerState = pagerState,
                    selected = pagerState.currentPage,
                )
            },
            leadingTopBarActions = {
                IconButton(
                    onClick = { actions.navCont.popBackStack() },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
                }
            },
            trailingTopBarActions = {
                TeamActions(teamId = teamVM.teamId)
                IconButton(onClick = { onSearchChange(true) }) {
                    Icon(Icons.Rounded.Search, contentDescription = "Search a comment")
                }
            },
            floatingActionButton = {
                when (pagerState.currentPage) {

                    0 -> {
                        if(teamVM.admin.contains(profileViewModel.userId) && teamVM.uploadStatus !is UploadStatus.Progress)
                        FloatingActionButton(
                            onClick = {
                                if (!teamVM.isEditing)
                                    teamVM.toggleEdit()
                                else {
                                    teamVM.checkAll()
                                }
                            },
                            modifier = Modifier
                                .padding(end = 10.dp),
                            content = {
                                if (!teamVM.isEditing)
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Edit Project Info"
                                    )
                                else
                                    Icon(
                                        painter = painterResource(id = R.drawable.round_save_24),
                                        contentDescription = "Save Changes",
                                    )
                            }
                        )
                    }

                    1 -> {}

                    2 -> {
                        Row(
                            modifier = Modifier
                                .height(60.dp)
                                .fillMaxWidth()
                                .padding(start = 45.dp, end = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { onMessageTextChange(it) },
                                shape = RoundedCornerShape(20.dp),
                                maxLines = 1,
                                placeholder = {
                                    Text(
                                        text = "Write a message...",
                                        fontStyle = FontStyle.Italic
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            FloatingActionButton(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        teamVM.sendMessage(
                                            messageText,
                                        )
                                        //keyboardController?.hide()
                                        onMessageTextChange("")
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.chat_paste_go_24dp),
                                    contentDescription = null
                                )
                            }

                        }
                    }

                    3 -> {}
                    4 -> {
                        FloatingActionButton(
                            onClick = {
                                if (!teamVM.isWritingFeedback)
                                    teamVM.toggleIsWritingFeedback()
                                else {
                                    //projectVM.validate()
                                }
                            },
                            modifier = Modifier
                                .padding(end = 10.dp),
                            content = {
                                if (!teamVM.isWritingFeedback)
                                    Icon(
                                        painter = painterResource(R.drawable.outline_feedback_24),
                                        contentDescription = "Add a Feedback",
                                    )
                            }
                        )
                    }

                    5 -> {}
                }
            }

        ) {
            HorizontalPager(state = pagerState, pageSpacing = 10.dp) {
                when (it) {
                    0 -> {
                        TeamInfo(teamVM = teamVM, snackbarHostState = snackbarHostState)
                    }

                    1 -> {
                        TeamProjects(actions = actions, teamVM = teamVM)
                    }

                    2 -> {
                        TeamChat(
                            actions = actions,
                            teamVm = teamVM,
                            search = search,
                            onSearchChange = onSearchChange,
                            query = query,
                            isQuerying = isQuerying,
                            onQueryChange = onQueryChange,
                        )
                    }

                    3 -> {
                        TeamMembers(actions = actions, teamVM = teamVM)
                    }

                    4 -> {
                        TeamFeedbacks(actions = actions, teamVM = teamVM)
                    }

                    5 -> {
                        TeamAchievement(actions = actions, teamVM = teamVM)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PortraitViewTeam(
    actions: Actions,
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
    isQuerying: () -> Boolean,
    onQueryChange: (String) -> Unit,
    pagerState: PagerState,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    teamVM: TeamViewModel,
) {

    val snackbarHostState = remember { SnackbarHostState() }

    Theme(
        applyToStatusBar = true,
        color = teamVM.teamColor
    ) {
        AppSurface(
            orientation = Orientation.PORTRAIT,
            actions = actions,
            title = teamVM.teamName,
            snackbarHostState = snackbarHostState,
            tabActions = {
                val items = listOf(
                    TabItem.TeamInfo,
                    TabItem.TeamProject,
                    TabItem.TeamChat,
                    TabItem.TeamMembers,
                    TabItem.TeamFeedbacks,
                    TabItem.TeamAchievement
                )
                ScrollableTab(
                    orientation = Orientation.PORTRAIT,
                    items = items,
                    pagerState = pagerState,
                    selected = pagerState.currentPage,
                )
            },
            leadingTopBarActions = {
                IconButton(
                    onClick = { actions.navCont.popBackStack() },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
                }
            },
            trailingTopBarActions = {
                TeamActions(teamId = teamVM.teamId)
                IconButton(onClick = { onSearchChange(true) }) {
                    Icon(Icons.Rounded.Search, contentDescription = "Search a comment")
                }
            },
            floatingActionButton = {
                when (pagerState.currentPage) {

                    0 -> {
                        if(teamVM.admin.contains(profileViewModel.userId) && teamVM.uploadStatus !is UploadStatus.Progress)
                        FloatingActionButton(
                            onClick = {
                                if (!teamVM.isEditing)
                                    teamVM.toggleEdit()
                                else {
                                    teamVM.checkAll()
                                }
                            },
                            modifier = Modifier,
                            content = {
                                if (!teamVM.isEditing)
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Edit Project Info"
                                    )
                                else
                                    Icon(
                                        painterResource(id = R.drawable.round_save_24),
                                        contentDescription = "Save Changes",
                                    )
                            }
                        )
                    }

                    1 -> {}

                    2 -> {
                        Row(
                            modifier = Modifier
                                .height(60.dp)
                                .fillMaxWidth()
                                .padding(start = 45.dp, end = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { onMessageTextChange(it) },
                                shape = RoundedCornerShape(20.dp),
                                maxLines = 1,
                                placeholder = {
                                    Text(
                                        text = "Write a message...",
                                        fontStyle = FontStyle.Italic
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            FloatingActionButton(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        teamVM.sendMessage(
                                            messageText,
                                        )
                                        //keyboardController?.hide()
                                        onMessageTextChange("")
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.chat_paste_go_24dp),
                                    contentDescription = null
                                )
                            }

                        }
                    }

                    3 -> {

                    }

                    4 -> {
                        FloatingActionButton(
                            onClick = {
                                if (!teamVM.isWritingFeedback)
                                    teamVM.toggleIsWritingFeedback()
                                else {
                                    //projectVM.validate()
                                }
                            },
                            modifier = Modifier,
                            content = {
                                if (!teamVM.isWritingFeedback)
                                    Icon(
                                        painter = painterResource(R.drawable.outline_feedback_24),
                                        contentDescription = "Add a Feedback"
                                    )
                            }
                        )
                    }

                    5 -> {}
                    else -> {}
                }
            }

        ) {
            HorizontalPager(state = pagerState, pageSpacing = 10.dp) {
                when (it) {
                    0 -> {
                        TeamInfo(teamVM = teamVM, snackbarHostState = snackbarHostState)
                    }

                    1 -> {
                        TeamProjects(actions = actions, teamVM = teamVM)
                    }

                    2 -> {
                        TeamChat(
                            actions = actions,
                            teamVm = teamVM,
                            search = search,
                            onSearchChange = onSearchChange,
                            query = query,
                            isQuerying = isQuerying,
                            onQueryChange = onQueryChange,
                        )
                    }

                    3 -> {
                        TeamMembers(actions = actions, teamVM = teamVM)
                    }

                    4 -> {
                        TeamFeedbacks(actions = actions, teamVM = teamVM)
                    }

                    5 -> {
                        TeamAchievement(actions = actions, teamVM = teamVM)
                    }

                    else -> {}
                }
            }
        }
    }
}