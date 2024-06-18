package com.teamon.app.account

import android.content.res.Configuration
import android.widget.Toast
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
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.Screen
import com.teamon.app.prefs
import com.teamon.app.profileViewModel
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.UploadStatus
import com.teamon.app.utils.themes.teamon.TeamOnTheme
import com.teamon.app.utils.viewmodels.NewAccountViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SignUpView(actions: Actions, newUserVm: NewAccountViewModel) {

    TeamOnTheme(applyToStatusBar = true) {

        var landscape by remember { mutableStateOf(false) }
        landscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (landscape) LandscapeView(actions = actions, newUserVm = newUserVm)
        else PortraitView(actions = actions, newUserVm = newUserVm)
    }


}

@Composable
fun LandscapeView(
    actions: Actions,
    newUserVm: NewAccountViewModel,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current.applicationContext
    LaunchedEffect(Unit) {
        Toast.makeText(
            context,
            "In order to sign up, please finalize your account.",
            Toast.LENGTH_LONG
        ).show()
    }

    LaunchedEffect(newUserVm.error) {
        if (newUserVm.error != null && newUserVm.error!!.isNotBlank()) {
            snackbarHostState.showSnackbar(newUserVm.error!!)
            newUserVm.setErrorMessage(null)
        }
    }

    AppSurface(
        snackbarHostState = snackbarHostState,
        isSigningUp = true,
        orientation = Orientation.LANDSCAPE,
        actions = actions,
        title = "My Account",
        floatingActionButton = {
            if (newUserVm.uploadStatus !is UploadStatus.Progress)
                FloatingActionButton(
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            if (newUserVm.validate()) {
                                snackbarHostState.showSnackbar("Sign up successful!")
                                actions.navCont.navigate(Screen.Main.route)
                            } else {
                                snackbarHostState.showSnackbar("An error occurred. Please try again.")
                            }
                        }

                    }) {
                    Text(text = "Sign Up", modifier = Modifier.padding(10.dp))
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
                            NewAccountQuickInfo(Orientation.LANDSCAPE, newUserVm = newUserVm)
                        }
                        Column(modifier = Modifier.weight(1.5f)) {
                            NewAccountPersonalInformation(Orientation.LANDSCAPE, userVm = newUserVm)
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
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 20.dp),
                                readOnly = false,
                                enabled = true,
                                value = newUserVm.bioValue,
                                label = { Text("Biography") },
                                onValueChange = { bio -> newUserVm.setBio(bio) },
                                isError = newUserVm.bioError.isNotBlank()
                            )
                            if (newUserVm.bioError.isNotBlank()) {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp, 0.dp, 20.dp, 0.dp),
                                    text = newUserVm.bioError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PortraitView(
    actions: Actions,
    newUserVm: NewAccountViewModel,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current.applicationContext

    LaunchedEffect(Unit) {
        Toast.makeText(
            context,
            "In order to sign up, please finalize your account.",
            Toast.LENGTH_LONG
        ).show()
    }

    LaunchedEffect(newUserVm.error) {
        if (newUserVm.error != null && newUserVm.error!!.isNotBlank()) {
            snackbarHostState.showSnackbar(newUserVm.error!!)
            newUserVm.setErrorMessage(null)
        }
    }


    AppSurface(
        snackbarHostState = snackbarHostState,
        isSigningUp = true,
        orientation = Orientation.PORTRAIT,
        title = "My Account",
        actions = actions,
        floatingActionButton = {
            if (newUserVm.uploadStatus !is UploadStatus.Progress)
                FloatingActionButton(
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            if (newUserVm.validate()) {
                                snackbarHostState.showSnackbar("Sign up successful!")
                                actions.navCont.navigate(Screen.Main.route)
                            } else {
                                snackbarHostState.showSnackbar("An error occurred. Please try again.")
                            }
                        }

                    }) {
                    Text(text = "Sign Up", modifier = Modifier.padding(10.dp))
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
                    NewAccountQuickInfo(Orientation.PORTRAIT, newUserVm = newUserVm)
                }
            }

            item {
                AnimatedItem(index = 2) {
                    NewAccountPersonalInformation(Orientation.PORTRAIT, userVm = newUserVm)
                }
            }
        }

    }
}