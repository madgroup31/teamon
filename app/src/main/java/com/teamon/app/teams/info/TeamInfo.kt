package com.teamon.app.myteams

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.palette.graphics.Palette
import com.teamon.app.R
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.account.createImageFile
import com.teamon.app.profileViewModel
import com.teamon.app.teams.saveImageToGallery
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.LoadingOverlay
import com.teamon.app.utils.graphics.StorageAccess
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.UploadStatus
import com.teamon.app.utils.graphics.asDate
import com.teamon.app.utils.graphics.getStorageAccess
import com.teamon.app.utils.graphics.toProjectColor
import com.teamon.app.utils.viewmodels.TeamViewModel
import java.util.Objects

@Composable
fun InfoActions() {

}

@Composable
fun TeamInfo(teamVM: TeamViewModel, snackbarHostState: SnackbarHostState) {

    /// NEW
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val file = context.createImageFile(id = teamVM.teamId)
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        "com.teamon.app" + ".provider", file
    )

    LaunchedEffect(teamVM.error) {
        teamVM.error?.let {
            snackbarHostState.showSnackbar(it)
        }
        teamVM.setErrorString(null)
    }


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                var originalBitmap: Bitmap =
                    Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
                val stream =
                    context.contentResolver.openInputStream(it)
                originalBitmap = BitmapFactory.decodeStream(stream)

                val convertedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

                convertedBitmap?.let {
                    val palette = Palette.from(it).generate()
                    val dominantColor = palette.getDominantColor(0)
                    val color = dominantColor.toProjectColor()
                    teamVM.setTeamImage(ImageSource.LIBRARY, uri, context, color)
                }
            }
        }
    )

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
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
                var originalBitmap: Bitmap =
                    Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
                val stream =
                    context.contentResolver.openInputStream(uri)
                originalBitmap = BitmapFactory.decodeStream(stream)

                val convertedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

                convertedBitmap?.let {
                    val palette = Palette.from(it).generate()
                    val dominantColor = palette.getDominantColor(0)
                    val color = dominantColor.toProjectColor()
                    teamVM.setTeamImage(ImageSource.CAMERA, uri, context, color)
                }
                expanded = false
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

    LazyColumn(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            AnimatedItem(index = 1) {
                Column {
                    Box(modifier = Modifier.size(100.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.BottomCenter)
                        ) {
                            LoadingOverlay(isLoading = teamVM.uploadStatus is UploadStatus.Progress)
                                TeamOnImage(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                    source = teamVM.teamImageSource,
                                    name = teamVM.teamName,
                                    surname = "",
                                    uri = teamVM.teamImage,
                                    contentScale = ContentScale.Crop,
                                    description = "Team Picture"
                                )


                            if (teamVM.isEditing) {
                                IconButton(
                                    modifier = Modifier.fillMaxSize(),
                                    colors = IconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(
                                            alpha = 0.75f
                                        ),
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                        disabledContainerColor = Color.Transparent,
                                        disabledContentColor = Color.Transparent
                                    ),
                                    onClick = { expanded = !expanded }) {
                                    Icon(
                                        Icons.Rounded.Edit,
                                        contentDescription = "Update profile picture"
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
                                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
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
                                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
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
                                                        Manifest.permission.READ_MEDIA_IMAGES,
                                                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                                                    )
                                                )
                                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                requestPermissions.launch(
                                                    arrayOf(
                                                        Manifest.permission.READ_MEDIA_IMAGES
                                                    )
                                                )
                                            } else {
                                                requestPermissions.launch(
                                                    arrayOf(
                                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            AnimatedItem(index = 2) {
                Column {
                    //Name
                    OutlinedTextField(
                        value = teamVM.teamName,
                        onValueChange = { teamVM.updateName(it) },
                        label = { Text("Team Name") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = teamVM.teamNameError.isNotBlank(),
                        readOnly = !teamVM.isEditing
                    )
                    if (teamVM.teamNameError.isNotBlank()) {
                        Text(
                            teamVM.teamNameError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    //Description
                    OutlinedTextField(
                        value = teamVM.teamDescription,
                        onValueChange = { teamVM.updateDescription(it) },
                        label = { Text("Team Description") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = teamVM.teamCategoryError.isNotBlank(),
                        readOnly = !teamVM.isEditing
                    )
                    if (teamVM.teamDescriptionError.isNotBlank()) {
                        Text(
                            teamVM.teamDescriptionError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    //Creation Data
                    OutlinedTextField(
                        value = teamVM.teamCreationDate,
                        onValueChange = { },
                        label = { Text("Team Creation Date") },
                        readOnly = true,
                        enabled = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Image(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = R.drawable.round_calendar_today_24),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                contentDescription = "Date picker"
                            )
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    //Category
                    OutlinedTextField(
                        value = teamVM.teamCategory,
                        onValueChange = { teamVM.updateCategory(it) },
                        label = { Text("Team Category") },
                        isError = teamVM.teamCategoryError.isNotBlank(),
                        readOnly = !teamVM.isEditing,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (teamVM.teamCategoryError.isNotBlank()) {
                        Text(
                            teamVM.teamCategoryError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (teamVM.isConfirmDialogShow) {
        AlertDialog(
            modifier = Modifier.wrapContentSize(),
            onDismissRequest = { teamVM.toggleConfirmDialog() },
            title = { Text(text = "Confirm changes") },
            text = {
                Text(text = "Are you sure to save the changes ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        teamVM.validate()
                        teamVM.toggleConfirmDialog()
                    }
                ) {
                    Text(text = "Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        teamVM.toggleConfirmDialog()
                    }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }


}
