package com.teamon.app.chats

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.chatsViewModel
import com.teamon.app.profileViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Message
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.MessageDeleteDialog
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.toInt
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter



@Composable
fun SentPersonalMessageCard(
    query: String,
    isQuerying: () -> Boolean,
    message: Message,
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
                    .padding(2.dp),
                horizontalAlignment = Alignment.End
            ) {
                Card(
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 5.dp
                    ),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 15.dp),
                    ) {
                        Column(modifier = Modifier) {

                            Text(
                                text = annotatedString,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                                overflow = TextOverflow.Ellipsis
                            )

                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.align(Alignment.Bottom)) {
                            Text(
                                modifier = Modifier,
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
fun ReceivedPersonalMessageCard(
    query: String,
    isQuerying: () -> Boolean,
    message: Message,
) {
    val sender by usersViewModel.getUser(message.senderId).collectAsState(initial = User())
    Theme(color = sender.color, applyToStatusBar = false) {
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
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(2.dp)
                    .align(Alignment.CenterVertically)
            ) {

                Card(
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        bottomStart = 5.dp,
                        topEnd = 20.dp,
                        bottomEnd = 20.dp
                    ),
                    colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 5.dp, bottom = 10.dp, start = 15.dp, end = 15.dp),
                    ) {
                        Column {

                            Text(
                                text = annotatedString,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                                overflow = TextOverflow.Ellipsis
                            )

                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.align(Alignment.Bottom)) {
                            Text(
                                modifier = Modifier,
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