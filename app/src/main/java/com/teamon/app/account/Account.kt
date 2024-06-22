package com.teamon.app.account

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.teamon.app.Actions
import com.teamon.app.Location
import com.teamon.app.NavigationItem
import com.teamon.app.R
import com.teamon.app.Screen
import com.teamon.app.prefs
import com.teamon.app.profileViewModel
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.LoadingOverlay
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.UploadStatus
import com.teamon.app.utils.themes.teamon.TeamOnTheme
import com.teamon.app.utils.viewmodels.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.withContext
import java.util.*


@Composable
fun AccountView(actions: Actions, userVm: UserViewModel? = null, signUp: Boolean = false) {

    val context = LocalContext.current
    Location.initialize(context, context as Activity)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    LaunchedEffect(key1 = profileViewModel.isEditing) {
        if (profileViewModel.isEditing)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    fusedLocationClient
                        .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnCompleteListener { location ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val l = location.await()

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Geocoder(context, Locale.getDefault()).getFromLocation(
                                        l.latitude,
                                        l.longitude,
                                        1
                                    ) {
                                        profileViewModel.setLocation(it.first()?.locality ?: "")
                                    }
                                } else profileViewModel.setLocation("")
                            }
                        }
                } catch (e: Exception) {
                    profileViewModel.setLocation("")
                }

            } else {
                profileViewModel.setLocation("")
            }

    }

    Theme(color = profileViewModel.color, applyToStatusBar = true) {

        var landscape by remember { mutableStateOf(false) }
        landscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        when (userVm) {
            null -> {
                if (signUp) profileViewModel.edit()
                if (landscape) LandscapeView(actions = actions, signUp = signUp)
                else PortraitView(actions = actions, signUp = signUp)
            }

            else -> {
                if (landscape) LandscapeView(actions = actions, userVm = userVm)
                else PortraitView(actions = actions, userVm = userVm)
            }
        }

    }
}

