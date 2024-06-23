package com.teamon.app.account

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.teamon.app.R
import com.teamon.app.profileViewModel
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.utils.graphics.LoadingOverlay
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.StorageAccess
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.UploadStatus
import com.teamon.app.utils.graphics.asPastRelativeDateTime
import com.teamon.app.utils.graphics.getStorageAccess
import com.teamon.app.utils.viewmodels.NewAccountViewModel
import com.teamon.app.utils.viewmodels.UserViewModel
import java.io.File
import java.util.Objects

fun Context.createImageFile(id: String): File {
    // Create an image file nameValue
    val imageFileName = id
    return File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
}

fun saveImageToGallery(context: Context, bitmap: Bitmap, filename: String) {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //use this if you want the image to be stored in the "Pictures" directory
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        } else {
            put(
                MediaStore.MediaColumns.DATA,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString() + File.separator + filename
            )
        }
    }

    val uri =
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        context.contentResolver.openOutputStream(it)?.let { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        }
    }
}

@Composable
fun AccountQuickInfo(
    orientation: Orientation,
    userVm: UserViewModel? = null,
    isSigningUp: Boolean = false
) {
    when (userVm) {
        null -> {
            var expanded by remember { mutableStateOf(false) }

            val context = LocalContext.current
            val file = context.createImageFile(profileViewModel.userId)
            val uri = FileProvider.getUriForFile(
                Objects.requireNonNull(context),
                "com.teamon.app" + ".provider", file
            )

            val galleryLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
                onResult = { result ->
                    result?.let {
                        profileViewModel.setProfileImage(
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
                        profileViewModel.setProfileImage(ImageSource.CAMERA, uri, context)
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
                                    LoadingOverlay(isLoading = profileViewModel.uploadStatus is UploadStatus.Progress)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .align(Alignment.Center)
                                    ) {
                                        TeamOnImage(
                                            modifier = if (profileViewModel.isEditing) Modifier
                                                .size(100.dp)
                                                .clip(CircleShape)
                                                .align(Alignment.Center)
                                                .alpha(0.5f) else Modifier
                                                .size(100.dp)
                                                .align(Alignment.Center)
                                                .clip(CircleShape),
                                            source = profileViewModel.profileImageSource,
                                            uri = profileViewModel.profileImageUri,
                                            name = profileViewModel.nameValue,
                                            color = profileViewModel.color,
                                            surname = profileViewModel.surnameValue,
                                            description = "My Profile picture"
                                        )

                                        if (profileViewModel.isEditing)

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .align(Alignment.Center)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Edit,
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .align(Alignment.Center)
                                                        .clickable {
                                                            expanded = !expanded
                                                        },
                                                    contentDescription = "Update profile picture"
                                                )



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
                                                    if (profileViewModel.profileImageSource != ImageSource.MONOGRAM)
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
                                                                profileViewModel.setProfileImage(
                                                                    source = ImageSource.MONOGRAM
                                                                )
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
                                    text = profileViewModel.emailValue,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 18.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    textAlign = TextAlign.Center,
                                    text = "Last Update: " + if (isSigningUp) "N/D" else profileViewModel.lastUpdate.asPastRelativeDateTime(),
                                    maxLines = 2,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic
                                )

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
                                LoadingOverlay(isLoading = profileViewModel.uploadStatus is UploadStatus.Progress)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.Center)
                                ) {
                                    TeamOnImage(
                                        modifier = if (profileViewModel.isEditing) Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .align(Alignment.Center)
                                            .alpha(0.5f) else Modifier
                                            .size(100.dp)
                                            .align(Alignment.Center)
                                            .clip(CircleShape),
                                        source = profileViewModel.profileImageSource,
                                        uri = profileViewModel.profileImageUri,
                                        name = profileViewModel.nameValue,
                                        color = profileViewModel.color,
                                        surname = profileViewModel.surnameValue,
                                        description = "My Profile picture"
                                    )


                                    if (profileViewModel.isEditing)
                                        Icon(
                                            Icons.Rounded.Edit,
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .align(Alignment.Center)
                                                .clickable {
                                                    expanded = !expanded
                                                },
                                            contentDescription = "Update profile picture"
                                        )



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
                                            onClick = {
                                                galleryLauncher.launch("image/*")
                                            }
                                        )
                                        if (profileViewModel.profileImageSource != ImageSource.MONOGRAM)
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
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = "Restore default",
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    profileViewModel.setProfileImage(source = ImageSource.MONOGRAM)
                                                    expanded = false
                                                }
                                            )
                                    }

                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row {

                            Text(
                                text = profileViewModel.emailValue,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 18.sp,
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row {
                            Text(
                                textAlign = TextAlign.Center,
                                text = "Last Update: " + profileViewModel.lastUpdate.asPastRelativeDateTime(),
                                maxLines = 2,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }

                }
            }

        }

        else -> {

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
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .align(Alignment.Center)
                                    ) {
                                        TeamOnImage(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(CircleShape)
                                                .align(Alignment.Center),
                                            source = userVm.profileImageSource,
                                            uri = userVm.profileImage.toUri(),
                                            name = userVm.nameValue,
                                            color = userVm.color,
                                            surname = userVm.surnameValue,
                                            description = userVm.nameValue + " " + userVm.surnameValue + " profile picture"
                                        )
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
                                    text = userVm.emailValue,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 18.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    textAlign = TextAlign.Center,
                                    text = "Last Update: " + userVm.lastUpdate.asPastRelativeDateTime(),
                                    maxLines = 2,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic
                                )

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
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.Center)
                                ) {
                                    TeamOnImage(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .align(Alignment.Center),
                                        source = userVm.profileImageSource,
                                        uri = userVm.profileImage.toUri(),
                                        name = userVm.nameValue,
                                        surname = userVm.surnameValue,
                                        color = userVm.color,
                                        description = userVm.nameValue + " " + userVm.surnameValue + " profile picture"
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row {

                            Text(
                                text = userVm.emailValue,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 18.sp,
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row {
                            Text(
                                textAlign = TextAlign.Center,
                                text = "Last Update: " + userVm.lastUpdate.asPastRelativeDateTime(),
                                maxLines = 2,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }

                }
            }

        }
    }

}

