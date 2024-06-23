package com.teamon.app.utils.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.teamon.app.Model
import com.teamon.app.attachmentsViewModel
import com.teamon.app.commentsViewModel
import com.teamon.app.historyViewModel
import com.teamon.app.projectsViewModel
import com.teamon.app.tasks.RecurringType
import com.teamon.app.tasks.Repeat
import com.teamon.app.tasks.TaskPriority
import com.teamon.app.tasks.TaskStatus
import com.teamon.app.tasksViewModel
import com.teamon.app.utils.classes.Attachment
import com.teamon.app.utils.classes.Comment
import com.teamon.app.utils.classes.History
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.UploadStatus
import com.teamon.app.utils.graphics.asDate
import com.teamon.app.utils.graphics.toTimestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.util.Calendar
import java.util.Locale

class TaskViewModel(val model: Model, var taskId: String) : ViewModel(){

    var task: StateFlow<Task> = model.getTask(taskId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Task())
    var projectMembers = mutableStateMapOf<String,User>()
    var projectId by mutableStateOf("")

    var projectColor by mutableStateOf(ProjectColors.PURPLE)

    var attachments: MutableList<Attachment> = mutableStateListOf()
    private set

    var history = mutableStateListOf<History>()
        private set

    var comments = mutableStateListOf<Comment>()
        private set

    var listUser by mutableStateOf(emptyList<String>())

    var uploadStatus by mutableStateOf(Any())
        private set

    private var updatingTask: Job? = null
    private var updatingAttachments: Job? = null
    private var updatingHistory: Job? = null
    private var updatingComments: Job? = null

    private fun startCollectingTask() {
        updatingTask = viewModelScope.launch {
            tasksViewModel.getTask(taskId).collect {
                taskName = it.taskName
                taskTag = it.tag
                taskStatus = it.status
                taskRepeat = it.repeat?: Repeat.Daily
                taskRecurringType = it.recurringType
                projectName = it.projectName
                taskPriority = it.priority
                listUser = it.listUser.toMutableStateList()
                taskEndRepeat = it.endRepeat.asDate()
                taskEndDate = it.endDate.asDate()
                taskDescription = it.description
                taskCreationDate = it.creationDate.asDate()
            }
        }
    }

    private fun startCollectingAttachments() {
        updatingAttachments = viewModelScope.launch {
            attachmentsViewModel.getTaskAttachments(taskId).collect {
                attachments.clear()
                attachments.addAll(it.values)
            }
        }
    }

    private fun startCollectingHistory() {
        updatingHistory = viewModelScope.launch {
            historyViewModel.getTaskHistory(taskId).collect {
                history.clear()
                history.addAll(it.values)
            }
        }
    }

    private fun startCollectingComments() {
        updatingComments = viewModelScope.launch {
            commentsViewModel.getTaskComments(taskId).collect {
                comments.clear()
                comments.addAll(it.values)
            }
        }
    }

    private fun stopCollectingTask() {
        updatingTask?.cancel()
        updatingTask = null
    }


    init {
        startCollectingTask()
        startCollectingAttachments()
        startCollectingHistory()
        startCollectingComments()
        viewModelScope.launch {
            tasksViewModel.getTaskProject(taskId).collect{ project ->
                projectId = project.projectId
                projectColor = project.projectColor
                projectsViewModel.getProjectMembers(projectId).collect {
                    projectMembers.clear()
                    projectMembers.putAll(it)
                }
            }
        }
    }

    private var originalTask = Task()

    var isEditing by mutableStateOf(false)
        private set

    fun isAssigned(): Boolean {
        return listUser.contains(Firebase.auth.currentUser!!.uid)
    }

    fun edit() {
        if (isAssigned()) {
            isEditing = true
            stopCollectingTask()
            originalTask = task.value
        }
    }

    fun updateListUser(user: User) {
        Log.d("listUser", "updating userId: ${user.userId}")
        listUser = if(listUser.contains(user.userId)) {
            listUser.filter { it != user.userId }.toMutableStateList()
        } else {
            listUser + user.userId
        }.toMutableStateList()
        listUser.forEach{
            Log.d("listUser", "userId: $it")
        }
    }

    var projectName by mutableStateOf("")
    var taskCreationDate by mutableStateOf("")
    var taskName by mutableStateOf("")
        private set
    var taskNameError by mutableStateOf("")
        private set
    fun updateTaskName(name: String) {
        taskName = name
    }
    private fun checkName(){
        taskNameError = if (taskName.isBlank()) {
            "Task name cannot be blank"
        } else ""
    }

    var taskDescription by mutableStateOf("")
        private set
    var taskDescriptionError by mutableStateOf("")
        private set
    fun updateTaskDescription(description: String) {
        taskDescription = description
    }
    private fun checkDescription(){
        taskDescriptionError = if (taskDescription.isBlank()) {
            "Task name cannot be blank"
        } else ""
    }

    var taskEndDate by mutableStateOf("")
        private set
    var taskEndDateError by mutableStateOf("")
        private set
    fun updateTaskEndDate(endDate: String) {
        taskEndDate = endDate
    }
    private fun checkEndDate(){
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        return try {
            if (taskEndDate.isBlank())
                throw DateTimeException("Task should have a deadline")
            val dateOfTaskEndDate = dateFormat.parse(taskEndDate)

            if(taskRecurringType == RecurringType.Recursive) {
                val dateOfEndRepeat = dateFormat.parse(taskEndRepeat)

                if (dateOfTaskEndDate!!.after(dateOfEndRepeat))
                    throw DateTimeException("Task deadline should be set before end recurrence")
            }
            val calendar = Calendar.getInstance()
            if (dateOfTaskEndDate!!.before(calendar.time))
                throw DateTimeException("Task deadline should be set to the future")
            taskEndDateError = ""
        } catch (e: Exception) {
            when (e) {
                is DateTimeException -> {
                    taskEndDateError = e.message.toString()
                }
                else -> {
                    taskEndDateError = "Invalid task deadline"
                }
            }
        }
    }

