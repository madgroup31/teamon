package com.teamon.app.utils.classes

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.teamon.app.utils.graphics.HistoryIcons

data class History (
    @DocumentId
    val historyId: String = "",
    val text: String = "",
    val icon: HistoryIcons = HistoryIcons.CREATION,
    val user: String = "",
    val timestamp: Timestamp = Timestamp.now()
)