package com.teamon.app.chats

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.chatsViewModel
import com.teamon.app.profileViewModel
import com.teamon.app.teamOnViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Message
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.MessageDeleteDialog
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.generateQRCode
import com.teamon.app.utils.graphics.toInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DayHeader(day: String?) {
    Box(modifier = Modifier.padding(5.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        }
        Box(modifier = Modifier.align(Alignment.Center)) {
            Row(
                modifier = Modifier
                    .width(120.dp)
                    .height(30.dp)
                    .padding(5.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = day ?: "", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SentTeamMessageCard(
    query: String,
    isQuerying: () -> Boolean,
    message: Message,
    partecipants: Int,
) {
    Theme(color = profileViewModel.color, applyToStatusBar = false) {
        var isShowingOptions by remember { mutableStateOf(false) }

        val instant = Instant.ofEpochSecond(message.timestamp.toInstant().epochSecond)
        val date = instant.atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val formattedDate = date.format(formatter)

        val formattedQuery = query.trim()
        val foundQuery = message.content.contains(formattedQuery, ignoreCase = true)

        val containerColor =
            if (isQuerying() && foundQuery) MaterialTheme.colorScheme.inversePrimary
            else MaterialTheme.colorScheme.primaryContainer

        val messageText = message.content

        val annotatedString = buildAnnotatedString {

            if (isQuerying() && foundQuery) {

                val indices = mutableListOf<Int>()
                var index = messageText.indexOf(formattedQuery, ignoreCase = true)

                while (index != -1 && index < messageText.length) {
                    indices.add(index)
                    index = messageText.indexOf(formattedQuery, index + 1, ignoreCase = true)
                }

                var startIdx = 0

                for (idx in indices) {

                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                        append(messageText.substring(startIdx, idx))
                    }
                    withStyle(style = SpanStyle(background = Color.Yellow)) {
                        append(messageText.substring(idx, idx + formattedQuery.length))
                    }
                    startIdx = idx + formattedQuery.length

                }

                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(messageText.substring(startIdx))
                }

            } else {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(messageText)
                }
            }
        }

        if (isShowingOptions) {
            MessageDeleteDialog(
                onDismiss = { isShowingOptions = false },
                onConfirm = { chatsViewModel!!.deleteMessage(message.messageId) }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            isShowingOptions = true
                            //chatsViewModel!!.deleteMessage(message.messageId)
                            //actions.openTask(actions.navCont.currentBackStackEntry!!.destination.route!!.split("/").first(), projectId, taskId)
                        }
                    )
                },
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(2.dp),
                horizontalAlignment = Alignment.End
            ) {
                Card(
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 5.dp
                    ),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                ) {
                    Column(
                        modifier = Modifier.wrapContentWidth(Alignment.Start)
                            .padding(vertical = 10.dp, horizontal = 15.dp)
                    ) {

                        Text(
                            text = annotatedString,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = Int.MAX_VALUE, // Allows text to expand vertically
                        )
                        Spacer(modifier = Modifier.height(4.dp)) // Adds space between text and date

                        Row(
                            modifier = Modifier.align(Alignment.End),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val readBy = partecipants - message.unread.size - 1
                            Text(
                                text = readBy.toString(),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                            Icon(
                                modifier = Modifier
                                    .size(18.dp)
                                    .alpha(0.75f),
                                painter = painterResource(R.drawable.outline_remove_red_eye_24),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }

    }
}


@Composable
fun ReceivedTeamMessageCard(
    query: String,
    isQuerying: () -> Boolean,
    message: Message,
    actions: Actions,
) {

    val author by usersViewModel.getUser(message.senderId).collectAsState(initial = User())

    Theme(color = author.color, applyToStatusBar = false) {

        val instant = Instant.ofEpochSecond(message.timestamp.toInstant().epochSecond)
        val date = instant.atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val formattedDate = date.format(formatter)

        val formattedQuery = query.trim()
        val foundQuery = message.content.contains(formattedQuery, ignoreCase = true)

        val containerColor =
            if (isQuerying() && foundQuery) MaterialTheme.colorScheme.inversePrimary
            else MaterialTheme.colorScheme.secondaryContainer

        val messageText = message.content

        val annotatedString = buildAnnotatedString {


            if (isQuerying() && foundQuery) {

                val indices = mutableListOf<Int>()
                var index = messageText.indexOf(formattedQuery, ignoreCase = true)

                while (index != -1 && index < messageText.length) {
                    indices.add(index)
                    index = messageText.indexOf(formattedQuery, index + 1, ignoreCase = true)
                }

                var startIdx = 0

                for (idx in indices) {

                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                        append(messageText.substring(startIdx, idx))
                    }
                    withStyle(style = SpanStyle(background = Color.Yellow)) {
                        append(messageText.substring(idx, idx + formattedQuery.length))
                    }
                    startIdx = idx + formattedQuery.length

                }

                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(messageText.substring(startIdx))
                }

            } else {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(messageText)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val selectedNavItem =
                actions.navCont.currentBackStackEntry?.destination?.route?.split("/")?.first()
                    .toString()
            Column(
                modifier = Modifier
                    .padding(2.dp)
                    .align(Alignment.Bottom)
            ) {
                IconButton(modifier = Modifier.size(32.dp), onClick = { }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        TeamOnImage(
                            modifier = Modifier
                                .clickable { actions.openProfile(selectedNavItem, author.userId) },
                            source = author.profileImageSource,
                            uri = author.profileImage?.toUri(),
                            name = author.name,
                            surname = author.surname,
                            color = author.color,
                            description = author.name + " " + author.surname + " profile image"
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .padding(2.dp)
                    .align(Alignment.CenterVertically)
            ) {

                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 5.dp, topEnd = 20.dp, bottomEnd = 20.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
                ) {
                    Column(
                        modifier = Modifier.wrapContentWidth(Alignment.Start)
                            .padding(vertical = 10.dp, horizontal = 15.dp)
                    ) {
                        Text(
                            modifier = Modifier.clickable {
                                actions.openProfile(
                                    selectedNavItem,
                                    author.userId
                                )
                            },
                            text = author.nickname,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = annotatedString,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = Int.MAX_VALUE, // Allows text to expand vertically
                        )
                        Spacer(modifier = Modifier.height(2.dp)) // Adds space between text and date
                        Row(
                            modifier = Modifier.align(Alignment.End),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }
    }
}