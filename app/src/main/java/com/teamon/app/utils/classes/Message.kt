package com.teamon.app.utils.classes

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Message(
    @DocumentId
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    var unread : List<String> = emptyList(),
)