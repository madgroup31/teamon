package com.teamon.app.utils.graphics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.teamon.app.Actions

enum class Orientation { PORTRAIT, LANDSCAPE }

enum class ProjectColors { //others has to be added
    AMBER, BLUE, CERULEAN, DODGE, GORSE, GREEN, MAGENTA, ORANGE, PEAR, PERSIAN, POMEGRANATE, PURPLE, ROBIN, SEANCE, SUSHI;

    fun toGradient(): List<Color> {
        return when (this) {
            AMBER -> listOf(com.teamon.app.utils.themes.amber.primaryContainerLight, com.teamon.app.utils.themes.amber.primaryContainerLightMediumContrast)
            BLUE -> listOf(com.teamon.app.utils.themes.blue.primaryContainerLight, com.teamon.app.utils.themes.blue.primaryContainerLightMediumContrast)
            CERULEAN -> listOf(com.teamon.app.utils.themes.cerulean.primaryContainerLight, com.teamon.app.utils.themes.cerulean.primaryContainerLightMediumContrast)
            DODGE -> listOf(com.teamon.app.utils.themes.dodge.primaryContainerLight, com.teamon.app.utils.themes.dodge.primaryContainerLightMediumContrast)
            GORSE -> listOf(com.teamon.app.utils.themes.gorse.primaryContainerLight, com.teamon.app.utils.themes.gorse.primaryContainerLightMediumContrast)
            GREEN -> listOf(com.teamon.app.utils.themes.green.primaryContainerLight, com.teamon.app.utils.themes.green.primaryContainerLightMediumContrast)
            MAGENTA -> listOf(com.teamon.app.utils.themes.magenta.primaryContainerLight, com.teamon.app.utils.themes.magenta.primaryContainerLightMediumContrast)
            ORANGE -> listOf(com.teamon.app.utils.themes.orange.primaryContainerLight, com.teamon.app.utils.themes.orange.primaryContainerLightMediumContrast)
            PEAR -> listOf(com.teamon.app.utils.themes.pear.primaryContainerLight, com.teamon.app.utils.themes.pear.primaryContainerLightMediumContrast)
            PERSIAN -> listOf(com.teamon.app.utils.themes.persian.primaryContainerLight, com.teamon.app.utils.themes.persian.primaryContainerLightMediumContrast)
            POMEGRANATE -> listOf(com.teamon.app.utils.themes.pomegranate.primaryContainerLight, com.teamon.app.utils.themes.pomegranate.primaryContainerLightMediumContrast)
            PURPLE -> listOf(com.teamon.app.utils.themes.purple.primaryContainerLight, com.teamon.app.utils.themes.purple.primaryContainerLightMediumContrast)
            ROBIN -> listOf(com.teamon.app.utils.themes.robin.primaryContainerLight, com.teamon.app.utils.themes.robin.primaryContainerLightMediumContrast)
            SEANCE -> listOf(com.teamon.app.utils.themes.seance.primaryContainerLight, com.teamon.app.utils.themes.seance.primaryContainerLightMediumContrast)
            SUSHI -> listOf(com.teamon.app.utils.themes.sushi.primaryContainerLight, com.teamon.app.utils.themes.sushi.primaryContainerLightMediumContrast)
        }
    }
}

enum class ProjectImages { //others has to be added
    BUILD, EMAIL, SHARE, CALL, CREATE, FACE, HOME, LOCATION, LOCK, PLAY, SEARCH, SHOPPING, SETTINGS, STAR, WARNING;

    fun toImage() : ImageVector {
        return when (this) {
            BUILD -> Icons.Rounded.Build
            EMAIL -> Icons.Rounded.Email
            SHARE -> Icons.Rounded.Share
            CALL -> Icons.Rounded.Call
            CREATE -> Icons.Rounded.Create
            FACE -> Icons.Rounded.Face
            HOME -> Icons.Rounded.Home
            LOCATION -> Icons.Rounded.LocationOn
            LOCK -> Icons.Rounded.Lock
            PLAY -> Icons.Rounded.PlayArrow
            SEARCH -> Icons.Rounded.Search
            SHOPPING -> Icons.Rounded.ShoppingCart
            SETTINGS -> Icons.Rounded.Settings
            STAR -> Icons.Rounded.Star
            WARNING -> Icons.Rounded.Warning
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSurface(
    orientation: Orientation,
    actions: Actions,
    title: String,
    isSigningUp: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    composableTitle: @Composable (() -> Unit)? = null,
    tabActions: @Composable () -> Unit = {},
    leadingTopBarActions: @Composable () -> Unit = {},
    trailingTopBarActions: @Composable (RowScope.() -> Unit) = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    val topAppBarScrollBehavior = remember { topAppBarScroll }

    val bottomAppBarState = rememberBottomAppBarState()
    val bottomAppBarScroll = BottomAppBarDefaults.exitAlwaysScrollBehavior(bottomAppBarState)
    val bottomAppBarScrollBehavior = remember { bottomAppBarScroll }


    Row {
            Column(modifier = Modifier.fillMaxHeight()) {
                if(!isSigningUp && orientation == Orientation.LANDSCAPE) {
                        NavRail(actions = actions)
                }
            }
            Column(modifier = Modifier.fillMaxSize()) {

                Scaffold(
                    modifier = Modifier
                        .nestedScroll(bottomAppBarScrollBehavior.nestedScrollConnection)
                        .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    snackbarHost = { SnackbarHost(modifier = Modifier, hostState = snackbarHostState) },
                    topBar = {
                                Column {
                                    TopBar(
                                        scrollBehavior = topAppBarScrollBehavior,
                                        title = title,
                                        composableTitle = composableTitle,
                                        orientation = orientation,
                                        snackbarHostState = snackbarHostState,
                                        leadingTopBarActions = leadingTopBarActions,
                                        trailingTopBarActions = trailingTopBarActions)
                                    tabActions()
                                }
                             },
                    bottomBar = { if(!isSigningUp && orientation == Orientation.PORTRAIT) NavigationBar(actions = actions, scrollBehavior = bottomAppBarScrollBehavior) },
                    floatingActionButton = floatingActionButton)
                { innerPadding ->



                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(0.dp, 5.dp, 0.dp, 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.95f),
                                shape = RoundedCornerShape(20.dp),
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                color = MaterialTheme.colorScheme.surfaceContainerLowest
                            ) {
                                content(innerPadding)
                            }

                    }
                }
            }
        }

}