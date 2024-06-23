package com.teamon.app.utils.graphics

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.teamon.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    title: String,
    composableTitle: @Composable (() -> Unit)? = null,
    orientation: Orientation,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    leadingTopBarActions: @Composable () -> Unit = {},
    trailingTopBarActions: @Composable RowScope.() -> Unit = {}) {
    val context = LocalContext.current

    var isConnected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {

            networkConnectivityFlow(context).collect { status ->
                isConnected = status
            }

    }

    if(orientation == Orientation.LANDSCAPE || composableTitle != null)
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
            actions = {

                IconButton(onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(if(isConnected) "Good internet connection! Data is up to date." else "No internet connection! Some functions are not available or limited.")
                    }
                }) {
                    Image(
                        painter = painterResource(id = if(isConnected) R.drawable.ic_cloud_done else R.drawable.ic_cloud_off),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        contentDescription = "Good internet connection")
                }
                trailingTopBarActions()
            })
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
        actions = {

            IconButton(onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    snackbarHostState.showSnackbar(if(isConnected) "Good internet connection! Data is up to date." else "No internet connection! Some functionalities are limited or not available.")
                }
            }) {
                Image(painter = painterResource(id = if(isConnected) R.drawable.ic_cloud_done else R.drawable.ic_cloud_off),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    contentDescription = "Good internet connection")
            }
            trailingTopBarActions()
        }
    )
}
