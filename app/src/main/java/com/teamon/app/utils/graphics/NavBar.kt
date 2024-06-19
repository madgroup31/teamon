package com.teamon.app.utils.graphics

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil.compose.AsyncImage
import com.teamon.app.Actions
import com.teamon.app.NavigationItem
import com.teamon.app.R
import com.teamon.app.Screen
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.printGraphPath
import com.teamon.app.profileViewModel
import com.teamon.app.usersViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("RestrictedApi")
@Composable
fun NavigationBar(scrollBehavior: BottomAppBarScrollBehavior, actions: Actions) {

    BottomAppBar(
        scrollBehavior = scrollBehavior,
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        val items = listOf(
            NavigationItem.Board,
            NavigationItem.MyTasks,
            NavigationItem.MyTeams,
            NavigationItem.Chats,
            NavigationItem.Account
        )
        items.forEach { item ->
            val title = item.title.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }
            val selectedNavItem =
                actions.navCont.currentBackStackEntry?.destination?.route?.split("/")?.first()
                    .toString()
            val size =
                animateDpAsState(targetValue = if (selectedNavItem == item.title) 28.dp else 24.dp)
            NavigationBarItem(
                modifier = Modifier,
                selected = selectedNavItem == item.title,
                onClick = {
                    actions.navCont.navigate(item.title) {
                        popUpTo(actions.navCont.graph.findStartDestination().id) {
                            saveState = item.title != selectedNavItem
                        }
                        restoreState = true
                    }

                    Log.d(
                        "nav",
                        printGraphPath(actions.navCont.currentBackStackEntry?.destination?.parent) + "/" + actions.navCont.currentBackStackEntry?.destination?.route
                    )

                },
                label = {
                    Text(
                        title,
                        style = MaterialTheme.typography.labelMedium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                icon = {
                    if (selectedNavItem == item.title && item.title != NavigationItem.Account.title)
                        Image(
                            painter = painterResource(id = item.focusedIcon),
                            modifier = Modifier.size(size.value),
                            contentDescription = item.title,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                        )
                    if (selectedNavItem == item.title && item.title == NavigationItem.Account.title) {
                        TeamOnImage(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape),
                            source = profileViewModel!!.profileImageSource,
                            uri = profileViewModel!!.profileImageUri,
                            name = profileViewModel!!.nameValue,
                            surname = profileViewModel!!.surnameValue,
                            color = profileViewModel.color,
                            description = profileViewModel!!.nameValue + " " + profileViewModel!!.surnameValue + " profile image",
                        )
                    }
                    if (selectedNavItem != item.title && item.title != NavigationItem.Account.title)
                        if (item.title != NavigationItem.Account.title)
                            Image(
                                painter = painterResource(id = item.icon),
                                modifier = Modifier.size(size.value),
                                contentDescription = item.title,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                    if (selectedNavItem != item.title && item.title == NavigationItem.Account.title) {
                            TeamOnImage(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                source = profileViewModel!!.profileImageSource,
                                uri = profileViewModel!!.profileImageUri,
                                name = profileViewModel!!.nameValue,
                                color = profileViewModel.color,
                                surname = profileViewModel!!.surnameValue,
                                description = "My Profile picture"
                            )
                    }
                    if (item.badgeCounter > 0) Badge(
                        modifier = Modifier
                            .zIndex(2f)
                            .offset(size.value, -(size.value / 5))
                    ) {
                        Text(
                            text = item.badgeCounter.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                })
        }
    }
}