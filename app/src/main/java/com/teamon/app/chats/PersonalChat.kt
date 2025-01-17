package com.teamon.app.chats

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.prefs
import com.teamon.app.profileViewModel
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.ChatDeleteDialog
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.asPastRelativeDate
import com.teamon.app.utils.viewmodels.ChatViewModel

@Composable
fun PersonalChatView(actions: Actions, chatVm: ChatViewModel) {

    Theme(color = profileViewModel.color, applyToStatusBar = true) {

        var landscape by remember { mutableStateOf(false) }
        landscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        var newMessage by rememberSaveable { mutableStateOf("") }

        val onMessageChange = { message: String ->
            newMessage = message
        }

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
        val onConfirmDeleteChat: () -> Unit = {
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
            chatVm = chatVm,
            newMessage = newMessage,
            onMessageChange = onMessageChange
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

@OptIn(ExperimentalFoundationApi::class)
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
    var textBoxHeight by remember { mutableIntStateOf(0) }
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
                    Column {
                        TeamOnImage(
                            modifier = Modifier
                                .size(32.dp)
                                .alpha(if (zombie) 0.5f else 1f)
                                .clip(CircleShape),
                            source = addressee.profileImageSource,
                            name = addressee.name,
                            surname = addressee.surname,
                            uri = addressee.profileImage?.toUri(),
                            color = addressee.color,
                            description =
                            addressee.name + " " + addressee.surname + " profile picture"
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier) {
                        Text(
                            text = addressee.name + " " + addressee.surname,
                            style = MaterialTheme.typography.titleLarge.copy(textDecoration = if (zombie) TextDecoration.LineThrough else TextDecoration.None),
                            fontStyle = if (zombie) FontStyle.Italic else FontStyle.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = addressee.nickname,
                            style = MaterialTheme.typography.bodySmall.copy(textDecoration = if (zombie) TextDecoration.LineThrough else TextDecoration.None),
                            fontStyle = if (zombie) FontStyle.Italic else FontStyle.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                    }

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
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth(0.9f)
                                .padding(start = 35.dp)
                                .align(Alignment.CenterHorizontally)
                                .onGloballyPositioned { textBoxHeight = it.size.height },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newMessage,
                                onValueChange = { onMessageChange(it) },
                                shape = RoundedCornerShape(20.dp),
                                maxLines = 5,
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
                                    Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .padding(start = 45.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "This user is not part of the team anymore.",
                            style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        ) {
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
                    val animate = prefs.getBoolean(
                        "animate",
                        true
                    )
                    if (chatVm.messages.isNotEmpty()) {
                        LaunchedEffect(chatVm.messages.size) {
                            if(animate)
                            listState.animateScrollToItem(chatVm.messages.size - 1, 2)
                            else
                                listState.scrollToItem(chatVm.messages.size - 1, 2)
                        }
                        val height = with(LocalDensity.current) {
                            textBoxHeight.toDp().value.toInt().dp + 15.dp
                        }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                //.fillMaxHeight(0.8f)
                                .padding(5.dp)
                                .padding(bottom = height)
                        ) {
                            val messages = chatVm.messages.sortedBy { it.timestamp }
                                .groupBy { it.timestamp.asPastRelativeDate() }

                            messages.forEach { (date, messagesForDate) ->
                                stickyHeader(date, String) {
                                    DayHeader(date)
                                }

                                items(messagesForDate, key = { it.messageId }) { message ->
                                    Box(modifier = if(animate) Modifier.animateItemPlacement() else Modifier) {
                                        when (message.senderId) {
                                            profileViewModel.userId -> SentPersonalMessageCard(
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
}


@OptIn(ExperimentalFoundationApi::class)
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
) {
    val team by chatVm.team.collectAsState(initial = Team())
    val addressee by chatVm.addressee.collectAsState(initial = User())
    var textBoxHeight by remember { mutableIntStateOf(0) }
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
                    Column {
                        TeamOnImage(
                            modifier = Modifier
                                .size(32.dp)
                                .alpha(if (zombie) 0.5f else 1f)
                                .clip(CircleShape),
                            source = addressee.profileImageSource,
                            name = addressee.name,
                            surname = addressee.surname,
                            uri = addressee.profileImage?.toUri(),
                            color = addressee.color,
                            description =
                            addressee.name + " " + addressee.surname + " profile picture"
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier) {
                        Text(
                            text = addressee.name + " " + addressee.surname,
                            style = MaterialTheme.typography.titleLarge.copy(textDecoration = if (zombie) TextDecoration.LineThrough else TextDecoration.None),
                            fontStyle = if (zombie) FontStyle.Italic else FontStyle.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = addressee.nickname,
                            style = MaterialTheme.typography.bodySmall.copy(textDecoration = if (zombie) TextDecoration.LineThrough else TextDecoration.None),
                            fontStyle = if (zombie) FontStyle.Italic else FontStyle.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                    }

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
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth(0.9f)
                                .padding(start = 35.dp)
                                .align(Alignment.CenterHorizontally)
                                .onGloballyPositioned { textBoxHeight = it.size.height },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newMessage,
                                onValueChange = { onMessageChange(it) },
                                shape = RoundedCornerShape(20.dp),
                                maxLines = 2,
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
                                    Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .padding(start = 45.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "This user is not part of the team anymore.",
                            style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        ) {
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
                    val animate = prefs.getBoolean("animate", true)
                    if (chatVm.messages.isNotEmpty()) {
                        LaunchedEffect(chatVm.messages.size) {
                            if(animate)
                            listState.animateScrollToItem(chatVm.messages.size - 1, 2)
                            else
                                listState.scrollToItem(chatVm.messages.size-1, 2)
                        }
                        val height = with(LocalDensity.current) {
                            textBoxHeight.toDp().value.toInt().dp + 15.dp
                        }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp)
                                .padding(bottom = height)
                        ) {
                            val messages = chatVm.messages.sortedBy { it.timestamp }
                                .groupBy { it.timestamp.asPastRelativeDate() }

                            messages.forEach { (date, messagesForDate) ->
                                stickyHeader(date, String) {
                                    DayHeader(date)
                                }

                                items(messagesForDate, key = { it.messageId }) { message ->
                                    Box(modifier = if(animate) Modifier.animateItemPlacement() else Modifier) {
                                    when (message.senderId) {
                                            profileViewModel.userId -> SentPersonalMessageCard(
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
}