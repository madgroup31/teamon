package com.teamon.app.login

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.Screen
import com.teamon.app.account.MyDatePickerDialog
import com.teamon.app.account.createImageFile
import com.teamon.app.account.saveImageToGallery
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.utils.graphics.LoadingOverlay
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.StorageAccess
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.UploadStatus
import com.teamon.app.utils.graphics.getStorageAccess
import com.teamon.app.utils.graphics.toInt
import com.teamon.app.utils.themes.teamon.TeamOnTheme
import com.teamon.app.utils.viewmodels.NewAccountViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.Objects

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
    var isLoading by remember {
        mutableStateOf(false)
    }
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
                                isLoading = true
                                snackbarHostState.showSnackbar("Sign up successful!")
                                actions.navCont.navigate(Screen.Main.route)
                                isLoading = false
                            } else {
                                snackbarHostState.showSnackbar("An error occurred. Please try again.")
                            }
                        }

                    }) {
                    Text(text = "Sign Up", modifier = Modifier.padding(10.dp))
                }
        })
    {
        LoadingOverlay(isLoading = isLoading)
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


@Composable
fun NewAccountQuickInfo(orientation: Orientation, newUserVm: NewAccountViewModel) {

    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val file = context.createImageFile(newUserVm.userId)
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        "com.teamon.app" + ".provider", file
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { result ->
            result?.let {
                newUserVm.setProfileImage(
                    uri = it,
                    source = ImageSource.LIBRARY,
                    context = context
                )
                expanded = false
            }
        }
    )

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {

            if (it) {
                newUserVm.setProfileImage(ImageSource.CAMERA, uri, context)
                expanded = false
                saveImageToGallery(
                    context,
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            context.contentResolver,
                            uri
                        )
                    ),
                    file.name
                )
            }
        }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Register ActivityResult handler
    val requestPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            // Handle permission requests results
            if (getStorageAccess(context) == StorageAccess.Full || getStorageAccess(context) == StorageAccess.Partial) {
                galleryLauncher.launch("image/*")
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }


    when (orientation) {
        Orientation.PORTRAIT -> {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, start = 10.dp, end = 10.dp, bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column(modifier = Modifier.weight(1.3f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingOverlay(isLoading = newUserVm.uploadStatus is UploadStatus.Progress)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .align(Alignment.Center)
                            ) {
                                if (newUserVm.uploadStatus !is UploadStatus.Progress)
                                    TeamOnImage(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .align(Alignment.Center)
                                            .clip(CircleShape)
                                            .alpha(0.5f),
                                        source = newUserVm.profileImageSource,
                                        uri = newUserVm.profileImageUri,
                                        name = newUserVm.nameValue,
                                        surname = newUserVm.surnameValue,
                                        color = newUserVm.color,
                                        description = "My Profile picture"
                                    )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .align(Alignment.Center)
                                ) {
                                    IconButton(
                                        modifier = Modifier.align(Alignment.Center),
                                        onClick = { expanded = !expanded },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Edit,
                                            contentDescription = "Update profile image"
                                        )
                                    }



                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.round_add_a_photo_24),
                                                        contentDescription = "Take a photo",
                                                        colorFilter = ColorFilter.tint(
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        "Take a photo"
                                                    )
                                                }
                                            },
                                            onClick = {
                                                val permissionCheckResult =
                                                    ContextCompat.checkSelfPermission(
                                                        context,
                                                        Manifest.permission.CAMERA
                                                    )
                                                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                                    cameraLauncher.launch(uri)
                                                } else {
                                                    // Request a permission
                                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.round_library_add_24),
                                                        contentDescription = "Upload from library",
                                                        colorFilter = ColorFilter.tint(
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        "Upload from library"
                                                    )
                                                }
                                            },
                                            onClick = {// Permission request logic
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                                    requestPermissions.launch(
                                                        arrayOf(
                                                            READ_MEDIA_IMAGES,
                                                            READ_MEDIA_VISUAL_USER_SELECTED
                                                        )
                                                    )
                                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    requestPermissions.launch(
                                                        arrayOf(
                                                            READ_MEDIA_IMAGES
                                                        )
                                                    )
                                                } else {
                                                    requestPermissions.launch(
                                                        arrayOf(
                                                            READ_EXTERNAL_STORAGE
                                                        )
                                                    )
                                                }
                                            }
                                        )
                                        if (newUserVm.profileImageSource != ImageSource.MONOGRAM)
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Image(
                                                            painter = painterResource(id = R.drawable.round_text_fields_24),
                                                            contentDescription = "Monogram",
                                                            colorFilter = ColorFilter.tint(
                                                                MaterialTheme.colorScheme.error
                                                            )
                                                        )
                                                        Spacer(
                                                            modifier = Modifier.width(
                                                                10.dp
                                                            )
                                                        )
                                                        Text(
                                                            text = "Restore default",
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    newUserVm.setProfileImage(source = ImageSource.MONOGRAM)
                                                    expanded = false
                                                }
                                            )
                                    }

                                }
                            }
                        }
                    }



                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(3f)
                            .padding(15.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = newUserVm.emailValue,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                    }
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Box(modifier = Modifier.size(100.dp)) {
                        LoadingOverlay(isLoading = newUserVm.uploadStatus is UploadStatus.Progress)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                        ) {
                            if (newUserVm.uploadStatus !is UploadStatus.Progress)
                                TeamOnImage(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .align(Alignment.Center)
                                        .clip(CircleShape)
                                        .alpha(0.5f),
                                    source = newUserVm.profileImageSource,
                                    uri = newUserVm.profileImageUri,
                                    name = newUserVm.nameValue,
                                    surname = newUserVm.surnameValue,
                                    color = newUserVm.color,
                                    description = "My Profile picture"
                                )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .align(Alignment.Center)
                            ) {
                                IconButton(
                                    modifier = Modifier.align(Alignment.Center),
                                    onClick = { expanded = !expanded },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                                ) {
                                    Icon(
                                        Icons.Rounded.Edit,
                                        contentDescription = "Update profile image"
                                    )
                                }



                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.round_add_a_photo_24),
                                                    contentDescription = "Take a photo",
                                                    colorFilter = ColorFilter.tint(
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    "Take a photo"
                                                )
                                            }
                                        },
                                        onClick = {
                                            val permissionCheckResult =
                                                ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.CAMERA
                                                )
                                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                                cameraLauncher.launch(uri)
                                            } else {
                                                // Request a permission
                                                permissionLauncher.launch(Manifest.permission.CAMERA)
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.round_library_add_24),
                                                    contentDescription = "Upload from library",
                                                    colorFilter = ColorFilter.tint(
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    "Upload from library"
                                                )
                                            }
                                        },
                                        onClick = {// Permission request logic
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                                requestPermissions.launch(
                                                    arrayOf(
                                                        READ_MEDIA_IMAGES,
                                                        READ_MEDIA_VISUAL_USER_SELECTED
                                                    )
                                                )
                                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                requestPermissions.launch(
                                                    arrayOf(
                                                        READ_MEDIA_IMAGES
                                                    )
                                                )
                                            } else {
                                                requestPermissions.launch(
                                                    arrayOf(
                                                        READ_EXTERNAL_STORAGE
                                                    )
                                                )
                                            }
                                        }
                                    )
                                    if (newUserVm.profileImageSource != ImageSource.MONOGRAM)
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.round_text_fields_24),
                                                        contentDescription = "Monogram",
                                                        colorFilter = ColorFilter.tint(
                                                            MaterialTheme.colorScheme.error
                                                        )
                                                    )
                                                    Spacer(
                                                        modifier = Modifier.width(
                                                            10.dp
                                                        )
                                                    )
                                                    Text(
                                                        text = "Restore default",
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            },
                                            onClick = {
                                                newUserVm.setProfileImage(source = ImageSource.MONOGRAM)
                                                expanded = false
                                            }
                                        )
                                }

                            }

                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row {

                    Text(
                        text = newUserVm.emailValue,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

            }

        }
    }

}

