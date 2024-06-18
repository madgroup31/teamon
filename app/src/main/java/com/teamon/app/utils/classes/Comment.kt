package com.teamon.app.utils.classes

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Comment (
    @DocumentId
    val commentId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val author: String = "",
    val text: String = ""
)