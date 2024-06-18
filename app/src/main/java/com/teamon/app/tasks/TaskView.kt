package com.teamon.app.tasks


import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamon.app.Actions
import com.teamon.app.R
import com.teamon.app.profileViewModel
import com.teamon.app.tasks.attachments.TaskAttachments
import com.teamon.app.tasks.comments.TaskComments
import com.teamon.app.tasks.history.TaskHistory
import com.teamon.app.tasks.info.TaskInfo
import com.teamon.app.usersViewModel
import com.teamon.app.utils.graphics.AppSurface
import com.teamon.app.utils.graphics.Orientation
import com.teamon.app.utils.graphics.ScrollableTab
import com.teamon.app.utils.graphics.TabItem
import com.teamon.app.utils.graphics.Theme
import com.teamon.app.utils.graphics.UploadStatus
import com.teamon.app.utils.viewmodels.Factory
import com.teamon.app.utils.viewmodels.NewAttachmentViewModel
import com.teamon.app.utils.viewmodels.TaskViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskView(
    actions: Actions,
    taskViewModel: TaskViewModel,
    startingTab: String = TabItem.TaskInfo.title
) {
    Theme(color = taskViewModel.projectColor, applyToStatusBar = true) {

        val dialogState = rememberSaveable { mutableStateOf(false) }
        var commentText by rememberSaveable { mutableStateOf("") }
        var search by rememberSaveable { mutableStateOf(false) }
        var query by rememberSaveable { mutableStateOf("") }

        val onCommentTextChange: (String) -> Unit = { commentText = it }
        val onSearchChange: (Boolean) -> Unit = { search = it }
        val isQuerying: () -> Boolean = { query.isNotBlank() && search }
        val onQueryChange: (String) -> Unit = { query = it }

        val items = listOf(
            TabItem.TaskInfo,
            TabItem.TaskHistory,
            TabItem.TaskComments,
            TabItem.TaskAttachments
        )

        val newAttachmentVM = viewModel<NewAttachmentViewModel>(
            factory = Factory(
                LocalContext.current.applicationContext,
                taskId = taskViewModel.taskId
            )
        )

        val pagerState = rememberPagerState(pageCount = { items.size },
            initialPage = items.indexOfFirst { it.title == startingTab })

        var landscape by remember { mutableStateOf(false) }
        landscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (landscape) LandscapeTaskView(
            actions = actions,
            taskViewModel = taskViewModel,
            dialogState = dialogState,
            commentText = commentText,
            onCommentTextChange = onCommentTextChange,
            search = search,
            onSearchChange = onSearchChange,
            pagerState = pagerState,
            query = query,
            isQuerying = isQuerying,
            onQueryChange = onQueryChange,
            newAttachmentVM = newAttachmentVM
        )
        else PortraitTaskView(
            actions = actions,
            taskViewModel = taskViewModel,
            dialogState = dialogState,
            commentText = commentText,
            onCommentTextChange = onCommentTextChange,
            search = search,
            onSearchChange = onSearchChange,
            query = query,
            pagerState = pagerState,
            isQuerying = isQuerying,
            onQueryChange = onQueryChange,
            newAttachmentVM = newAttachmentVM

        )

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LandscapeTaskView(
    actions: Actions,
    taskViewModel: TaskViewModel,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
    isQuerying: () -> Boolean,
    onQueryChange: (String) -> Unit,
    dialogState: MutableState<Boolean>,
    pagerState: PagerState,
    newAttachmentVM: NewAttachmentViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var modified by remember { mutableStateOf(true) }
    val onModifiedChange: (Boolean) -> Unit = { modified = it }
    val items =
        listOf(TabItem.TaskInfo, TabItem.TaskHistory, TabItem.TaskComments, TabItem.TaskAttachments)

    val snackbarHostState = remember { SnackbarHostState() }
    if (taskViewModel.uploadStatus is UploadStatus.Error)
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("An error occurred. Please try again.")
        }

    AppSurface(
        snackbarHostState = snackbarHostState,
        title = taskViewModel.taskName,
        tabActions = {

            ScrollableTab(
                orientation = Orientation.LANDSCAPE,
                items = items,
                pagerState = pagerState,
                selected = pagerState.currentPage,
            )
        },
        orientation = Orientation.LANDSCAPE,
        actions = actions,
        leadingTopBarActions = {
            IconButton(onClick = { actions.navCont.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
            }
        },
        trailingTopBarActions = {
            when (pagerState.currentPage) {
                0 -> {}
                1 -> {}
                2 -> {
                    IconButton(onClick = { onSearchChange(true) }) {
                        Icon(Icons.Rounded.Search, contentDescription = "Search a comment")
                    }
                }

                3 -> {}
            }
        },
        floatingActionButton = {

            when (pagerState.currentPage) {
                0 -> {
                    if (taskViewModel.isAssigned()) {
                        if (!taskViewModel.isEditing) {

                            FloatingActionButton(modifier = Modifier
                                .padding(end = 10.dp),
                                onClick = {
                                    if (taskViewModel.taskStatus == TaskStatus.Overdue) dialogState.value =
                                        true else taskViewModel.edit()
                                }) {


                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                        } else {

                            FloatingActionButton(modifier = Modifier
                                .padding(end = 10.dp), onClick = { taskViewModel.checkAll() }) {

                                Icon(
                                    painter = painterResource(id = R.drawable.round_save_24),
                                    contentDescription = "Save",
                                )
                            }
                        }
                    }
                }

                1 -> {}
                2 -> {

                    Row(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .padding(start = 45.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { onCommentTextChange(it) },
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 1,
                            placeholder = {
                                Text(
                                    text = "Type a comment...",
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        FloatingActionButton(
                            onClick = {
                                if (commentText.isNotBlank()) {


                                    taskViewModel.addComment(
                                        profileViewModel.userId,
                                        commentText
                                    )

                                    keyboardController?.hide()
                                    onModifiedChange(true)
                                    onCommentTextChange("")
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.chat_paste_go_24dp),
                                contentDescription = null
                            )
                        }

                    }
                }

                3 -> {
                    if (taskViewModel.isAssigned())
                        FloatingActionButton(
                            modifier = Modifier
                                .padding(end = 10.dp),
                            onClick = { newAttachmentVM.show() },
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.cloud_upload_24dp),
                                contentDescription = null
                            )
                        }
                }

            }

        }
    ) {
        if (dialogState.value && taskViewModel.taskStatus == TaskStatus.Overdue) {
            AlertDialog(
                onDismissRequest = {
                    dialogState.value = false
                },
                title = {
                    Text(text = "The task is expired")
                },
                text = {
                    Text(text = "If you proceed, you will not be able to modify the end date")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            dialogState.value = false
                            taskViewModel.edit()
                        }
                    ) {
                        Text(text = "Confirm")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            dialogState.value = false
                        }
                    ) {
                        Text(text = "Cancel")
                    }
                }
            )
        }

        HorizontalPager(state = pagerState, pageSpacing = 10.dp) {
            when (it) {
                0 -> TaskInfo(
                    actions = actions,
                    taskViewModel = taskViewModel,
                )

                1 -> {
                    TaskHistory(taskViewModel.history)
                }

                2 -> {
                    TaskComments(
                        actions = actions, modified = modified, onModifiedChange = onModifiedChange,
                        search = search,
                        onSearchChange = onSearchChange,
                        query = query,
                        isQuerying = isQuerying,
                        onQueryChange = onQueryChange,
                        taskViewModel = taskViewModel
                    )
                }

                3 -> {
                    TaskAttachments(
                        taskViewModel.taskId,
                        actions,
                        taskViewModel,
                        snackbarHostState,
                        newAttachmentVM,
                    )
                }
            }
        }
    }

}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PortraitTaskView(
    actions: Actions,
    taskViewModel: TaskViewModel,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    search: Boolean,
    onSearchChange: (Boolean) -> Unit,
    query: String,
    isQuerying: () -> Boolean,
    onQueryChange: (String) -> Unit,
    pagerState: PagerState,
    dialogState: MutableState<Boolean>,
    newAttachmentVM: NewAttachmentViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var modified by remember { mutableStateOf(true) }
    val onModifiedChange: (Boolean) -> Unit = { modified = it }
    val snackbarHostState = remember { SnackbarHostState() }
    if (taskViewModel.uploadStatus is UploadStatus.Error)
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("An error occurred. Please try again.")
        }

    AppSurface(
        snackbarHostState = snackbarHostState,
        title = taskViewModel.taskName,
        orientation = Orientation.PORTRAIT,
        tabActions = {
            val items = listOf(
                TabItem.TaskInfo,
                TabItem.TaskHistory,
                TabItem.TaskComments,
                TabItem.TaskAttachments
            )
            ScrollableTab(
                orientation = Orientation.PORTRAIT,
                items = items,
                pagerState = pagerState,
                selected = pagerState.currentPage,
            )
        },
        actions = actions,
        leadingTopBarActions = {
            IconButton(onClick = { actions.navCont.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
            }
        },
        trailingTopBarActions = {
            when (pagerState.currentPage) {
                0 -> {}
                1 -> {}
                2 -> {
                    IconButton(onClick = { onSearchChange(true) }) {
                        Icon(Icons.Rounded.Search, contentDescription = "Search a comment")
                    }
                }

                3 -> {}
            }
        },
        floatingActionButton = {
            when (pagerState.currentPage) {
                0 -> {
                    if (taskViewModel.isAssigned()) {
                        if (!taskViewModel.isEditing) {
                            FloatingActionButton(onClick = {
                                if (taskViewModel.taskStatus == TaskStatus.Overdue) dialogState.value =
                                    true else taskViewModel.edit()
                            }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                        } else {
                            FloatingActionButton(onClick = { taskViewModel.checkAll() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.round_save_24),
                                    contentDescription = "Save",

                                    )
                            }
                        }
                    }
                }

                1 -> {}
                2 -> {

                    Row(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .padding(start = 35.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { onCommentTextChange(it) },
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 1,
                            placeholder = {
                                Text(
                                    text = "Type a comment...",
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(10.dp))
                        FloatingActionButton(
                            onClick = {
                                if (commentText.isNotBlank()) {

                                    taskViewModel.addComment(
                                        profileViewModel.userId,
                                        commentText
                                    )
                                    keyboardController?.hide()
                                    onModifiedChange(true)
                                    onCommentTextChange("")

                                }
                            }) {
                            Icon(
                                painter = painterResource(id = R.drawable.chat_paste_go_24dp),
                                contentDescription = null
                            )
                        }

                    }

                }

                3 -> {
                    if (taskViewModel.isAssigned())
                    FloatingActionButton(
                        onClick = { newAttachmentVM.show() },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.cloud_upload_24dp),
                            contentDescription = null
                        )
                    }
                }
            }
        }) {


        if (dialogState.value && taskViewModel.taskStatus == TaskStatus.Overdue) {
            AlertDialog(
                modifier = Modifier.wrapContentSize(),
                onDismissRequest = {
                    dialogState.value = false
                },
                title = {
                    Text(text = "The task is expired")
                },
                text = {
                    Text(text = "If you proceed, you will not be able to modify the end date")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            dialogState.value = false
                            taskViewModel.edit()
                        }
                    ) {
                        Text(text = "Confirm")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            dialogState.value = false
                        }
                    ) {
                        Text(text = "Cancel")
                    }
                }
            )
        }

        HorizontalPager(state = pagerState, pageSpacing = 10.dp) {
            when (it) {

                0 -> TaskInfo(
                    actions = actions,
                    taskViewModel = taskViewModel,
                )

                1 -> {
                    TaskHistory(taskViewModel.history)
                }


                2 -> {
                    TaskComments(
                        actions = actions, modified = modified, onModifiedChange = onModifiedChange,
                        search = search,
                        onSearchChange = onSearchChange,
                        query = query,
                        isQuerying = isQuerying,
                        onQueryChange = onQueryChange,
                        taskViewModel = taskViewModel
                    )
                }


                3 -> {
                    TaskAttachments(
                        taskViewModel.taskId,
                        actions,
                        taskViewModel,
                        snackbarHostState,
                        newAttachmentVM,
                    )
                }
            }
        }
    }
}