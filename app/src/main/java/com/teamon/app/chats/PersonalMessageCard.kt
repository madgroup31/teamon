package com.teamon.app.chats

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.teamon.app.Actions
import com.teamon.app.chatsViewModel
import com.teamon.app.utils.classes.Message
import com.teamon.app.utils.graphics.MessageDeleteDialog
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter



@Composable
fun SentPersonalMessageCard(
    query: String,
    isQuerying: () -> Boolean,
    message: Message,
) {
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
            .padding(start = 10.dp, end = 10.dp)
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
            OutlinedCard(
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 2.dp
                ),
                //onClick = { actions.openTask(actions.navCont.currentBackStackEntry!!.destination.route!!.split("/").first(), projectId, taskId) },
                colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
                modifier = Modifier
                    .padding(5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row {
                        Text(
                            text = annotatedString,
                            //style = MaterialTheme.typography.bodyMedium,
                            //color = colorToApply,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
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
    val instant = Instant.ofEpochSecond(message.timestamp.toInstant().epochSecond)
    val date = instant.atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val formattedDate = date.format(formatter)

    val formattedQuery = query.trim()
    val foundQuery = message.content.contains(formattedQuery, ignoreCase = true)

    val containerColor =
        if (isQuerying() && foundQuery) MaterialTheme.colorScheme.inversePrimary
        else MaterialTheme.colorScheme.tertiaryContainer

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
            .padding(start = 10.dp, end = 10.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(2.dp),
            horizontalAlignment = Alignment.Start
        ) {
            OutlinedCard(
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 2.dp
                ),
                //onClick = { actions.openTask(actions.navCont.currentBackStackEntry!!.destination.route!!.split("/").first(), projectId, taskId) },
                colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
                modifier = Modifier
                    .padding(5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row {
                        Text(
                            text = annotatedString,
                            //style = MaterialTheme.typography.bodyMedium,
                            //color = colorToApply,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }

}