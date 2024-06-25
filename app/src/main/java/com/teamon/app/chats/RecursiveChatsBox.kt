package com.teamon.app.chats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.chatsViewModel
import com.teamon.app.prefs
import com.teamon.app.teamsViewModel
import com.teamon.app.utils.classes.Team

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun RecursiveChatsBox(
    teamId: String,
    startNewChat: (String) -> Unit,
    actions: Actions,
) {
    val team by teamsViewModel.getTeam(teamId).collectAsState(initial = Team())
    val lastMessages by chatsViewModel.getLastMessages(teamId).collectAsState(initial = emptyMap())

    val animate = prefs.getBoolean("animate", true)
    var expanded by remember { mutableStateOf(false) }
    val boxHeight by animateDpAsState(
        targetValue = if (expanded) (lastMessages.size * 80.dp) else (70.dp + (lastMessages.size.takeIf { it<3 }?:3) * 25.dp),
        animationSpec = if (animate) spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ) else snap(), label = ""
    )

    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        modifier = Modifier
            .padding(10.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                RoundedCornerShape(20.dp)
            )
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
            Box {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                IconButton(onClick = { startNewChat(teamId) }, colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)) {
                    Icon(
                        painter = painterResource(R.drawable.round_add_circle_outline_24),
                        contentDescription = "Start new chat",)
                }
            }
            }

        }
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TeamChatCard(actions = actions, team = team)
        }


        AnimatedVisibility(modifier = Modifier.fillMaxWidth(), visible = expanded) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { expanded = false }) {
                    Icon(Icons.Rounded.Person, contentDescription = "Personal Chats")
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        modifier = Modifier,
                        text ="Personal Chats",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

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
                val elevation: Dp by animateDpAsState(
                    targetValue = if (!expanded) (index * 3.dp + 1.dp) else 1.dp,
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
                    if (expanded || lastMessages.values.size == 1)
                        PersonalChatCard(
                            chatId = it.chatId,
                            actions = actions,
                            team = team,
                            elevation = elevation,
                        )
                    else
                        PersonalChatCard(
                            chatId = it.chatId,
                            actions = actions,
                            team = team,
                            setView = { expanded = it },
                            elevation = elevation
                        )
                }
            }
        }

    }
}