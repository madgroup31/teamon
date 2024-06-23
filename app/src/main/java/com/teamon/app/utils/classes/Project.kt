package com.teamon.app.utils.classes

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.ProjectImages

data class Project(
    @DocumentId
    var projectId: String = "",
    val projectName: String = "",
    val projectImage: ProjectImages = ProjectImages.WARNING,
    val projectColor: ProjectColors = ProjectColors.PURPLE,
    val description: String = "",
    val favorite: Boolean = false,
    val endDate: Timestamp = Timestamp.now(),
    val chatBadge: Boolean = false,
    var tasks: List<String> = emptyList(),
    val progress: Float = 0f,
    val performances: List<String> = emptyList(),
    val feedbacks: List<String> = emptyList(),
    val teams: List<String> = emptyList(),
)