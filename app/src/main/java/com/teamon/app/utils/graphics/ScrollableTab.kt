package com.teamon.app.utils.graphics

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ScrollableTab(
    orientation: Orientation,
    items: List<TabItem>,
    selected: Int,
    pagerState: PagerState,
) {
    val scope = rememberCoroutineScope()
    when (orientation) {
        Orientation.PORTRAIT -> {
            PrimaryScrollableTabRow(
                selectedTabIndex = selected,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                items.forEachIndexed { index, s ->
                    Tab(
                        selected = selected == index,
                        onClick = { scope.launch {
                            pagerState.animateScrollToPage(index)
                        } }) {
                        Row(
                            modifier = Modifier.padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (selected == index) Image(
                                painter = painterResource(id = s.focusedIcon),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                            )
                            else Image(
                                painter = painterResource(id = s.icon),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                s.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

            }
            Spacer(modifier = Modifier.height(5.dp))
        }

        else -> {
            PrimaryTabRow(
                selectedTabIndex = selected,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                items.forEachIndexed { index, s ->
                    Tab(
                        selected = selected == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } }) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                s.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

}