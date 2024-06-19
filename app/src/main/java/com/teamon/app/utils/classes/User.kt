package com.teamon.app.utils.classes

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.utils.graphics.ProjectColors

data class User (
    @DocumentId
    val userId: String = "",
    val name: String = "",
    val surname: String = "",
    val nickname: String = "",
    val email: String = "",
    val location: String = "",
    val birthdate: Timestamp = Timestamp.now(),
    val lastUpdate: Timestamp = Timestamp.now(),
    val profileImageSource: ImageSource = ImageSource.MONOGRAM,
    val profileImage: String? = null,
    val biography: String = "",
    val feedbacks: List<String> = emptyList(),
    val favorites: List<String> = emptyList(),
    val color: ProjectColors = ProjectColors.entries.toTypedArray().random(),
)

