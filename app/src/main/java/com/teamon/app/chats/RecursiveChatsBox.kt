package com.teamon.app.chats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.chatsViewModel
import com.teamon.app.prefs
import com.teamon.app.teamsViewModel
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.graphics.TeamOnImage

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun RecursiveChatsBox(
    teamId: String,
    startNewChat: (String) -> Unit,
    actions: Actions,
) {
    val team by teamsViewModel.getTeam(teamId).collectAsState(initial = Team())
    val unreadTeamChatMessages by chatsViewModel.getUnreadTeamChatMessages(teamId).collectAsState(initial = 0)
    val lastMessages by chatsViewModel.getLastMessages(teamId).collectAsState(initial = emptyMap())


    val animate = prefs.getBoolean("animate", true)
    var expanded by remember { mutableStateOf(false) }
    val boxHeight by animateDpAsState(
        targetValue = if (expanded) (lastMessages.size * 80.dp) else (70.dp + (lastMessages.size.takeIf { it<3 }?:3) * 25.dp),
        animationSpec = if (animate) spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ) else snap(), label = ""
    )

    ElevatedCard(
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { expanded = !expanded }
    )
    {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
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
        }


        AnimatedVisibility(modifier = Modifier.fillMaxWidth(), visible = expanded) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { expanded = false }) {
                    Text(
                        modifier = Modifier,
                        text ="Show less",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = null)
                }


            }
        }
        Box(
            modifier = Modifier
                .height(boxHeight)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))

        ) {
            lastMessages.values.take(if (!expanded) 3 else lastMessages.size).forEachIndexed { index, it ->
                val offsetY: Dp by animateDpAsState(
                    targetValue = if (expanded) (index * 80).dp else (index * 25).dp,
                    animationSpec = if (animate) spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ) else snap(), label = ""
                )
                val padding: Dp by animateDpAsState(
                    targetValue = if (!expanded) (index * 10).dp else 0.dp,
                    animationSpec = if (animate) spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ) else snap(), label = ""
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex((lastMessages.size - index).toFloat())
                        .height(70.dp)
                        .offset(y = offsetY)
                        .padding(horizontal = padding.takeIf { it > 0.dp } ?: 0.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (expanded)
                        PersonalChatCard(
                            chatId = it.chatId,
                            actions = actions,
                            team = team
                        )
                    else
                        PersonalChatCard(
                            chatId = it.chatId,
                            actions = actions,
                            team = team,
                            setView = { expanded = it },
                        )
                }
            }
        }

    }
}