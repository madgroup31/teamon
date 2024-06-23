package com.teamon.app.utils.classes

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Attachment(
    @DocumentId
    var attachmentId: String = "",
    var ownerId: String = "",
    var name: String = "",
    var description: String = "",
    var fileSize: Long = 0L,
    var fileType: String = "",
    var uploadedOn: Timestamp = Timestamp.now(),
    var downloadUrl: String? = null,
)