package com.teamon.app.chats

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.graphics.ColorFilter
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
import androidx.compose.ui.unit.times
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

    val team by teamsViewModel.getTeam(teamId).collectAsState(initial = Team())
    //val userChats by chatsViewModel!!.getUserChats(teamId).collectAsState(initial = emptyMap())

    val unreadTeamChatMessages by chatsViewModel.getUnreadTeamChatMessages(teamId).collectAsState(initial = 0)
    val unreadMessages by chatsViewModel.getUnreadMessages(teamId).collectAsState(initial = emptyMap())
    val lastMessages by chatsViewModel.getLastMessages(teamId).collectAsState(initial = null)



    var isExpanded by remember { mutableStateOf(true) }

    Theme(
        applyToStatusBar = false,
        color = team.color
    ) {
        ElevatedCard(
            //colors = CardDefaults.elevatedCardColors(color.toColor()),
            shape = RoundedCornerShape(20.dp),
            modifier =
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
            ,
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row {
                            TeamOnImage(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape),
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
                                modifier = Modifier.align(Alignment.CenterVertically),
                                badge = {
                                    if (unreadTeamChatMessages > 0) {
                                        Badge(modifier = Modifier.offset(x = 10.dp)) {
                                            Text(
                                                text = unreadTeamChatMessages.toString(),
                                            )
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    text = "Group Chat",
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

                            TextButton(
                                onClick = { startNewChat(teamId) },
                                modifier = Modifier,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.round_add_circle_24),
                                    contentDescription = "Start new chat",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Start a new chat",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    if (lastMessages?.isNotEmpty() == true) {
                        Column(
                            modifier = Modifier,
                            horizontalAlignment = Alignment.End
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
                                    Icon(
                                        imageVector = if (!isExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                                        contentDescription = "Contract/Expand",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
                if (isExpanded) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                RoundedCornerShape(20.dp)
                            )
                            .align(Alignment.CenterHorizontally),
                    ) {
                        val height = lastMessages?.let {it.size * 70.dp} ?: 70.dp
                        Column(
                            modifier = Modifier
                                .height(height)
                                .verticalScroll(rememberScrollState())
                        ) {
                            lastMessages?.let { messages ->
                                messages.entries
                                .sortedByDescending { it.value.timestamp }
                                .forEachIndexed { index, (chatId, _) ->
                                    //val userId = chat.userIds.first { id -> id != profileViewModel!!.userId }
                                    PersonalChatCard(
                                        actions = actions,
                                        chatId = chatId,
                                        //userId = userId,
                                        //userId = profileViewModel!!.userId,
                                        team = team,
                                        //lastMessage = message.messageId,
                                        //zombie = !team.users.contains(userId),
                                        //userId = chat.userIds.first { id -> id != profileViewModel!!.userId },
                                        //lastMessage = chat.messages.maxBy { message -> message.timestamp },
                                        //unread = chat.messages.count { message -> message.unread },
                                    )

                                    if (index != messages.size - 1) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                modifier = Modifier
                                                    .fillMaxWidth(0.85f)
                                                    .padding(0.dp),
                                                thickness = 1.dp,
                                            )
                                        }
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
    //lastMessage: Message,
    chatId: String,
    //userId: String,
    team: Team,
    //zombie: Boolean,
    //unread: Int
) {
    val lastMessage by chatsViewModel.getLastChatMessage(chatId).collectAsState(initial = Message())
    val unreadMessages by chatsViewModel.getUnreadMessagesInChat(chatId).collectAsState(initial = 0)
    val user by chatsViewModel.getCorrespondent(chatId).collectAsState(initial = User())

    val zombie = !team.users.contains(user.userId)


    Row(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
            val selectedNavItem =
                actions.navCont.currentBackStackEntry?.destination?.route?.split("/")?.first()
                    .toString()
            Box(contentAlignment = Alignment.Center) {
                TeamOnImage(
                    modifier = Modifier
                        .size(50.dp)
                        .alpha(if (zombie) 0.5f else 1f)
                        .clip(CircleShape)
                        .clickable { actions.openProfile(selectedNavItem, user.userId) },
                    source = user.profileImageSource,
                    uri = user.profileImage?.toUri(),
                    name = user.name,
                    surname = user.surname,
                    color = user.color,
                    description = "User profile image",
                )
                if(team.admin.contains(user.userId))
                Image(modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    painter = painterResource(id = R.drawable.ic_admin),
                    contentDescription = "Admin badge")

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
            val selectedNavItem =
                actions.navCont.currentBackStackEntry?.destination?.route?.split("/")?.first()
                    .toString()
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            )
            {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .clickable { actions.openProfile(selectedNavItem, user.userId) },
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
                    .clickable(onClick = { actions.openPersonalChat(user.userId, team.teamId) }),
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
                                Box{
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
                                Icon(modifier = Modifier.size(18.dp), imageVector = Icons.Rounded.Check, contentDescription = null)
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
                IconButton(
                    onClick = { actions.openPersonalChat(user.userId, team.teamId) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Send,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
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