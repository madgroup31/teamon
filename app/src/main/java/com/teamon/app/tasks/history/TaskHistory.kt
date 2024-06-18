package com.teamon.app.tasks.history

import androidx.collection.emptyLongSet
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teamon.app.historyViewModel
import com.teamon.app.teamOnViewModel
import com.teamon.app.utils.classes.History
import com.teamon.app.utils.graphics.AnimatedGrid
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.asPastRelativeDate

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
    } else
        AnimatedGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            columns = GridCells.Adaptive(400.dp),
            items = history.sortedBy { it.timestamp }.reversed()
                .groupBy { it.timestamp.asPastRelativeDate() }.values.toList()
        ) { it, index ->
            val list = it as List<History>
            Column {
                DayHeader(list.first().timestamp.asPastRelativeDate())
                list.forEach {
                    TaskHistoryCard(it)
                }
            }
        }
}