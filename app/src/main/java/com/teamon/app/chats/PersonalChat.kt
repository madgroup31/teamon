package com.teamon.app.chats

import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.chatsViewModel
import com.teamon.app.profileViewModel
import com.teamon.app.teamsViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.ChatDeleteDialog
import com.teamon.app.utils.graphics.MessageDeleteDialog
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.asPastRelativeDate
import com.teamon.app.utils.themes.teamon.TeamOnTheme
import com.teamon.app.utils.viewmodels.ChatViewModel
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PersonalChatView(actions: Actions, chatVm: ChatViewModel) {

    TeamOnTheme(applyToStatusBar = true) {

        var landscape by remember { mutableStateOf(false) }
        landscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        var newMessage by rememberSaveable { mutableStateOf("") }

        val onMessageChange = { message: String ->
            newMessage = message
        }

        /*
        modified: Boolean,
        onModifiedChange: (Boolean) -> Unit,
        search: Boolean,
        onSearchChange: (Boolean) -> Unit,
        query: String,
        isQuerying: () -> Boolean,
        onQueryChange: (String) -> Unit,
         */

        var search by remember { mutableStateOf(false) }
        val onSearchChange = { it: Boolean ->
            search = it
        }

        var query by remember { mutableStateOf("") }
        val onQueryChange = { it: String ->
            query = it
        }
        val isQuerying = {
            query.isNotEmpty()
        }

        var isShowingDeleteChat by remember { mutableStateOf(false) }
        val toggleShowingDeleteChat = {
            isShowingDeleteChat = !isShowingDeleteChat
        }
        val onConfirmDeleteChat : () -> Unit = {
            chatVm.deleteChat()
            isShowingDeleteChat = false
            actions.navCont.popBackStack()
        }

        if (landscape) LandscapeView(
            actions = actions,
            search = search,
            onSearchChange = onSearchChange,
            query = query,
            isQuerying = isQuerying,
            onQueryChange = onQueryChange,
            newMessage = newMessage,
            onMessageChange = onMessageChange,
            chatVm = chatVm,
            isShowingDeleteChat = isShowingDeleteChat,
            toggleShowingDeleteChat = toggleShowingDeleteChat,
            onConfirmDeleteChat = onConfirmDeleteChat
        )
        else PortraitView(
            actions = actions,
            search = search,
            onSearchChange = onSearchChange,
            query = query,
            isQuerying = isQuerying,
            onQueryChange = onQueryChange,
            newMessage = newMessage,
            onMessageChange = onMessageChange,
            chatVm = chatVm,
            isShowingDeleteChat = isShowingDeleteChat,
            toggleShowingDeleteChat = toggleShowingDeleteChat,
            onConfirmDeleteChat = onConfirmDeleteChat
        )
    }

}

