package com.teamon.app.tasks.comments

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teamon.app.prefs
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.asPastRelativeDate
import com.teamon.app.utils.viewmodels.TaskViewModel

@Composable
fun DayHeader(day: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(start = 12.dp, end = 12.dp, top = 7.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = day,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLowest),
            style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TaskComments(
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
    textBoxHeight: Int,
    isQuerying: () -> Boolean,
    onQueryChange: (String) -> Unit,
    taskViewModel: TaskViewModel,
) {

    var landscape by remember { mutableStateOf(false) }
    landscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (landscape) LandscapeTaskCommentView(
        taskViewModel = taskViewModel,
        search = search,
        onSearchChange = onSearchChange,
        query = query,
        textBoxHeight = textBoxHeight,
        isQuerying = isQuerying,
        onQueryChange = onQueryChange
    )
    else PortraitTaskCommentView(
        search = search,
        onSearchChange = onSearchChange,
        query = query,
        textBoxHeight = textBoxHeight,
        isQuerying = isQuerying,
        onQueryChange = onQueryChange,
        taskViewModel = taskViewModel
    )

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PortraitTaskCommentView(
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
    textBoxHeight: Int,
    isQuerying: () -> Boolean,
    onQueryChange: (String) -> Unit,
    taskViewModel: TaskViewModel
) {

    if (taskViewModel.comments.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedItem(index = 1) {
                Text(
                    text = "No Comments available yet.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic
                )
            }

        }
    } else
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
                            label = { Text("Search in comments") },
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
            val listState = rememberLazyListState()
            val animate = prefs.getBoolean("animate", true)
            LaunchedEffect(Unit) {
                if(taskViewModel.comments.isNotEmpty()) {
                    if(animate)
                    listState.animateScrollToItem(taskViewModel.comments.size - 1)
                    else
                        listState.scrollToItem(taskViewModel.comments.size -1)
                }
            }

            val height = with(LocalDensity.current) {
                textBoxHeight.toDp().value.toInt().dp + 20.dp
            }

            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = height),
                state = listState) {

                val commentItems = taskViewModel.comments.sortedBy { it.timestamp }
                    .groupBy { it.timestamp.asPastRelativeDate() }
                commentItems.forEach { (date, commentsPerDate) ->
                    stickyHeader(date, String) {
                        DayHeader(date)
                    }
                    items(commentsPerDate, key = { it.commentId }) {
                        Box(modifier = if(animate) Modifier.animateItemPlacement() else Modifier) {
                            CommentCard(
                                comment = it,
                                query = query,
                                isQuerying = isQuerying,
                            )
                        }
                    }
                }
            }


        }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LandscapeTaskCommentView(
    taskViewModel: TaskViewModel,
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
    textBoxHeight: Int,
    isQuerying: () -> Boolean,
    onQueryChange: (String) -> Unit,
) {
    if (taskViewModel.comments.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedItem(index = 1) {
                Text(
                    text = "No Comments available yet.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic
                )
            }

        }
    } else
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
                            label = { Text("Search in comments") },
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
            val listState = rememberLazyListState()
            val animate = prefs.getBoolean("animate", true)
            LaunchedEffect(Unit) {
                if(taskViewModel.comments.isNotEmpty()) {
                    if(animate)
                    listState.animateScrollToItem(taskViewModel.comments.size - 1)
                    else
                        listState.scrollToItem(taskViewModel.comments.size-1)
                }
            }

            val height = with(LocalDensity.current) {
                textBoxHeight.toDp().value.toInt().dp + 20.dp
            }

            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = height),
                state = listState) {

                val commentItems = taskViewModel.comments.sortedBy { it.timestamp }
                    .groupBy { it.timestamp.asPastRelativeDate() }
                commentItems.forEach { (date, commentsPerDate) ->
                    stickyHeader(date, String) {
                        DayHeader(date)
                    }
                    items(commentsPerDate, key = { it.commentId }) {
                        Box(modifier = if(animate) Modifier.animateItemPlacement() else Modifier) {
                            CommentCard(
                                comment = it,
                                query = query,
                                isQuerying = isQuerying,
                            )
                        }
                    }
                }
            }
        }


}