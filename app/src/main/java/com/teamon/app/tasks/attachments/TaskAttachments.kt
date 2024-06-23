package com.teamon.app.tasks.attachments

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.attachmentsViewModel
import com.teamon.app.profileViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Attachment
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.AnimatedGrid
import com.teamon.app.utils.graphics.AnimatedItem
import com.teamon.app.utils.graphics.FilePickerContract
import com.teamon.app.utils.graphics.TeamOnImage
import com.teamon.app.utils.graphics.asPastRelativeDateTime
import com.teamon.app.utils.graphics.getFileTypeIcon
import com.teamon.app.utils.viewmodels.NewAttachmentViewModel
import com.teamon.app.utils.viewmodels.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TaskAttachmentInfo(attachment: Attachment, actions: Actions) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp)),
        tonalElevation = 5.dp,
        shadowElevation = 5.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(5f)) {
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                modifier = Modifier
                                    .size(30.dp),
                                painter = painterResource(id = getFileTypeIcon(attachment.fileType)),
                                contentDescription = "File icon",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                attachment.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Box {
                            Box {
                                Image(
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                    painter = painterResource(id = R.drawable.rounded_cloud_download_24),
                                    contentDescription = "File downloaded"
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row {
                    Column {
                        OutlinedTextField(
                            label = { Text("Name") },
                            value = attachment.name,
                            maxLines = 1,
                            onValueChange = {},
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        OutlinedTextField(
                            label = { Text("Description") },
                            value = attachment.description,
                            maxLines = 3,
                            onValueChange = {},
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        OutlinedTextField(
                            label = { Text("File Type") },
                            value = attachment.fileType,
                            onValueChange = {},
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        if (attachment.ownerId != "") {
                            val author by usersViewModel!!.getUser(attachment.ownerId)
                                .collectAsState(initial = User())
                            OutlinedTextField(
                                label = { Text("Owner") },
                                trailingIcon = {
                                    TeamOnImage(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape),
                                        source = author.profileImageSource,
                                        uri = author.profileImage?.toUri(),
                                        name = author.name,
                                        surname = author.surname,
                                        color = author.color,
                                        description = author.name + " " + author.surname + " profile image"
                                    )

                                },
                                value = author.name + " " + author.surname,
                                onValueChange = {},
                                readOnly = true
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                        Row {
                            Column(modifier = Modifier.weight(1.5f)) {
                                OutlinedTextField(
                                    label = { Text("Uploaded on") },
                                    value = attachment.uploadedOn.asPastRelativeDateTime(),
                                    singleLine = true,
                                    onValueChange = {},
                                    readOnly = true
                                )
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    label = { Text("File Size") },
                                    value = String.format(
                                        "%.2f",
                                        (attachment.fileSize.toFloat() / (1024 * 1024))
                                    ) + " MB",
                                    onValueChange = {},
                                    readOnly = true
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(15.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    ElevatedButton(onClick = { actions.navCont.popBackStack() }) {
                        Text("Close")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val intent =
                                Intent(Intent.ACTION_VIEW, Uri.parse(attachment.downloadUrl))
                            launcher.launch(intent)
                        }
                    }) {
                        Text("Open")
                    }
                }
            }
        }
    }

}

@Composable
fun TaskAttachment(
    taskId: String,
    attachment: Attachment,
    actions: Actions,
    snackbarHostState: SnackbarHostState
) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    var showDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable {
                CoroutineScope(Dispatchers.IO).launch {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(attachment.downloadUrl))
                    launcher.launch(intent)
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(40.dp)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center),
                        painter = painterResource(id = getFileTypeIcon(attachment.fileType)),
                        contentDescription = "File icon",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(4f), horizontalAlignment = Alignment.Start) {
            Text(
                attachment.name,
                maxLines = 1,
                color = MaterialTheme.colorScheme.primary,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                attachment.description,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp
            )

        }
        Column(
            modifier = Modifier.weight(2f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {

                IconButton(onClick = {
                    actions.openAttachmentInfo(
                        actions.navCont.currentBackStackEntry!!.destination.route!!.split("/")
                            .first(), taskId, attachment.attachmentId
                    )
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.outline_info_24),
                        contentDescription = "Download file",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                }
                if (profileViewModel!!.userId == attachment.ownerId)
                    IconButton(onClick = {
                        showDialog = true
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.round_delete_24),
                            contentDescription = "Delete file",
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error)
                        )
                    }
            }
        }
    }
    if (showDialog)
        AlertDialog(
            title = { Text("Delete the attachment \"" + attachment.name + "\"?") },
            text = { Text("Are you sure you want to delete this attachment? This operation is irreversible.") },
            onDismissRequest = { showDialog = false },
            dismissButton = {
                ElevatedButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (!attachmentsViewModel.deleteAttachment(
                                    taskId = taskId,
                                    attachmentId = attachment.attachmentId
                                )
                            )
                                snackbarHostState.showSnackbar("An error occurred while deleting the attachment. Please try again.")
                            showDialog = false
                        }
                    }) {
                    Text("Delete")
                }
            })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskAttachments(
    taskId: String,
    actions: Actions,
    taskViewModel: TaskViewModel,
    snackbarHostState: SnackbarHostState,
    newAttachmentVM: NewAttachmentViewModel
) {
    val attachments = taskViewModel.attachments
    val context = LocalContext.current.applicationContext

    val filePickerLauncher = rememberLauncherForActivityResult(FilePickerContract()) { uri: Uri? ->
        uri?.let {
            newAttachmentVM.setFile(context, it)
        }
    }

    LaunchedEffect(key1 = newAttachmentVM.error) {
        if(newAttachmentVM.error.isNotBlank()) {
            snackbarHostState.showSnackbar(newAttachmentVM.error)
            newAttachmentVM.setErrorMessage("")
        }

    }

    if (attachments.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedItem(index = 1) {
                Text(
                    text = "No Attachments available yet.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic
                )
            }

        }
    } else

        AnimatedGrid(
            modifier = Modifier.fillMaxSize(),
            columns = StaggeredGridCells.Adaptive(250.dp),
            items = attachments
        ) { it, index ->
            TaskAttachment(taskId, it as Attachment, actions, snackbarHostState)
            if (index < attachments.size - 1)
                HorizontalDivider()
        }

    val sheetState = rememberModalBottomSheetState()
    if (newAttachmentVM.isShowing)
        ModalBottomSheet(sheetState = sheetState, onDismissRequest = { newAttachmentVM.hide() }) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 40.dp)
                    .verticalScroll(scrollState),
            ) {
                Row {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(5f)
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        if (newAttachmentVM.progress == null)
                            Text(
                                text = "Upload Attachment",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                fontStyle = MaterialTheme.typography.titleLarge.fontStyle,
                            )
                        else
                            Text(
                                text = "Uploading Attachment...",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                fontStyle = MaterialTheme.typography.titleLarge.fontStyle,
                            )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(2f)
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (newAttachmentVM.progress != null)
                            CircularProgressIndicator()

                    }

                }
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
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = false,
                            enabled = true,
                            value = newAttachmentVM.attachmentName,
                            onValueChange = { name -> newAttachmentVM.setName(name) },
                            trailingIcon = {
                                Image(
                                    painter = painterResource(R.drawable.round_text_fields_24),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                    contentDescription = "New Task name"
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            label = { Text("File Name") },
                            isError = newAttachmentVM.nameError.isNotBlank(),
                            supportingText = {
                                if (newAttachmentVM.nameError.isNotBlank()) {
                                    Text(
                                        newAttachmentVM.nameError,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )

                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = false,
                            enabled = true,
                            value = newAttachmentVM.attachmentDescription,
                            trailingIcon = {
                                Image(
                                    painter = painterResource(R.drawable.round_text_fields_24),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                    contentDescription = "New Task name"
                                )
                            },
                            singleLine = false,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            label = { Text("Description") },
                            onValueChange = { description ->
                                newAttachmentVM.setDescription(
                                    description
                                )
                            },
                            isError = newAttachmentVM.descriptionError.isNotBlank(),
                            supportingText = {
                                if (newAttachmentVM.descriptionError.isNotBlank()) {
                                    Text(
                                        newAttachmentVM.descriptionError,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )

                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = newAttachmentVM.file != null,
                            value = newAttachmentVM.file?.canonicalFile?.path ?: "",
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.round_text_fields_24),
                                    contentDescription = "New Task name"
                                )
                            },
                            singleLine = false,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            label = { Text("File") },
                            onValueChange = {},
                            isError = newAttachmentVM.descriptionError.isNotBlank(),
                            supportingText = {
                                if (newAttachmentVM.fileError.isNotBlank()) {
                                    Text(
                                        newAttachmentVM.fileError,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
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
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = newAttachmentVM.file != null,
                            value = newAttachmentVM.fileType,
                            trailingIcon = {
                                Icon(
                                    modifier = Modifier.size(28.dp),
                                    painter = painterResource(getFileTypeIcon(newAttachmentVM.fileType)),
                                    contentDescription = "New Task name"
                                )
                            },
                            singleLine = false,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            label = { Text("File Type") },
                            onValueChange = {},
                        )
                    }
                    Spacer(modifier = Modifier.weight(0.2f))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = newAttachmentVM.file != null,
                            value = String.format(
                                "%.2f",
                                ((newAttachmentVM.file?.length()?.toFloat() ?: 0f) / (1024 * 1024))
                            ) + " MB",
                            singleLine = false,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            label = { Text("File Size") },
                            onValueChange = {},
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedButton(onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                filePickerLauncher.launch()
                            }
                        }) {
                            Text("Choose a file...")
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ElevatedButton(onClick = { newAttachmentVM.clear(); newAttachmentVM.hide() }) {
                            Text("Cancel")
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (newAttachmentVM.progress == null) {
                            Button(onClick = { newAttachmentVM.upload() }) {

                                Text("Upload")
                            }
                        }
                    }
                }
            }
        }
}