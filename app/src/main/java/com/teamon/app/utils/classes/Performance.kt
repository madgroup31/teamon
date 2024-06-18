package com.teamon.app.utils.classes

import com.google.firebase.firestore.DocumentId
import java.util.UUID

class Performance(
    @DocumentId
    var performanceId: String = UUID.randomUUID().toString(),
    var year: Int = 0,
    var list: IntArray = IntArray(12)
)