    var taskPriority by mutableStateOf(TaskPriority.Medium)
        private set
    private var taskPriorityError by mutableStateOf("")
    fun updateTaskPriority(priority: TaskPriority) {
        taskPriority = priority
    }
    private fun checkPriority(){
        // should be always set with default options
    }

    var taskStatus by mutableStateOf(TaskStatus.Progress)
        private set
    private var taskStatusError by mutableStateOf("")
    fun updateTaskStatus(status: TaskStatus) {
        taskStatus = status
    }
    private fun checkStatus(){
        // should be always set with default options
    }


    var taskRecurringType by mutableStateOf(RecurringType.Fixed)
        private set

    var taskRepeat by mutableStateOf(Repeat.Daily)
        private set
    var taskRepeatError by mutableStateOf("")
        private set
    fun updateTaskRepeat(repeat: Repeat) {
        taskRepeat = repeat
    }
    private fun checkRepeat(){
        taskRepeatError = ""
    }

    var taskEndRepeat by mutableStateOf("")
        private set
    var taskEndRepeatError by mutableStateOf("")
        private set
    fun updateTaskEndRepeat(endRepeat: String) {
        taskEndRepeat = endRepeat
    }
    private fun checkEndRepeat(){
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        return try {
            if (taskEndRepeat.isBlank())
                throw DateTimeException("When do you wanna stop the recurrence?")
            val dateOfEndRepeat = dateFormat.parse(taskEndRepeat)


            val dateOfEndDate = dateFormat.parse(taskEndDate)
            if (dateOfEndRepeat!!.before(dateOfEndDate))
                throw DateTimeException("Recurrence deadline should be set after the task deadline")
            val calendar = Calendar.getInstance()
            if (dateOfEndRepeat.before(calendar.time))
                throw DateTimeException("Recurrence deadline should be set to the future")
            taskEndRepeatError = ""
        } catch (e: Exception) {
            when (e) {
                is DateTimeException -> {
                    taskEndRepeatError = e.message.toString()
                }
                else -> {
                    taskEndRepeatError = "Invalid recurrence deadline"
                }
            }
        }
    }

    var taskTag by mutableStateOf("")
        private set
    var taskTagError by mutableStateOf("")
        private set
    fun updateTaskTag(tag: String) {
        taskTag = tag
    }
    private fun checkTag(){
        taskTagError = if(taskTag.isBlank()){
            "Tag cannot be blank"
        } else ""

    }

    fun addComment(userId: String, comment: String) {
        viewModelScope.launch {
            val newComment = Comment(
                author = userId,
                text = comment,
                timestamp = Timestamp.now()
            )
            tasksViewModel.addComment(taskId, newComment)
        }

    }

    fun validate() {
        checkName()
        checkDescription()
        if(taskStatus != TaskStatus.Overdue){
            checkEndDate()
        }
        checkPriority()
        checkStatus()
        checkTag()
        if(taskRecurringType == RecurringType.Recursive){
            checkRepeat()
            checkEndRepeat()
        }
        if(taskNameError == "" &&
            taskDescriptionError == "" &&
            taskEndDateError == "" &&
            taskPriorityError == "" &&
            taskStatusError == "" &&
            taskTagError == "" &&
            taskEndRepeatError == "" &&
            taskRepeatError == "") {
            viewModelScope.launch {
                if(model.updateTask(
                    taskId,
                    task.value.copy(
                        taskName = taskName,
                        description = taskDescription,
                        endDate = taskEndDate.toTimestamp(),
                        creationDate = taskCreationDate.toTimestamp(),
                        priority = taskPriority,
                        status = taskStatus,
                        listUser = listUser,
                        recurringType = taskRecurringType,
                        repeat = if(taskRecurringType == RecurringType.Recursive) taskRepeat else null,
                        endRepeat = if(taskRecurringType == RecurringType.Recursive) taskEndRepeat.toTimestamp() else "".toTimestamp(),
                        tag = taskTag
                    )
                )){
                    uploadStatus = Any()
                    isEditing = false
                    startCollectingTask()
                }else{
                    uploadStatus = UploadStatus.Error("An error occurred. Please try again.")
                    isEditing = true
                    stopCollectingTask()
                }
            }
        }else {
            uploadStatus = Any()
            isEditing = true
            stopCollectingTask()
        }
    }


    var isConfirmDialogShow by mutableStateOf(false)
        private set

    fun toggleConfirmDialog()
    {
        isConfirmDialogShow= !isConfirmDialogShow
    }


    fun checkAll()
    {
        checkName()
        checkDescription()
        if(taskStatus != TaskStatus.Overdue){
            checkEndDate()
        }
        checkPriority()
        checkStatus()
        checkTag()
        if(taskRecurringType == RecurringType.Recursive){
            checkRepeat()
            checkEndRepeat()
        }
        if( taskNameError == "" &&
            taskDescriptionError == "" &&
            taskEndDateError == "" &&
            taskPriorityError == "" &&
            taskStatusError == "" &&
            taskTagError == "" &&
            taskEndRepeatError == "" &&
            taskRepeatError == "")
        {
            isConfirmDialogShow= true
        }
    }


}