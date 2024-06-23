package com.teamon.app.teams

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.palette.graphics.Palette
import com.teamon.app.R
import com.teamon.app.account.createImageFile
import com.teamon.app.profileViewModel
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.utils.graphics.StorageAccess
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.getStorageAccess
import com.teamon.app.utils.graphics.toProjectColor
import com.teamon.app.utils.viewmodels.NewTeamViewModel
import java.io.File
import java.util.Objects
import java.util.UUID

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
fun ModalBottomSheetContentTeam(newTeamVM: NewTeamViewModel) {

    val scrollState = rememberScrollState()

    /// NEW
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val file = context.createImageFile(id = UUID.randomUUID().toString())
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        "com.teamon.app" + ".provider", file
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { result ->
            result?.let { it ->
                val originalBitmap: Bitmap
                val stream =
                    context.contentResolver.openInputStream(it)
                originalBitmap = BitmapFactory.decodeStream(stream)

                val convertedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

                convertedBitmap?.let {
                    val palette = Palette.from(it).generate()
                    val dominantColor = palette.getDominantColor(0)
                    val color = dominantColor.toProjectColor()
                    newTeamVM.setTeamImage(ImageSource.LIBRARY, result, color)
                }
                expanded = false
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
                val originalBitmap: Bitmap
                val stream =
                    context.contentResolver.openInputStream(uri)
                originalBitmap = BitmapFactory.decodeStream(stream)

                val convertedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

                convertedBitmap?.let { bitmap ->
                    val palette = Palette.from(bitmap).generate()
                    val dominantColor = palette.getDominantColor(0)
                    val color = dominantColor.toProjectColor()
                    newTeamVM.setTeamImage(ImageSource.CAMERA, uri, color)
                }
                expanded = false
            }
        }

    rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Register ActivityResult handler
    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
        // Handle permission requests results
        if (getStorageAccess(context) == StorageAccess.Full || getStorageAccess(context) == StorageAccess.Partial) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }



    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 40.dp)
            .verticalScroll(scrollState),
    )
    {
        // CREATE NEW TEAM
        Row {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // CREATE A TEAM
                Text(
                    text = "Create New Team",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontStyle = MaterialTheme.typography.titleLarge.fontStyle,
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(id = R.drawable.outline_info_24), contentDescription = "Team image info")
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Team image can be updated after Team creation.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TEAM PICTURE
            Column(
                modifier = Modifier.weight(3f)
            ) {
                Box(modifier = Modifier.size(100.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    ) {

                        TeamOnImage(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            source = newTeamVM.teamImageSource,
                            color = newTeamVM.teamColor,
                            name = newTeamVM.teamName.takeIf { it.isNotBlank() }?:"Team",
                            surname = "",
                            description = "Team image")

                    }
                }

            }

            Spacer(modifier =Modifier.width(15.dp))

            //NAME
            Column(
                modifier = Modifier
                    .weight(7f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = newTeamVM.teamName,
                    onValueChange = { newTeamVM.editName(it) },
                    label = { Text("Team Name") },
                    trailingIcon = {
                                   Icon(
                                       painterResource(id = R.drawable.round_text_fields_24),
                                       contentDescription = "Team Name"
                                   )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = newTeamVM.teamNameError.isNotBlank(),
                )
                if (newTeamVM.teamNameError.isNotBlank()) {
                    Text(
                        newTeamVM.teamNameError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        //DESCRIPTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = newTeamVM.teamDescription,
                    onValueChange = { newTeamVM.editDescription(it) },
                    label = { Text("Team Description") },
                    trailingIcon = {
                        Icon(
                            painterResource(id = R.drawable.round_text_fields_24),
                            contentDescription = "Team Name"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = newTeamVM.teamDescriptionError.isNotBlank(),
                )
                if (newTeamVM.teamDescriptionError.isNotBlank()) {
                    Text(
                        newTeamVM.teamDescriptionError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        // CREATION DATE
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = newTeamVM.teamCreationDate,

                    readOnly = true,
                    enabled = true,
                    onValueChange = {},
                    label = { Text("Team Creation Date") },
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
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        //CATEGORY
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Category
                OutlinedTextField(
                    value = newTeamVM.teamCategory,
                    onValueChange = { newTeamVM.editCategory(it) },
                    label = { Text("Team Category") },
                    trailingIcon = {
                        Icon(
                            painterResource(id = R.drawable.round_text_fields_24),
                            contentDescription = "Team Name"
                        )
                    },
                    isError = newTeamVM.teamCategoryError.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (newTeamVM.teamCategoryError.isNotBlank()) {
                    Text(
                        newTeamVM.teamCategoryError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = true,
                    value = newTeamVM.selectedCollaborators.filter { it.value }
                        .map { it.key }.joinToString("\n") { it.name + " " + it.surname },
                    singleLine = false,
                    trailingIcon = {
                        IconButton(enabled = true,
                            onClick = { newTeamVM.openMembersDialog() }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Members")
                        }
                    },
                    label = { Text("Members") },
                    onValueChange = { }
                )
            }
        }

        if (newTeamVM.isMembersDialog) {
            AssignTeamDialog(newTeamVM = newTeamVM)
        }

        Spacer(modifier = Modifier.height(16.dp))


        Spacer(modifier = Modifier.height(16.dp))

        //RESET - SAVE BUTTON
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            ElevatedButton(onClick = { newTeamVM.reset() }) {
                Text("Reset")
            }

            Spacer(modifier = Modifier.width(5.dp))

            Button(onClick = { newTeamVM.validate() })
            {
                Text("Save")
            }
        }
    }
}


@Composable
fun AssignTeamDialog(newTeamVM: NewTeamViewModel) {
    Dialog(onDismissRequest = { newTeamVM.openMembersDialog() }) {
        OutlinedCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp, bottom = 30.dp)
        ) {
            LazyColumn {
                item {
                    Row {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (newTeamVM.selectedCollaborators.filter { it.value }
                                        .isNotEmpty())
                                    "Members (" + newTeamVM.selectedCollaborators.filter { it.value }.size + ")" else "Members",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                newTeamVM.selectedCollaborators
                    .toList().sortedBy { it.first.name }
                    .forEach {
                    item {
                        MemberTeam(
                            user = it.first,
                            selected = it.second,
                            newTeamVM = newTeamVM
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        ElevatedButton(onClick = {
                            newTeamVM.openMembersDialog()
                        })
                        {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        Button(onClick = {
                            //newTeamVM.updateListUser()
                            newTeamVM.openMembersDialog()
                        }) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun MemberTeam(
    user: User,
    selected: Boolean,
    newTeamVM: NewTeamViewModel
) {

    val userId = profileViewModel.userId //me
    val me = userId == user.userId

    val fullName = if (me) {
        user.name + " " + user.surname + " (Me)"
    } else {
        user.name + " " + user.surname
    }

    ListItem(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp),
        headlineContent = {
            Text(text = fullName, fontWeight = if(me) FontWeight.Bold else FontWeight.Normal) },
        leadingContent = {
            if (me) {
                TeamOnImage(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                    source = profileViewModel.profileImageSource,
                    uri = profileViewModel.profileImageUri,
                    name = profileViewModel.nameValue,
                    color = profileViewModel.color,
                    surname = profileViewModel.surnameValue,
                    description = profileViewModel.nameValue + " " + profileViewModel.surnameValue + " profile image")
            }
            else if(user.profileImage != null)
            {
                TeamOnImage(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                    source = user.profileImageSource,
                    uri = user.profileImage.toUri(),
                    color = user.color,
                    name = user.name,
                    surname = user.surname,
                    description = user.name + " " + user.surname + " profile image")
            }
            else{
                Icon(modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                    painter = painterResource(id = R.drawable.baseline_person_pin_24),
                    contentDescription = "Profile image")
            }
        },
        trailingContent = {
            if(me)
            {
                Checkbox(checked = true,
                    enabled= false,
                    onCheckedChange = {}
                )
            }
            else
            {
                Checkbox(
                    checked = selected,
                    onCheckedChange = {
                        newTeamVM.modifyCollaborators(user,it)
                    }
                )
            }

        }
    )
}