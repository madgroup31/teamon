package com.teamon.app.chats

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.chatsViewModel
import com.teamon.app.profileViewModel
import com.teamon.app.teamsViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Chat
import com.teamon.app.utils.classes.Message
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.asCompactPastRelativeDateTime


@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun Chats(  //TODO: Move from here
    teamId: String,
    actions: Actions,
    startNewChat: (String) -> Unit,
) {

    val team by teamsViewModel.getTeam(teamId).collectAsState(initial = Team())

    Theme(
        applyToStatusBar = false,
        color = team.color
    ) {
        RecursiveChatsBox(teamId = teamId, startNewChat = startNewChat, actions = actions)
    }
}

@SuppressLint("StateFlowValueCalledInComposition", "SuspiciousIndentation")
@Composable
fun PersonalChatCard(
    actions: Actions,
    chatId: String,
    initialLastMessage: Message,
    team: Team,
    setView: ((Boolean) -> Unit)? = null,
    elevation: Dp
) {
    val lastMessage by chatsViewModel.getLastChatMessage(chatId).collectAsState(initial = initialLastMessage)
    val unreadMessages by chatsViewModel.getUnreadMessagesInChat(chatId).collectAsState(initial = 0)
    val user by chatsViewModel.getCorrespondent(chatId).collectAsState(initial = User())

    val zombie = !team.users.contains(user.userId)

        ElevatedCard(
            elevation = CardDefaults.elevatedCardElevation(elevation),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    RoundedCornerShape(20.dp)
                ),
            onClick = {
                if(setView != null) setView(true)
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            )
            {
                val selectedNavItem =
                    actions.navCont.currentBackStackEntry?.destination?.route?.split("/")?.first()
                        .toString()
                Column(verticalArrangement = Arrangement.Center) {
                    Box {
                    TeamOnImage(
                        modifier = Modifier
                            .size(50.dp)
                            .alpha(if (zombie) 0.5f else 1f)
                            .clip(CircleShape)
                            .clickable {
                                if (setView != null) setView(true) else actions.openProfile(
                                    selectedNavItem,
                                    user.userId
                                )
                            },
                        source = user.profileImageSource,
                        uri = user.profileImage?.toUri(),
                        name = user.name,
                        surname = user.surname,
                        color = user.color,
                        description = "User profile image",
                    )
                    if (team.admin.contains(user.userId))
                        Image(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.BottomEnd),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            painter = painterResource(id = R.drawable.ic_admin),
                            contentDescription = "Admin badge"
                        )
                }
                }


                Spacer(modifier = Modifier.weight(0.3f))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(6f),
                    verticalArrangement = Arrangement.Center
                )
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    )
                    {
                        Text(
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .clickable {
                                    if (setView != null) setView(true) else actions.openProfile(
                                        selectedNavItem,
                                        user.userId
                                    )
                                },
                            text = user.nickname,
                            style = MaterialTheme.typography.titleMedium.copy(textDecoration = if (zombie) TextDecoration.LineThrough else TextDecoration.None),
                            fontStyle = if (zombie) FontStyle.Italic else FontStyle.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )



                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = lastMessage.timestamp.asCompactPastRelativeDateTime(),
                            style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End
                        )

                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                if (setView != null)
                                    setView(true)
                                else
                                    actions.openPersonalChat(
                                        user.userId,
                                        team.teamId
                                    )
                            }),
                        horizontalArrangement = Arrangement.Center,
                    )
                    {
                        Column(
                            modifier = Modifier
                                .weight(0.7f)

                        ) {
                            Row {
                                val content = lastMessage.content
                                Text(
                                    text = content,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 2,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (lastMessage.senderId == profileViewModel.userId) {
                                    val isMessageRead by chatsViewModel.isMessageRead(
                                        lastMessage.messageId,
                                        user.userId
                                    ).collectAsState(initial = false)
                                    if (isMessageRead) {
                                        Box {
                                            Icon(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .alpha(0.75f),
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null
                                            )
                                            Icon(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .offset(x = 5.dp),
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null
                                            )
                                        }
                                    } else {
                                        Icon(
                                            modifier = Modifier.size(18.dp),
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        BadgedBox(badge = {
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
                        }) {
                        IconButton(
                            onClick = { if(setView != null) setView(true) else actions.openPersonalChat(user.userId, team.teamId) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                        ) {
                                Icon(
                                    painter = painterResource(R.drawable.chat_paste_go_24dp),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.Center),
                                    contentDescription = "Go to Chat",
                                )
                            }

                        }

                    }
                }
            }
        }

}

@SuppressLint("StateFlowValueCalledInComposition", "SuspiciousIndentation")
@Composable
fun TeamChatCard(
    actions: Actions,
    team: Team
) {
    val chat by chatsViewModel.getTeamChat(team.teamId).collectAsState(initial = Chat())
    val lastMessage by chatsViewModel.getLastChatMessage(chat.chatId).collectAsState(initial = Message())
    val unreadMessages by chatsViewModel.getUnreadMessagesInChat(chat.chatId).collectAsState(initial = 0)

    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = 30.dp, end = 10.dp, start = 10.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                RoundedCornerShape(20.dp)
            ),
        onClick = {
            actions.openTeamChat(team.teamId)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        )
        {
                Column(verticalArrangement = Arrangement.Center) {
                    Box {
                        TeamOnImage(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .clickable {
                                    actions.openTeam(team.teamId)
                                },
                            source = team.imageSource,
                            uri = team.image.toUri(),
                            name = team.name,
                            surname = "",
                            color = team.color,
                            description = "Team image",
                        )
                    }
                }


                Spacer(modifier = Modifier.weight(0.3f))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(6f),
                    verticalArrangement = Arrangement.Center
                )
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    )
                    {
                        Text(
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .clickable {
                                    actions.openTeam(team.teamId)
                                },
                            text = "Team Chat",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.None),
                            fontStyle = FontStyle.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )



                        Spacer(modifier = Modifier.width(5.dp))
                        if (lastMessage.content.isNotBlank())
                            Text(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                text = lastMessage.timestamp.asCompactPastRelativeDateTime(),
                                style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End
                            )

                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                actions.openTeamChat(team.teamId)
                            }),
                        horizontalArrangement = Arrangement.Center,
                    )
                    {
                        Column(
                            modifier = Modifier
                                .weight(0.7f)

                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (lastMessage.content.isBlank())
                                    Text(
                                        text = "No messages yet",
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 2,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                else {
                                    val sender by usersViewModel.getUser(lastMessage.senderId)
                                        .collectAsState(initial = User())
                                    val zombie = !team.users.contains(sender.userId)
                                    val isCurrentUser = sender.userId == profileViewModel.userId
                                    val senderName = if (isCurrentUser) "Me" else sender.nickname
                                    val messageContent = lastMessage.content

                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(
                                                style = SpanStyle(
                                                    fontWeight = FontWeight.Medium,
                                                    textDecoration = if (zombie) TextDecoration.LineThrough else TextDecoration.None
                                                )
                                            ) {
                                                append("$senderName: ")
                                            }
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                                append(messageContent)
                                            }
                                        },
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 4,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (sender.userId == profileViewModel.userId) {
                                        Spacer(modifier = Modifier.width(5.dp))
                                        val readBy = team.users.size - lastMessage.unread.size - 1
                                        Icon(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .alpha(0.75f),
                                            painter = painterResource(R.drawable.outline_remove_red_eye_24),
                                            contentDescription = null
                                        )
                                        Text(
                                            text = readBy.toString(),
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    BadgedBox(badge = {
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
                    }) {
                        IconButton(
                            onClick = { actions.openTeamChat(team.teamId) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.chat_paste_go_24dp),
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.Center),
                                contentDescription = "Go to Chat",
                            )
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

    val user by usersViewModel.getUser(userId).collectAsState(initial = User())

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