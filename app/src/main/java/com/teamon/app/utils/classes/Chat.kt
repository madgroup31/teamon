package com.teamon.app.utils.classes

import com.google.firebase.firestore.DocumentId

data class Chat(
    @DocumentId
    val chatId: String = "",
    val teamId: String = "",
    val userIds: List<String> = listOf(),
    val personal : Boolean = true,
)