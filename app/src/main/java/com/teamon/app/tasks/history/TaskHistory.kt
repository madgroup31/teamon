package com.teamon.app.tasks.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teamon.app.prefs
import com.teamon.app.utils.classes.History
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.asPastRelativeDate

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskHistory(history: List<History>) {
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        if (listState.layoutInfo.totalItemsCount > 0)
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
    }


    if (history.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedItem(index = 1) {
                Text(
                    text = "No History available yet.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic
                )
            }

        }
    } else {
        val animate = prefs.getBoolean("animate", true)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {


            val historyItems = history.sortedBy { it.timestamp }.reversed()
                .groupBy { it.timestamp.asPastRelativeDate() }
            historyItems.forEach { (day, historyPerDay) ->
                stickyHeader(day, String) {
                    DayHeader(day)
                }
                items(historyPerDay, key = { it.historyId }) {
                    Box(modifier = if(animate) Modifier.animateItemPlacement() else Modifier) {
                        TaskHistoryCard(it)
                    }
                }
            }

        }
    }
}