@Composable
fun NewAccountPersonalInformation(orientation: Orientation, userVm: NewAccountViewModel) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    LaunchedEffect(Unit) {
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
                                    userVm.setLocation(it.first()?.locality?:"")
                                }
                            }
                            else userVm.setLocation("")
                        }
                    }
            } catch (e: Exception) {
                userVm.setLocation("")
            }

        } else {
            userVm.setLocation("")
        }

    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {

        Text(
            text = "Personal Information",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp),
                horizontalAlignment = Alignment.Start
            ) {
                OutlinedTextField(
                    readOnly = false,
                    enabled = true,
                    value = userVm.nameValue,
                    singleLine = true,
                    isError = userVm.nameError.isNotBlank(),
                    supportingText = {
                        if(userVm.nameError.isNotBlank())
                            Text(text = userVm.nameError)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("Name") },
                    onValueChange = { name -> userVm.setName(name) },
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp),
                horizontalAlignment = Alignment.End
            ) {
                OutlinedTextField(
                    readOnly = false,
                    enabled = true,
                    value = userVm.surnameValue,
                    singleLine = true,
                    isError = userVm.surnameError.isNotBlank(),
                    supportingText = {
                        if(userVm.surnameError.isNotBlank())
                            Text(text = userVm.surnameError)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("Surname") },
                    onValueChange = { surname -> userVm.setSurname(surname) },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp),
                horizontalAlignment = Alignment.Start
            ) {
                OutlinedTextField(
                    readOnly = false,
                    enabled = true,
                    value = userVm.nicknameValue,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = userVm.nicknameError.isNotBlank(),
                    supportingText = {
                        if(userVm.nicknameError.isNotBlank())
                            Text(text = userVm.nicknameError)
                    },
                    label = { Text("Nickname") },
                    onValueChange = { nickname -> userVm.setNickname(nickname) }
                )
            }


            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp),
                horizontalAlignment = Alignment.End
            ) {
                OutlinedTextField(
                    readOnly = false,
                    enabled = true,
                    singleLine = true,
                    trailingIcon = {
                        IconButton(enabled = true, onClick = {}) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = "Location")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    value = userVm.locationValue,
                    isError = userVm.locationError.isNotBlank(),
                    supportingText = {
                        if(userVm.locationError.isNotBlank())
                            Text(text = userVm.locationError)
                    },
                    label = { Text("Location") },
                    onValueChange = { location -> userVm.setLocation(location) },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxSize(),
                    readOnly = true,
                    enabled = true,
                    value = userVm.birthdateValue,
                    singleLine = true,
                    isError = userVm.birthdateError.isNotBlank(),
                    trailingIcon = {
                        IconButton(enabled = true,
                            onClick = { userVm.setDatePickerDialog() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.round_calendar_today_24),
                                contentDescription = "Date picker"
                            )
                        }
                    },
                    supportingText = {
                        if(userVm.birthdateError.isNotBlank())
                            Text(text = userVm.birthdateError)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Birthdate") },
                    onValueChange = { birthdate -> userVm.setBirthdate(birthdate) },
                )
            }
            var colorExpanded by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxSize(),
                    readOnly = true,
                    enabled = true,
                    value = userVm.color.toString().lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    singleLine = true,
                    isError = false,
                    supportingText = {Text("")},
                    leadingIcon = { Surface(modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape), color = Color(userVm.color.toInt())
                    ) {} },
                    trailingIcon = {
                        IconButton(enabled = true,
                            onClick = { colorExpanded = !colorExpanded  }) {
                            Icon(if(!colorExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp, contentDescription = null)
                        }
                    },
                    label = { Text("Color") },
                    onValueChange = {  },
                )
                DropdownMenu(expanded = colorExpanded, onDismissRequest = { colorExpanded = false }) {
                    ProjectColors.entries.forEach {
                        DropdownMenuItem(
                            leadingIcon = { Surface(modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape), color = Color(it.toInt())
                            ) {} },
                            text = { Text(it.name.lowercase()
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }) },
                            onClick = { userVm.setColor(it.name) })
                    }
                }
            }


            if (userVm.datePickerDialog) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    MyDatePickerDialog(
                        onDateSelected = {
                            userVm.setBirthdate(
                                it.replace(
                                    "/",
                                    "-"
                                )
                            )
                        },
                        onDismiss = { userVm.setDatePickerDialog() },
                        actualBirthdate = userVm.birthdateValue
                    )
                }
            }

        }


        if (orientation == Orientation.PORTRAIT) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                enabled = true,
                readOnly = false,
                value = userVm.bioValue,
                isError = userVm.bioError.isNotBlank(),
                supportingText = {
                    if(userVm.bioError.isNotBlank())
                        Text(text = userVm.bioError)
                },
                label = { Text("Biography") },
                onValueChange = { bio -> userVm.setBio(bio) },
            )
        }

    }
}