@Composable
fun PortraitView(
    actions: Actions,
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
    isQuerying: () -> Boolean,
    onQueryChange: (String) -> Unit,
    chatVm: ChatViewModel,
    newMessage: String,
    onMessageChange: (String) -> Unit,
    isShowingDeleteChat: Boolean,
    toggleShowingDeleteChat: () -> Unit,
    onConfirmDeleteChat: () -> Unit,
) {

    val team by chatVm.team.collectAsState(initial = Team())
    val addressee by chatVm.addressee.collectAsState(initial = User())

    val listState = rememberLazyListState()

    val zombie = !team.users.contains(addressee.userId)

    Theme(
        applyToStatusBar = true,
        color = team.color,
    ) {
        AppSurface(
            orientation = Orientation.PORTRAIT,
            actions = actions,
            title = addressee.name + " " + addressee.surname,
            composableTitle = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamOnImage(
                        modifier = Modifier
                            .size(32.dp)
                            .alpha(if (zombie) 0.5f else 1f)
                            .clip(CircleShape),
                        source = addressee.profileImageSource,
                        name = addressee.name,
                        surname = addressee.surname,
                        uri = addressee.profileImage?.toUri(),
                        description =
                        addressee.name + " " + addressee.surname + " profile picture"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = addressee.name + " " + addressee.surname,
                        style = MaterialTheme.typography.titleLarge.copy(textDecoration = if (zombie) TextDecoration.LineThrough else TextDecoration.None),
                        fontStyle = if (zombie) FontStyle.Italic else FontStyle.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            leadingTopBarActions = {

                IconButton(onClick = { actions.navCont.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
                }

            },
            trailingTopBarActions = {
                IconButton(onClick = { onSearchChange(true) }) {
                    Icon(Icons.Rounded.Search, contentDescription = "Search a comment")
                }
            },
            floatingActionButton = {
                if (!zombie) {
                    Row(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .padding(start = 45.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMessage,
                            onValueChange = { onMessageChange(it) },
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
                                if (newMessage.isNotBlank()) {
                                    chatVm.sendMessage(
                                        newMessage,
                                    )
                                    //keyboardController?.hide()
                                    onMessageChange("")
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
                else {
                    Row(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .padding(start = 45.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "This user is not part of the team",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        FloatingActionButton(
                            onClick = {
                                toggleShowingDeleteChat()
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.chat_paste_go_24dp),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        ) {

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
                if (chatVm.messages.isNotEmpty()) {
                    LaunchedEffect(chatVm.messages.size) {
                        listState.animateScrollToItem(chatVm.messages.size - 1, 2)
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            //.fillMaxHeight(0.8f)
                            .padding(5.dp)
                            .padding(bottom = 80.dp)
                    ) {
                        items(chatVm.messages.sortedBy { it.timestamp }
                            .groupBy { it.timestamp.asPastRelativeDate() }.values.toList()) {
                            DayHeader(it.first().timestamp.asPastRelativeDate())
                            it.forEach { message ->
                                when (message.senderId) {
                                    profileViewModel!!.userId -> SentPersonalMessageCard(
                                        message = message,
                                        query = query,
                                        isQuerying = isQuerying,
                                    )

                                    else -> ReceivedPersonalMessageCard(
                                        message = message,
                                        query = query,
                                        isQuerying = isQuerying,
                                    )
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
                        Text(
                            text = "No messages yet in this chat!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

            }
            if (isShowingDeleteChat) {
                ChatDeleteDialog(
                    onDismiss = { toggleShowingDeleteChat() },
                    onConfirm = { onConfirmDeleteChat() }
                )
            }
        }
    }
}


@Composable
fun LandscapeView(
    actions: Actions,
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
    isQuerying: () -> Boolean,
    onQueryChange: (String) -> Unit,
    chatVm: ChatViewModel,
    newMessage: String,
    onMessageChange: (String) -> Unit,
    isShowingDeleteChat: Boolean,
    toggleShowingDeleteChat: () -> Unit,
    onConfirmDeleteChat: () -> Unit,
) {
    val team by chatVm.team.collectAsState(initial = Team())
    val addressee by chatVm.addressee.collectAsState(initial = User())

    val listState = rememberLazyListState()

    val zombie = !team.users.contains(addressee.userId)
    Log.d("chat", "zombie: $zombie")

    Theme(
        applyToStatusBar = true,
        color = team.color,
    ) {
        AppSurface(
            orientation = Orientation.LANDSCAPE,
            actions = actions,
            title = addressee.name + " " + addressee.surname,
            composableTitle = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamOnImage(
                        modifier = Modifier
                            .size(32.dp)
                            .alpha(if (zombie) 0.5f else 1f)
                            .clip(CircleShape),
                        source = addressee.profileImageSource,
                        name = addressee.name,
                        surname = addressee.surname,
                        uri = addressee.profileImage?.toUri(),
                        description =
                        addressee.name + " " + addressee.surname + " profile picture"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = addressee.name + " " + addressee.surname,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            leadingTopBarActions = {

                IconButton(onClick = { actions.navCont.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
                }

            },
            trailingTopBarActions = {
                IconButton(onClick = { onSearchChange(true) }) {
                    Icon(Icons.Rounded.Search, contentDescription = "Search a comment")
                }
            },
            floatingActionButton = {
                if (!zombie) {
                    Row(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .padding(start = 45.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMessage,
                            onValueChange = { onMessageChange(it) },
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
                                if (newMessage.isNotBlank()) {
                                    chatVm.sendMessage(
                                        newMessage,
                                    )
                                    //keyboardController?.hide()
                                    onMessageChange("")
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
                else {
                    Row(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .padding(start = 45.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "This user is not part of the team",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        ) {

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
                if (chatVm.messages.isNotEmpty()) {
                    LaunchedEffect(chatVm.messages.size) {
                        listState.animateScrollToItem(chatVm.messages.size - 1, 2)
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            //.fillMaxHeight(0.8f)
                            .padding(5.dp)
                            .padding(bottom = 80.dp)
                    ) {
                        items(chatVm.messages.sortedBy { it.timestamp }
                            .groupBy { it.timestamp.asPastRelativeDate() }.values.toList()) {
                            DayHeader(it.first().timestamp.asPastRelativeDate())
                            it.forEach { message ->
                                when (message.senderId) {
                                    profileViewModel!!.userId -> SentPersonalMessageCard(
                                        message = message,
                                        query = query,
                                        isQuerying = isQuerying,
                                    )

                                    else -> ReceivedPersonalMessageCard(
                                        message = message,
                                        query = query,
                                        isQuerying = isQuerying,
                                    )
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