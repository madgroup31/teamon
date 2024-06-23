package com.teamon.app.utils.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamon.app.Model
import com.teamon.app.utils.classes.Attachment
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class AttachmentsViewModel(val model: Model): ViewModel() {
    fun getAttachment(attachmentId: String) = model.getAttachment(attachmentId)

    fun getTaskAttachments(taskId: String) : Flow<Map<String, Attachment>> = channelFlow {
        val attachments = mutableMapOf<String, Attachment>()
        model.getTask(taskId).collect{ task ->
            attachments.clear()
            task.attachments.forEach {
                launch {
                    model.getAttachment(it).collect {attachment ->
                        attachments[attachment.attachmentId] = attachment
                        send(attachments)
                    }
                }
            }
        }
        awaitClose {  }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    suspend fun createAttachment(taskId: String, attachment: Attachment) = model.createAttachment(taskId, attachment)

    suspend fun updateAttachment(attachmentId: String, attachment: Attachment) = model.updateAttachment(attachmentId, attachment)

    suspend fun deleteAttachment(taskId: String, attachmentId: String) = model.deleteAttachment(taskId, attachmentId)
}

