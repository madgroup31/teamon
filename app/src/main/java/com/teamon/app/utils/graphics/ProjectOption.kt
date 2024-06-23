package com.teamon.app.utils.graphics

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.teamon.app.R
import com.teamon.app.profileViewModel
import com.teamon.app.projectsViewModel
import com.teamon.app.utils.classes.Project

@Composable
fun ProjectCardDropdownMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    projectId: String,
    onProjectDelete: (String) -> Unit
) {
    var showingDeletionDialog by remember { mutableStateOf(false) }
    DropdownMenu(
        modifier = Modifier,
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) }
    ) {
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = "Update project") },
            text = { Text("Update") },
            onClick = { /*actions.editProject(selectedTabItem, taskId)*/ }
        )
        val project by projectsViewModel.getProject(projectId).collectAsState(initial = Project())
        val admins by projectsViewModel.getProjectAdmins(projectId).collectAsState(initial = emptyMap())


        if( admins.values.any { it.userId == profileViewModel.userId }) {
            HorizontalDivider()
            DropdownMenuItem(
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.round_delete_24),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
                        contentDescription = "Delete project"
                    )
                },
                text = { Text("Delete project", color = MaterialTheme.colorScheme.error) },
                onClick = { showingDeletionDialog = true }
            )
            if (showingDeletionDialog) {
                AlertDialog(
                    onDismissRequest = { showingDeletionDialog = false },
                    title = { Text(text = "Are you sure you want to delete the project \"" + project.projectName + "\"?") },
                    text = { Text(text = "By continuing, this project will be permanently deleted.") },
                    confirmButton = {
                        ElevatedButton(
                            elevation = ButtonDefaults.elevatedButtonElevation(4.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            onClick = {
                                showingDeletionDialog = false
                                onExpandedChange(false)
                                onProjectDelete(projectId)
                            }
                        ) { Text(text = "Delete") }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showingDeletionDialog = false }
                        ) { Text(text = "Cancel") }
                    }
                )
            }
        }
    }
}