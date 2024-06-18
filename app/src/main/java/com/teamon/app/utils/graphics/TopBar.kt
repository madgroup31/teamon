package com.teamon.app.utils.graphics

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    title: String,
    composableTitle: @Composable (() -> Unit)? = null,
    orientation: Orientation,
    leadingTopBarActions: @Composable () -> Unit = {},
    trailingTopBarActions: @Composable RowScope.() -> Unit = {}) {
    if(orientation == Orientation.LANDSCAPE)
        CenterAlignedTopAppBar(
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
            title = {
                if(composableTitle == null)
                    Text(text = title, style = MaterialTheme.typography.headlineSmall, overflow = TextOverflow.Ellipsis, maxLines = 1)
                else
                    composableTitle()
            },
            navigationIcon = leadingTopBarActions,
            actions = trailingTopBarActions)
    else MediumTopAppBar(
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        title = {
            if(composableTitle == null)
                Text(text = title, style = MaterialTheme.typography.headlineSmall, overflow = TextOverflow.Ellipsis, maxLines = 1)
            else
                composableTitle() },
        navigationIcon = leadingTopBarActions,
        actions = trailingTopBarActions
    )
}
