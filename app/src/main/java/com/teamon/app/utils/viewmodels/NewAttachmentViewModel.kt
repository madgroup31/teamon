package com.teamon.app.utils.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.teamon.app.Model
import com.teamon.app.attachmentsViewModel
import com.teamon.app.profileViewModel
import com.teamon.app.tasksViewModel
import com.teamon.app.utils.classes.Attachment
import com.teamon.app.utils.graphics.UploadStatus
import com.teamon.app.utils.graphics.copyUriToFile
import com.teamon.app.utils.graphics.getFileType
import kotlinx.coroutines.launch
import java.io.File

class NewAttachmentViewModel(val model: Model, private val taskId: String): ViewModel() {

    var attachmentName by mutableStateOf("")
    private set

    var nameError by mutableStateOf("")
    private set

    var attachmentDescription by mutableStateOf("")
    private set

    var descriptionError by mutableStateOf("")
        private set

    fun setFile(context: Context, uri: Uri) {
        copyUriToFile(context, uri)?.let {
            file = it
            fileType = getFileType(it)
        }
    }

    var fileType by mutableStateOf("None")
        private set

    var file by mutableStateOf<File?>(null)
    private set

    var fileError by mutableStateOf("")
    private set

    fun setName(attachmentName: String) {
        this.attachmentName = attachmentName
    }

    var isShowing by mutableStateOf(false)
    private set

    fun show() {
        isShowing = true
    }

    fun hide() {
        isShowing = false
    }
    fun setDescription(attachmentDescription: String) {
        this.attachmentDescription = attachmentDescription
    }

    fun clear() {
        attachmentName = ""
        attachmentDescription = ""
        nameError = ""
        descriptionError = ""
        file = null
        fileType = "None"
        nameError = ""
        descriptionError = ""
        fileError = ""
        progress = null
    }

    var progress by mutableStateOf<Float?>(null)
    private set

    var error by mutableStateOf("")
    private set

    fun validate(): Boolean {
        if(attachmentName.isBlank())
            nameError = "Name cannot be blank"
        if(attachmentDescription.isBlank())
            descriptionError = "Description cannot be blank"
        if(file == null)
            fileError = "Please select a file to upload."
        return nameError.isBlank() && descriptionError.isBlank() && fileError.isBlank()
    }

    fun upload() {
        if(validate()) {
            viewModelScope.launch {
                val attachment = Attachment(
                    name = attachmentName,
                    description = attachmentDescription,
                    ownerId = profileViewModel.userId,
                    fileType = fileType,
                    downloadUrl = "",
                    fileSize = file?.length() ?: 0
                )

                val attachmentId = attachmentsViewModel.createAttachment(taskId, attachment)
                if (attachmentId != null) {
                    tasksViewModel.uploadFile(attachmentId, file!!).collect { status ->
                        when (status) {
                            is UploadStatus.Progress -> {
                                progress = status.progress
                            }

                            is UploadStatus.Error -> {
                                attachmentsViewModel.deleteAttachment(taskId, attachmentId)
                                error = status.errorMessage
                                clear()
                                hide()
                            }

                            is UploadStatus.Success -> {
                                if(!attachmentsViewModel.updateAttachment(attachmentId = attachmentId,
                                        attachment.copy(uploadedOn = Timestamp.now(), downloadUrl = status.downloadUrl)))
                                    error = "An error occurred while uploading attachment. Please try again."
                                clear()
                                hide()
                            }
                        }
                    }
                }
                else
                    error = "Error uploading attachment. Please try again."
            }
        }
    }

    fun setErrorMessage(e: String?) {
        error = e?:""
    }

}