package com.teamon.app.teams.chat

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.teamon.app.chats.DayHeader
import com.teamon.app.chats.ReceivedTeamMessageCard
import com.teamon.app.chats.SentTeamMessageCard
import com.teamon.app.profileViewModel
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.asPastRelativeDate
import com.teamon.app.utils.viewmodels.TeamViewModel

@Composable
fun TeamChat(
    actions: Actions,
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
    isQuerying: () -> Boolean,
    onQueryChange: (String) -> Unit,
    teamVm: TeamViewModel,
) {

    var landscape by remember { mutableStateOf(false) }
    landscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (landscape) LandscapeTeamChatView(
        actions = actions,
        teamVm = teamVm,
        search = search,
        onSearchChange = onSearchChange,
        query = query,
        isQuerying = isQuerying,
        onQueryChange = onQueryChange,
    )
    else PortraitTeamChatView(
        actions = actions,
        teamVm = teamVm,
        search = search,
        onSearchChange = onSearchChange,
        query = query,
        isQuerying = isQuerying,
        onQueryChange = onQueryChange,
    )

}


@Composable
fun PortraitTeamChatView(
    actions: Actions,
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
    isQuerying: () -> Boolean,
    onQueryChange: (String) -> Unit,
    teamVm: TeamViewModel,
) {

    val listState = rememberLazyListState()

    Column {
        if (search) {
            Row {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                ) {
                    OutlinedTextField(
                        value = query,
                        maxLines = 1,
                        shape = RoundedCornerShape(20.dp),
                        onValueChange = { onQueryChange(it) },
                        label = { Text("Search in messages") },
                        trailingIcon = {
                            IconButton(onClick = {
                                onSearchChange(false)
                                onQueryChange("")
                            }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Stop Search",
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 8.dp)
                    )
                }
            }
        }
        Row {
            if (teamVm.messages.isNotEmpty()) {
                LaunchedEffect(teamVm.messages.size) {
                    listState.animateScrollToItem(teamVm.messages.size - 1, 2)
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(5.dp)
                        .padding(bottom = 80.dp)
                ) {
                    itemsIndexed(teamVm.messages.sortedBy { it.timestamp }
                        .groupBy { it.timestamp.asPastRelativeDate() }.values.toList()) { index, it ->
                        AnimatedItem(index = index) {
                            Column {
                                DayHeader(it.first().timestamp.asPastRelativeDate())
                                it.forEach { message ->
                                    when (message.senderId) {
                                        profileViewModel.userId -> SentTeamMessageCard(
                                            message = message,
                                            query = query,
                                            isQuerying = isQuerying,
                                            partecipants = teamVm.users.size
                                        )

                                        else ->
                                            ReceivedTeamMessageCard(
                                                message = message,
                                                query = query,
                                                isQuerying = isQuerying,
                                                actions = actions,
                                            )

                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedItem(index = 1) {

                        Text(
                            text = "No messages yet in this chat!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

        }


    }
}


@Composable
fun LandscapeTeamChatView(
    actions: Actions,
    teamVm: TeamViewModel,
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
    isQuerying: () -> Boolean,
    onQueryChange: (String) -> Unit,
) {

    val listState = rememberLazyListState()
    Column {
        if (search) {
            Row {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                ) {
                    OutlinedTextField(
                        value = query,
                        maxLines = 1,
                        shape = RoundedCornerShape(20.dp),
                        onValueChange = { onQueryChange(it) },
                        label = { Text("Search in messages") },
                        trailingIcon = {
                            IconButton(onClick = {
                                onSearchChange(false)
                                onQueryChange("")
                            }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Stop Search",
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 8.dp)
                    )
                }
            }
        }
        Row {
            if (teamVm.messages.isNotEmpty()) {
                LaunchedEffect(teamVm.messages.size) {
                    listState.animateScrollToItem(teamVm.messages.size - 1, 2)
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(5.dp)
                        .padding(bottom = 80.dp)
                ) {
                    itemsIndexed(teamVm.messages.sortedBy { it.timestamp }
                        .groupBy { it.timestamp.asPastRelativeDate() }.values.toList()) { index, it ->
                        AnimatedItem(index = index) {
                            Column {
                                DayHeader(it.first().timestamp.asPastRelativeDate())
                                it.forEach { message ->
                                    when (message.senderId) {
                                        profileViewModel.userId -> SentTeamMessageCard(
                                            message = message,
                                            query = query,
                                            isQuerying = isQuerying,
                                            partecipants = teamVm.users.size
                                        )

                                        else ->
                                            ReceivedTeamMessageCard(
                                                message = message,
                                                query = query,
                                                isQuerying = isQuerying,
                                                actions = actions,
                                            )

                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedItem(index = 1) {

                        Text(
                            text = "No messages yet in this chat!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

        }
    }
}