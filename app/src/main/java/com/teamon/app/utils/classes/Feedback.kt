package com.teamon.app.utils.classes

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Feedback(
    @DocumentId
    val feedbackId: String = "",
    val authorId: String = "",
    val description: String = "",
    val value: Int = 0,
    val anonymous: Boolean = true,
    val timestamp: Timestamp = Timestamp.now(),
)