@Composable
fun LandscapeView(
    actions: Actions,
    userVm: UserViewModel? = null,
    signUp: Boolean = false
) {
    val snackbarHostState = remember { SnackbarHostState() }
    if (profileViewModel.uploadStatus is UploadStatus.Error || userVm?.error == true)
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("An error occurred. Please try again.")
        }
    var isSigningUp by rememberSaveable { mutableStateOf(signUp) }

    when (userVm) {
        null -> {
            AppSurface(
                snackbarHostState = snackbarHostState,
                isSigningUp = isSigningUp,
                orientation = Orientation.LANDSCAPE,
                actions = actions,
                trailingTopBarActions = {
                    if (!signUp) {
                        var animate by remember {
                            mutableStateOf(
                                prefs.getBoolean(
                                    "animate",
                                    true
                                )
                            )
                        }
                        var showDropdown by rememberSaveable { mutableStateOf(false) }
                        IconButton(onClick = { showDropdown = true }) {
                            Icon(
                                Icons.Rounded.MoreVert,
                                contentDescription = "More account options"
                            )
                        }
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }) {
                            DropdownMenuItem(text = {
                                Text(
                                    if (animate) "Disable animations" else "Enable animations",
                                    color = if (animate) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }, onClick = {
                                prefs.edit().putBoolean("animate", !animate).apply(); animate =
                                !animate
                            })
                        }
                    }
                },
                title = "My Account",
                floatingActionButton = {

                    if (!profileViewModel.isEditing) {
                        FloatingActionButton(modifier = Modifier
                            .padding(end = 10.dp), onClick = { profileViewModel.edit() }) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Edit"
                            )
                        }
                    } else {
                        if (profileViewModel.uploadStatus !is UploadStatus.Progress)
                            FloatingActionButton(modifier = Modifier
                                .padding(end = 10.dp),
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        if (signUp) {
                                            if (profileViewModel.validate()) {
                                                snackbarHostState.showSnackbar("Sign up successful!")
                                                isSigningUp = false
                                            }
                                        } else {
                                            profileViewModel.validate()
                                        }
                                    }
                                }) {
                                if (signUp)
                                    Text(text = "Sign Up", modifier = Modifier.padding(10.dp))
                                else
                                    Image(
                                        painterResource(id = R.drawable.round_save_24),
                                        contentDescription = "Save",
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                                    )
                            }
                    }
                })
            {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    item {
                        AnimatedItem(index = 1) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    AccountQuickInfo(
                                        Orientation.LANDSCAPE,
                                        isSigningUp = isSigningUp
                                    )
                                }
                                Column(modifier = Modifier.weight(1.5f)) {
                                    AccountPersonalInformation(Orientation.LANDSCAPE)
                                }
                            }
                        }
                    }
                    item {
                        AnimatedItem(index = 1) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    OutlinedTextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp, 0.dp, 20.dp, 0.dp),
                                        readOnly = !profileViewModel.isEditing,
                                        enabled = true,
                                        value = profileViewModel.bioValue,
                                        label = { Text("Biography") },
                                        onValueChange = { bio -> profileViewModel.setBio(bio) },
                                        isError = profileViewModel.bioError.isNotBlank()
                                    )
                                    if (profileViewModel!!.bioError.isNotBlank()) {
                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp, 0.dp, 20.dp, 0.dp),
                                            text = profileViewModel!!.bioError,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (!signUp) {
                        item {
                            AnimatedItem(index = 2) {
                                AccountFeedback(
                                    actions = actions,
                                    orientation = Orientation.LANDSCAPE,
                                )
                            }
                        }
                        item {
                            AnimatedItem(index = 3) {
                                AccountPerformance(
                                    profileViewModel!!.feedbacks,
                                    profileViewModel!!.tasks
                                )
                            }
                        }
                        item {
                            AnimatedItem(index = 4) {
                                ProgressSliders()
                            }
                        }
                    }

                    item {
                        AnimatedItem(index = 5) {
                            Row(modifier = Modifier.padding(20.dp)) {
                                Column {
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error,
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        onClick = { /*TODO*/ }) {
                                        Text("Log out")
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            contentColor = MaterialTheme.colorScheme.onError,
                                            containerColor = MaterialTheme.colorScheme.error
                                        ),
                                        onClick = { /*TODO*/ }) {
                                        Text("Delete account")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        else -> {
            AppSurface(
                orientation = Orientation.LANDSCAPE,
                actions = actions,
                leadingTopBarActions = {
                    IconButton(
                        onClick = { actions.navCont.popBackStack() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
                    }
                },
                floatingActionButton = {
                    if (!userVm.isWritingFeedback) {
                        FloatingActionButton(
                            onClick = {
                                userVm.toggleIsWritingFeedback()
                            },
                            modifier = Modifier
                                .padding(end = 10.dp),
                            content = {
                                Image(
                                    painter = painterResource(R.drawable.outline_feedback_24),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                    contentDescription = "Add a Feedback"
                                )
                            }
                        )
                    }
                },
                title = userVm.nameValue + " " + userVm.surnameValue
            )
            {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    item {
                        AnimatedItem(index = 1) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    AccountQuickInfo(Orientation.LANDSCAPE, userVm)
                                }
                                Column(modifier = Modifier.weight(1.5f)) {
                                    AccountPersonalInformation(Orientation.LANDSCAPE, userVm)
                                }
                            }
                        }
                    }
                    item {
                        AnimatedItem(index = 1) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    OutlinedTextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp, 0.dp, 20.dp, 0.dp),
                                        readOnly = true,
                                        enabled = false,
                                        value = userVm.bioValue,
                                        label = { Text("Biography") },
                                        onValueChange = { },
                                    )

                                }
                            }
                        }
                    }
                    item {
                        AnimatedItem(index = 2) {
                            AccountFeedback(
                                actions = actions,
                                orientation = Orientation.LANDSCAPE,
                                userVm
                            )
                        }
                    }
                    item {
                        AnimatedItem(index = 3) {
                            AccountPerformance(userVm.feedbacks, userVm.tasks)
                        }
                    }
                    item {
                        AnimatedItem(index = 4) {
                            ProgressSliders(userVm)
                        }
                    }
                }
                if (userVm.isWritingFeedback) {
                    NewPersonalFeedbackDialog(
                        title = userVm.nameValue + " " + userVm.surnameValue,
                        onDismissRequest = { userVm.toggleIsWritingFeedback() },
                        userVm = userVm
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PortraitView(
    actions: Actions,
    userVm: UserViewModel? = null,
    signUp: Boolean = false
) {
    val snackbarHostState = remember { SnackbarHostState() }
    if (profileViewModel.uploadStatus is UploadStatus.Error || userVm?.error == true)
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("An error occurred. Please try again.")
        }

    var isSigningUp by rememberSaveable { mutableStateOf(signUp) }
    val context = LocalContext.current.applicationContext

    LaunchedEffect(isSigningUp) {
        if (signUp && !isSigningUp) {
            Toast.makeText(
                context,
                "Sign up successful!\nWelcome to TeamOn " + profileViewModel.nameValue + "!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    when (userVm) {
        null -> {
            AppSurface(
                snackbarHostState = snackbarHostState,
                isSigningUp = isSigningUp,
                orientation = Orientation.PORTRAIT,
                trailingTopBarActions = {
                    if (!signUp) {
                        var animate by remember {
                            mutableStateOf(
                                prefs.getBoolean(
                                    "animate",
                                    true
                                )
                            )
                        }

                        val infiniteTransition = rememberInfiniteTransition()
                        val anim by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 0.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 3000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        if (!animate) {
                            IconButton(onClick = {
                                prefs.edit().putBoolean("animate", true).apply();
                                animate = true
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar("Animations enabled")
                                }
                            }) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        modifier = Modifier.align(Alignment.Center),
                                        painter = painterResource(R.drawable.round_hdr_weak_24),
                                        contentDescription = "Stop animation"
                                    )
                                }
                            }
                        } else
                            IconButton(onClick = {
                                prefs.edit().putBoolean("animate", false).apply();
                                animate = false
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar("Animations disabled")
                                }
                            }) {
                                AnimatedContent(targetState = anim >= 0.5f, transitionSpec = {
                                    fadeIn(animationSpec = tween(1500)) togetherWith fadeOut(
                                        animationSpec = tween(1500)
                                    )
                                }) { targetState ->
                                    val iconPainter: Painter = if (targetState) {
                                        painterResource(R.drawable.round_hdr_weak_24)
                                    } else {
                                        painterResource(R.drawable.round_hdr_strong_24)
                                    }
                                    Icon(
                                        painter = iconPainter,
                                        contentDescription = "More account options",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .alpha(if (targetState) anim else 1f - anim)
                                    )
                                }
                            }
                    }
                },
                title = "My Account",
                actions = actions,
                floatingActionButton = {
                    if (profileViewModel.uploadStatus !is UploadStatus.Progress)
                        if (!profileViewModel.isEditing) {
                            FloatingActionButton(
                                onClick = { profileViewModel.edit() }
                            ) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                        } else {
                            FloatingActionButton(
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        if (isSigningUp) {
                                            isSigningUp = !profileViewModel.validate()
                                        } else {
                                            profileViewModel.validate()
                                        }
                                    }

                                }) {
                                if (isSigningUp)
                                    Text(text = "Sign Up", modifier = Modifier.padding(10.dp))
                                else
                                    Image(
                                        painterResource(id = R.drawable.round_save_24),
                                        contentDescription = "Save",
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                                    )
                            }

                        }

                })
            {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    item {
                        AnimatedItem(index = 1) {
                            AccountQuickInfo(Orientation.PORTRAIT, isSigningUp = isSigningUp)
                        }
                    }
                    item {
                        AnimatedItem(index = 2) {
                            AccountPersonalInformation(Orientation.PORTRAIT)
                        }
                    }
                    if (!isSigningUp) {
                        item {
                            AnimatedItem(index = 3) {
                                AccountFeedback(
                                    actions = actions,
                                    orientation = Orientation.PORTRAIT,
                                )
                            }
                        }
                        item {
                            AnimatedItem(index = 4) {
                                AccountPerformance(
                                    profileViewModel.feedbacks,
                                    profileViewModel.tasks
                                )
                            }
                        }
                        item {
                            AnimatedItem(index = 5) {
                                ProgressSliders()
                            }
                        }
                    }
                    item {
                        AnimatedItem(index = 6) {
                            Row(modifier = Modifier.padding(20.dp)) {
                                Column {
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error,
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        onClick = {
                                            profileViewModel.signOut();
                                            actions.navCont.navigate(Screen.Login.route) {
                                                popUpTo(Screen.Login.route) { inclusive = true }
                                            }
                                        }) {
                                        Text("Log out")
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            contentColor = MaterialTheme.colorScheme.onError,
                                            containerColor = MaterialTheme.colorScheme.error
                                        ),
                                        onClick = {
                                            profileViewModel.deleteAccount();
                                            actions.navCont.navigate(Screen.Login.route) {
                                                popUpTo(Screen.Login.route) { inclusive = true }
                                            }
                                        }) {
                                        Text("Delete account")
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

        else -> {
            AppSurface(
                orientation = Orientation.PORTRAIT,
                title = userVm.nameValue + " " + userVm.surnameValue,
                leadingTopBarActions = {
                    IconButton(
                        onClick = { actions.navCont.popBackStack() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
                    }
                },
                floatingActionButton = {
                    if (!userVm.isWritingFeedback) {
                        FloatingActionButton(
                            onClick = {
                                userVm.toggleIsWritingFeedback()
                            },
                            modifier = Modifier,
                            content = {
                                Image(
                                    painter = painterResource(R.drawable.outline_feedback_24),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                    contentDescription = "Add a Feedback"
                                )
                            }
                        )
                    }
                },
                actions = actions
            )
            {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        AnimatedItem(index = 1) {
                            AccountQuickInfo(Orientation.PORTRAIT, userVm)
                        }
                    }
                    item {
                        AnimatedItem(index = 2) {
                            AccountPersonalInformation(Orientation.PORTRAIT, userVm)
                        }
                    }
                    item {
                        AnimatedItem(index = 3) {
                            AccountFeedback(
                                actions = actions,
                                orientation = Orientation.PORTRAIT,
                                userVm
                            )
                        }
                    }
                    item {
                        AnimatedItem(index = 4) {
                            AccountPerformance(userVm.feedbacks, userVm.tasks)
                        }
                    }
                    item {
                        AnimatedItem(index = 5) {
                            ProgressSliders(userVm)
                        }
                    }
                }
                if (userVm.isWritingFeedback) {
                    NewPersonalFeedbackDialog(
                        title = userVm.nameValue + " " + userVm.surnameValue,
                        onDismissRequest = { userVm.toggleIsWritingFeedback() },
                        userVm = userVm
                    )
                }
            }
        }
    }

}
