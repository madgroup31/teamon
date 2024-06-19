package com.teamon.app.tasks.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.teamon.app.R
import com.teamon.app.profileViewModel
import com.teamon.app.teamOnViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Comment
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.TeamOnImage
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
fun CommentCard(
    query: String,
    isQuerying: () -> Boolean,
    comment: Comment,
) {
    val instant = Instant.ofEpochSecond(comment.timestamp.toInstant().epochSecond)
    val date = instant.atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val formattedDate = date.format(formatter)

    val formattedQuery = query.trim()
    val foundQuery = comment.text.contains(formattedQuery, ignoreCase = true)

    val containerColor =
        if (foundQuery) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    val commentText = comment.text

    val annotatedString = buildAnnotatedString {

        if (isQuerying() && foundQuery) {

            val indices = mutableListOf<Int>()
            var index = comment.text.indexOf(formattedQuery, ignoreCase = true)

            while (index != -1 && index < comment.text.length) {
                indices.add(index)
                index = comment.text.indexOf(formattedQuery, index + 1, ignoreCase = true)
            }

            var startIdx = 0

            for (idx in indices) {

                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(commentText.substring(startIdx, idx))
                }
                withStyle(style = SpanStyle(background = Color.Yellow)) {
                    append(comment.text.substring(idx, idx + formattedQuery.length))
                }
                startIdx = idx + formattedQuery.length

            }

            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                append(commentText.substring(startIdx))
            }

        } else {
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                append(commentText)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp), horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(2.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Column(
            modifier = Modifier
                .weight(7f)
                .padding(2.dp)
                .align(Alignment.CenterVertically)
        ) {
            OutlinedCard(
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 2.dp
                ),
                shape = RoundedCornerShape(20.dp),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val author by usersViewModel.getUser(comment.author).collectAsState(initial = User())
                        var text = author.name + " " + author.surname
                        val color = MaterialTheme.colorScheme.primary
                        var weight = FontWeight.Medium
                        if (author.userId == profileViewModel.userId) {
                            text = text.plus(" (Me)")
                            weight = FontWeight.Bold
                        }
                        Text(
                            text = text,
                            fontWeight = weight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 16.sp,
                            color = color
                        )
                    }
                    Row {
                        Text(
                            text = annotatedString,
                            //style = MaterialTheme.typography.bodyMedium,
                            //color = colorToApply,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(2.dp)
                .align(Alignment.CenterVertically)
        ) {
            IconButton(modifier = Modifier.size(30.dp), onClick = { }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    var loading by remember { mutableStateOf(true) }
                    val author by usersViewModel.getUser(comment.author).collectAsState(initial = User())
                    TeamOnImage(
                        modifier = Modifier.size(32.dp),
                        source = author.profileImageSource,
                        uri = author.profileImage?.toUri(),
                        name = author.name,
                        surname = author.surname,
                        color = author.color,
                        description = author.name + " " + author.surname + " profile image")

                }
            }
        }
    }

}