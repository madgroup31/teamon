package com.teamon.app.utils.classes

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.teamon.app.tasks.RecurringType
import com.teamon.app.tasks.Repeat
import com.teamon.app.tasks.TaskPriority
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.tasks.TaskStatus

data class Task(
    @DocumentId
    var taskId: String = "",
    val projectName: String = "",
    val description: String = "",
    var taskName: String = "",
    var creationDate: Timestamp = Timestamp.now(),
    var endDate: Timestamp = Timestamp.now(),
    val status: TaskStatus = TaskStatus.Progress,
    val priority: TaskPriority = TaskPriority.Medium,
    val listUser: List<String> = emptyList(),
    val tag: String = "",
    val recurringType: RecurringType = RecurringType.Fixed,
    val recurringSet: String? = null,
    val repeat: Repeat? = null,
    val endRepeat: Timestamp = Timestamp.now(),
    var attachments: List<String> = listOf(),
    var history: List<String> = listOf(),
    var comments: List<String> = listOf()
)