package com.teamon.app.tasks.comments

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teamon.app.Actions
import com.teamon.app.utils.classes.Comment
import com.teamon.app.utils.graphics.AnimatedGrid
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.asPastRelativeDate
import com.teamon.app.utils.viewmodels.TaskViewModel

@Composable
fun DayHeader(day: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = day, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.size(7.dp))
        HorizontalDivider(
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TaskComments(
    actions: Actions,
    modified: Boolean,
    onModifiedChange: (Boolean) -> Unit,
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
    isQuerying: () -> Boolean,
    onQueryChange: (String) -> Unit,
    taskViewModel: TaskViewModel,
) {

    var landscape by remember { mutableStateOf(false) }
    landscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (landscape) LandscapeTaskCommentView(
        actions = actions,
        taskViewModel = taskViewModel,
        modified = modified,
        search = search,
        onSearchChange = onSearchChange,
        query = query,
        isQuerying = isQuerying,
        onQueryChange = onQueryChange,
        onModifiedChange = onModifiedChange
    )
    else PortraitTaskCommentView(
        actions = actions,
        taskViewModel = taskViewModel,
        modified = modified,
        search = search,
        onSearchChange = onSearchChange,
        query = query,
        isQuerying = isQuerying,
        onQueryChange = onQueryChange,
        onModifiedChange = onModifiedChange
    )

}


@Composable
fun PortraitTaskCommentView(
    modified: Boolean,
    onModifiedChange: (Boolean) -> Unit,
    actions: Actions,
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
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
            AnimatedGrid(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
                scrollToLast = true,
                columns = StaggeredGridCells.Adaptive(400.dp),
                items = taskViewModel.comments.sortedBy { it.timestamp }
                    .groupBy { it.timestamp.asPastRelativeDate() }.values.toList()
            ) { it, index ->
                val list = it as List<Comment>
                Column {
                    DayHeader(list.first().timestamp.asPastRelativeDate())
                    list.forEachIndexed { index, it ->
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


@Composable
fun LandscapeTaskCommentView(
    actions: Actions,
    modified: Boolean,
    onModifiedChange: (Boolean) -> Unit,
    taskViewModel: TaskViewModel,
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
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


        AnimatedGrid(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
            columns = StaggeredGridCells.Adaptive(400.dp),
            scrollToLast = true,
            items = taskViewModel.comments.sortedBy { it.timestamp }
                .groupBy { it.timestamp.asPastRelativeDate() }.values.toList()
        ) { it, index ->
            val list = it as List<Comment>
            list.forEach {
                Column {
                    DayHeader(list.first().timestamp.asPastRelativeDate())
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