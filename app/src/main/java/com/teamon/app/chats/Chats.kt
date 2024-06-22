package com.teamon.app.chats

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.teamon.app.profileViewModel
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.Theme
//import com.teamon.app.utils.ui.theme.UserProfileTheme

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.teamon.app.Actions
import com.teamon.app.chatsViewModel
import com.teamon.app.myteams.TeamCard
import com.teamon.app.teamOnViewModel
import com.teamon.app.teamsViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Chat
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.toProjectColor
import com.teamon.app.utils.themes.teamon.TeamOnTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID


@Composable
fun ChatsView(actions: Actions, userId: String?, teamId: String?) {
    if(userId != null && teamId != null) actions.openPersonalChat(userId, teamId)
    Theme(color = profileViewModel.color, applyToStatusBar = true) {

        var landscape by remember { mutableStateOf(false) }
        landscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        var isStartingNewChat by remember { mutableStateOf(false) }
        var newChatTeamId by remember { mutableStateOf(UUID.randomUUID().toString()) }

        val startNewChat = { teamId: String ->
            isStartingNewChat = true
            newChatTeamId = teamId
        }
        //TODO: ToggleNewChat with teamId :? Int = null
        val onNewChatStarted = {
            isStartingNewChat = false
            newChatTeamId = UUID.randomUUID().toString()
        }

        if (landscape) LandscapeView(
            actions = actions,
            startNewChat = startNewChat,
            isStartingNewChat = isStartingNewChat,
            newChatTeamId = newChatTeamId,
            onNewChatStarted = onNewChatStarted,
        ) else PortraitView(
            actions = actions,
            startNewChat = startNewChat,
            isStartingNewChat = isStartingNewChat,
            newChatTeamId = newChatTeamId,
            onNewChatStarted = onNewChatStarted,
        )
    }
}

@Composable
fun LandscapeView(
    actions: Actions,
    isStartingNewChat: Boolean,
    newChatTeamId: String,
    startNewChat: (String) -> Unit,
    onNewChatStarted: () -> Unit,
) {

    val teams by teamsViewModel!!.getTeams().collectAsState(initial = emptyMap())
    val ownTeamsIds =
        teams.values.filter { it.users.any { member -> member == profileViewModel!!.userId } }
            .map { it.teamId }
            .toList()

    AppSurface(
        orientation = Orientation.LANDSCAPE,
        actions = actions,
        title = "Chats",
        trailingTopBarActions = {},
    ) {
        if (ownTeamsIds.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No Available Chats.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic
                )
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = 350.dp),
                contentPadding = PaddingValues(8.dp),
                content = {
                    itemsIndexed(ownTeamsIds) { index, teamId ->
                        AnimatedItem(index = index) {

                            Chats(
                                teamId,
                                actions,
                                startNewChat = startNewChat,
                            )
                        }
                        //ChatElevatedCard(teamChat = fake_team_chat, actions = actions)
                    }
                }
            )
            if (isStartingNewChat) {
                NewChatDialog(
                    onDismissRequest = { onNewChatStarted() },
                    actions = actions,
                    teamId = newChatTeamId
                )
            }

        }

    }
}

@Composable
fun PortraitView(
    actions: Actions,
    isStartingNewChat: Boolean,
    newChatTeamId: String,
    startNewChat: (String) -> Unit,
    onNewChatStarted: () -> Unit,
) {
    val teams by teamsViewModel!!.getTeams().collectAsState(initial = emptyMap())
    val ownTeamsIds =
        teams.values.filter { it.users.any { member -> member == profileViewModel!!.userId } }
            .map { it.teamId }
            .toList()

    AppSurface(
        orientation =
        Orientation.PORTRAIT,
        actions = actions,
        title = "Chats"
    ) {
        if (ownTeamsIds.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No Available Chats.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 350.dp),
                contentPadding = PaddingValues(8.dp),
                content = {
                    itemsIndexed(ownTeamsIds) { index, teamId ->
                        AnimatedItem(index = index) {

                            Chats(
                                teamId,
                                actions,
                                startNewChat = startNewChat,
                            )
                        }
                        //ChatElevatedCard(teamChat = fake_team_chat, actions = actions)
                    }
                }
            )
            if (isStartingNewChat) {
                NewChatDialog(
                    onDismissRequest = { onNewChatStarted() },
                    actions = actions,
                    teamId = newChatTeamId
                )
            }
        }

    }
}

