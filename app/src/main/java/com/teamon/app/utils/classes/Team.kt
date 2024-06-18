package com.teamon.app.utils.classes

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.utils.graphics.ProjectColors

data class Team (
    @DocumentId
    var teamId: String = "",
    var name: String = "",
    var description: String = "",
    var image: String = "",
    val imageSource: ImageSource = ImageSource.REMOTE,
    var admin: List<String> = listOf(),
    var users: List<String> = emptyList(),
    val category: String = "",
    val creationDate: Timestamp = Timestamp.now(),
    val feedback: List<String> = emptyList(),
    val performance: List<String> = emptyList(),
    val color: ProjectColors = ProjectColors.PURPLE,
)