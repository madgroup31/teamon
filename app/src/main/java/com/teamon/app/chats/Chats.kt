package com.teamon.app.chats


//import com.teamon.app.utils.ui.theme.UserProfileTheme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teamon.app.Actions
import com.teamon.app.profileViewModel
import com.teamon.app.teamsViewModel
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.LoadingOverlay
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.Theme
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

        val teams by teamsViewModel.getTeams().collectAsState(initial = null)
        val ownTeamsIds =
            teams?.values?.filter { it.users.any { member -> member == profileViewModel.userId } }
                ?.map { it.teamId }
                ?.toList()

        if (landscape) LandscapeView(
            actions = actions,
            startNewChat = startNewChat,
            isStartingNewChat = isStartingNewChat,
            data = ownTeamsIds,
            newChatTeamId = newChatTeamId,
            onNewChatStarted = onNewChatStarted,
        ) else PortraitView(
            actions = actions,
            startNewChat = startNewChat,
            isStartingNewChat = isStartingNewChat,
            data = ownTeamsIds,
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
    data: List<String>?,
    onNewChatStarted: () -> Unit,
) {
    AppSurface(
        orientation = Orientation.LANDSCAPE,
        actions = actions,
        title = "Chats",
        trailingTopBarActions = {},
    ) {
        if(data == null)
            LoadingOverlay(isLoading = true)
        else
        if (data.isEmpty()) {
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
                    itemsIndexed(data) { index, teamId ->
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
    data: List<String>?,
    startNewChat: (String) -> Unit,
    onNewChatStarted: () -> Unit,
) {

    AppSurface(
        orientation =
        Orientation.PORTRAIT,
        actions = actions,
        title = "Chats"
    ) {
        if(data == null)
            LoadingOverlay(isLoading = true)
        else
        if (data.isEmpty()) {
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
                    itemsIndexed(data) { index, teamId ->
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

