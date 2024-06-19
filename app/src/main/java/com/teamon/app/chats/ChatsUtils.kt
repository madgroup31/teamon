package com.teamon.app.chats

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.chatsViewModel
import com.teamon.app.profileViewModel
import com.teamon.app.teamsViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Message
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.asCompactPastRelativeDateTime


@Composable
fun Chats(  //TODO: Move from here
    teamId: String,
    actions: Actions,
    startNewChat: (String) -> Unit,
    //teamName: String,
    //personalChats: List<Chat>,
    //unreadTeamChatMessages: Int
) {

    val team by teamsViewModel!!.getTeam(teamId).collectAsState(initial = Team())
    val userChats by chatsViewModel!!.getUserChats(teamId).collectAsState(initial = emptyMap())

    val unreadMessages by chatsViewModel!!.getUnreadMessages(teamId).collectAsState(initial = emptyMap())

    Log.d("chat", "userChats: $userChats")
    Log.d("chat", "unreadMessages: $unreadMessages")

    var isExpanded by remember { mutableStateOf(true) }

    Theme(
        applyToStatusBar = false,
        color = team.color
    ) {
        ElevatedCard(
            //colors = CardDefaults.elevatedCardColors(color.toColor()),
            shape = RoundedCornerShape(20.dp),
            modifier =
            if (isExpanded) {
                Modifier
                    .padding(8.dp)
                    .height(
                        if (userChats.size > 2) 448.dp
                        else (userChats.size * 80 + 225).dp
                    )
                    .verticalScroll(rememberScrollState())
                    //.wrapContentHeight()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        RoundedCornerShape(20.dp)
                    )
                    .fillMaxWidth()
            } else {
                Modifier
                    .padding(8.dp)
                    //.verticalScroll(rememberScrollState())
                    //.height(height)
                    .wrapContentHeight()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        RoundedCornerShape(20.dp)
                    )
                    .fillMaxWidth()
            },
            //.heightIn(max = 350.dp) ,
            content = {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                )
                {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row {
                            TeamOnImage(
                                modifier = Modifier
                                    .size(50.dp).clip(CircleShape),
                                source = team.imageSource,
                                name = team.name,
                                surname = "",
                                uri = team.image.toUri(),
                                color = team.color,
                                description = "Team image"
                            )
                        }
                        Row {
                            TextButton(
                                onClick = { actions.openTeam(teamId) },
                                modifier = Modifier,
                            )
                            {
                                Text(
                                    text = team.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 2.dp)
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                ) {
                    ElevatedButton(
                        onClick = { actions.openTeamChat(teamId) },
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .fillMaxWidth(),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 10.dp),
                        colors = ButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        content = {
                            BadgedBox(
                                badge = {
                                    /*if (unreadTeamChatMessages > 0) {
                                        Badge(modifier = Modifier.offset(x = 10.dp)) {
                                            Text(
                                                text = unreadTeamChatMessages.toString(),
                                            )
                                        }
                                    }

                                     */
                                }
                            ) {
                                Text(
                                    text = "Team Chat",
                                )
                            }
                        }
                    )
                }
                if (!isExpanded) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .wrapContentHeight()
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {

                        TextButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier
                        ) {

                            val nUnreadMessages = unreadMessages.values.sum()
                            val nUnreadChats = unreadMessages.filter { it.value > 0 }.size

                            val annotatedString = if (nUnreadMessages > 0) {
                                buildAnnotatedString {

                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red,
                                            shadow = Shadow(color = Color.Red, blurRadius = 1f),
                                        )
                                    ) {
                                        append(nUnreadMessages.toString())
                                    }
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.secondary,
                                            shadow = Shadow(
                                                color = MaterialTheme.colorScheme.secondary,
                                                blurRadius = 1f
                                            ),
                                        )
                                    ) {
                                        append(
                                            " Unread messages in "
                                        )
                                    }
                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red,
                                            shadow = Shadow(color = Color.Red, blurRadius = 1f),
                                        )
                                    ) {
                                        append(nUnreadChats.toString())
                                    }
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.secondary,
                                            shadow = Shadow(
                                                color = MaterialTheme.colorScheme.secondary,
                                                blurRadius = 1f
                                            ),
                                        )
                                    ) {
                                        append(
                                            " chats "
                                        )
                                    }
                                }
                            } else {
                                buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.secondary,
                                            shadow = Shadow(
                                                color = MaterialTheme.colorScheme.secondary,
                                                blurRadius = 1f
                                            ),
                                        )
                                    ) {
                                        append(
                                            "No unread messages"
                                        )
                                    }
                                }
                            }

                            Text(
                                text = annotatedString,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                )
                {
                    Column(
                        modifier = Modifier
                        //.weight(0.7f),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { startNewChat(teamId) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(horizontal = 0.dp)
                                    .wrapContentWidth()
                            )
                            {
                                Icon(
                                    imageVector = Icons.Filled.AddCircle,
                                    contentDescription = "Start new chat",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }

                            TextButton(
                                onClick = { startNewChat(teamId) },
                                modifier = Modifier,
                            ) {
                                Text(
                                    text = "Start a new chat",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    if (userChats.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                            //.weight(0.3f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { isExpanded = !isExpanded },
                                    modifier = Modifier
                                        .padding(horizontal = 0.dp),

                                    ) {

                                    Text(
                                        text = if (!isExpanded) "Show" else "Hide",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                IconButton(
                                    onClick = { isExpanded = !isExpanded },
                                    modifier = Modifier
                                        .padding(horizontal = 0.dp)
                                        .wrapContentWidth()
                                )
                                {
                                    Icon(
                                        imageVector = if (!isExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                                        contentDescription = "Expand",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
                if (isExpanded) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(horizontal = 8.dp, vertical = 0.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                RoundedCornerShape(20.dp)
                            )
                            .align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .height(if (userChats.size > 2) 215.dp else (userChats.size * 80).dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            userChats
                                .entries
                                //.sortedByDescending { it.key.maxBy { message -> message.timestamp }.timestamp }
                                .forEachIndexed { index, (chatId, chat) ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 0.dp, vertical = 2.dp)
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .verticalScroll(rememberScrollState())
                                        //.border(1.dp, Color.Gray, RoundedCornerShape(20.dp))
                                    )
                                    {
                                        val userId = chat.userIds.first { id -> id != profileViewModel!!.userId }
                                        PersonalChatCard(
                                            actions = actions,
                                            chatId = chatId,
                                            userId = userId,
                                            //userId = profileViewModel!!.userId,
                                            teamId = teamId,
                                            zombie = !team.users.contains(userId),
                                            //userId = chat.userIds.first { id -> id != profileViewModel!!.userId },
                                            //lastMessage = chat.messages.maxBy { message -> message.timestamp },
                                            //unread = chat.messages.count { message -> message.unread },
                                        )
                                    }
                                    if (index != userChats.size - 1) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            HorizontalDivider(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.8f)
                                                    .padding(0.dp),
                                                thickness = 1.dp,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        )
    }
}

@SuppressLint("StateFlowValueCalledInComposition", "SuspiciousIndentation")
@Composable
fun PersonalChatCard(
    actions: Actions,
    chatId: String,
    userId: String,
    teamId: String,
    zombie: Boolean,
    //unread: Int
) {

    val lastMessage by chatsViewModel!!.getLastChatMessage(chatId).collectAsState(initial = Message())
    val unreadMessages by chatsViewModel!!.getUnreadMessagesInChat(chatId).collectAsState(initial = 0)
    val user by usersViewModel!!.getUser(userId).collectAsState(initial = User())

    Log.d("chat", "unread: $unreadMessages")

    Row(
        modifier = Modifier
            .padding(top = 4.dp),
        //.border(1.dp, Color.Gray, RoundedCornerShape(20.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(2f)
        )
        {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val selectedNavItem =
                    actions.navCont.currentBackStackEntry?.destination?.route?.split("/")?.first()
                        .toString()
                Box(
                    modifier = Modifier
                        .clickable(onClick = {
                            //if(!zombie)
                                actions.openProfile(selectedNavItem, userId)
                        })
                        .size(50.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    TeamOnImage(
                        modifier = Modifier
                            .size(50.dp)
                            .alpha(if (zombie) 0.5f else 1f)
                            .clip(CircleShape),
                        source = user.profileImageSource,
                        uri = user.profileImage?.toUri(),
                        name = user.name,
                        surname = user.surname,
                        color = user.color,
                        description = "User profile image",
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(5f),
            verticalArrangement = Arrangement.Center
        )
        {
            val selectedNavItem =
                actions.navCont.currentBackStackEntry?.destination?.route?.split("/")?.first()
                    .toString()
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            )
            {
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .clickable(onClick = { actions.openProfile(selectedNavItem, userId) })
                ) {
                    Text(
                        text = "${user.name} ${user.surname}",
                        style = MaterialTheme.typography.titleMedium.copy(textDecoration = if (zombie) TextDecoration.LineThrough else TextDecoration.None),
                        fontStyle = if (zombie) FontStyle.Italic else FontStyle.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (lastMessage != null)
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                ) {
                    Text(
                        text = lastMessage.timestamp.asCompactPastRelativeDateTime(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            )
            {
                if (lastMessage != null)
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                        .clickable(onClick = { actions.openPersonalChat(userId, teamId) })
                ) {
                    var content = lastMessage.content
                    if (lastMessage.senderId == profileViewModel!!.userId) {
                        val isMessageRead by chatsViewModel!!.isMessageRead(lastMessage.messageId, userId).collectAsState(initial = false)
                        if (isMessageRead) {
                            content += " ✓✓"
                        }
                        else {
                            content += " ✓"
                        }
                    }
                    Text(
                        text = content,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(0.3f),
                ) {
                    Box {
                        IconButton(
                            onClick = { actions.openPersonalChat(userId, teamId) },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Go to Chat",
                            )
                        }
                        if (unreadMessages > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                            ) {
                                Text(
                                    text = unreadMessages.toString(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewChatDialog(
    onDismissRequest: () -> Unit,
    teamId: String,
    actions: Actions,
) {

    val team by teamsViewModel.getTeam(teamId).collectAsState(initial = Team())

    Theme(
        applyToStatusBar = false,
        color = team.color
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
                        Column(
                            modifier = Modifier
                                .padding(30.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = team.name,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = if (team.users.size > 1) "Select a member to chat with" else "No members to chat with",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    if (team.users.isNotEmpty()) {
                        //teamVm.users.sortedBy { it. }
                        items(team.users.filter { it != profileViewModel.userId }) { member ->
                            PartecipantCard_(
                                userId = member,
                                onUserSelected = {
                                    actions.openPersonalChat(
                                        userId = it,
                                        teamId = team.teamId
                                    )
                                    onDismissRequest()
                                },
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
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
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun PartecipantCard_(
    userId: String,
    onUserSelected: (String) -> Unit,
) {

    //val selectedNavItem =
    //   actions.navCont.currentBackStackEntry?.destination?.route?.split("/")?.first().toString()
    var loading by remember { mutableStateOf(true) }

    val user by usersViewModel!!.getUser(userId).collectAsState(initial = User())

    ListItem(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp),
        headlineContent = {
            Text(
                user.name + " " + user.surname,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        },
        leadingContent = {
            IconButton(onClick = { /*TODO: Open Profile*/ }) {
                TeamOnImage(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                    source = user.profileImageSource,
                    uri = user.profileImage?.toUri(),
                    name = user.name,
                    surname = user.surname,
                    color = user.color,
                    description = "User profile image",
                )
            }
        },
        trailingContent = {
            FilledTonalButton(
                shape = MaterialTheme.shapes.extraLarge,
                onClick = { onUserSelected(user.userId) }) {

                Icon(
                    painter = painterResource(id = R.drawable.outline_comment_24),
                    contentDescription = "Select member to chat with",
                )

            }
        }
    )
}