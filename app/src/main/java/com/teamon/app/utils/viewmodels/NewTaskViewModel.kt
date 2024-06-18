package com.teamon.app.utils.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.toMutableStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.teamon.app.Model
import com.teamon.app.projectsViewModel
import com.teamon.app.tasks.RecurringType
import com.teamon.app.tasks.Repeat
import com.teamon.app.tasks.TaskPriority
import com.teamon.app.tasks.TaskStatus
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.currentTimeSeconds
import com.teamon.app.utils.graphics.toTimestamp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale

class NewTaskViewModel(val model: Model, projectId: String, userId: String) : ViewModel() {
    var selectedCollaborators = mutableStateMapOf<User, Boolean>()
    val myID = userId
    var projectId by mutableStateOf(projectId)
    val users = usersViewModel!!.users.value

    lateinit var projects: StateFlow<Map<String, Project>>
    lateinit var teams: StateFlow<Map<String, Team>>
    var projectMembers = mutableStateMapOf<String, User>()

    init {
        viewModelScope.launch {
            teams = usersViewModel!!.getUserTeams(myID).stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
        }
        viewModelScope.launch {
            projects = usersViewModel!!.getUserProjects(myID).stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
        }
        if (projectId != "") {
            updateProjectMembers(projectId)
        }
    }

    suspend fun addTask(task: Task) = model.addTask(projectId, task)

    fun updateProjectMembers(pId: String) {
        projectId = pId
        viewModelScope.launch {
            projectsViewModel!!.getProjectMembers(projectId).collect {
                projectMembers.clear()
                projectMembers.putAll(it)

                selectedCollaborators.clear()
                projectMembers.values.forEach { user ->
                    if (user.userId != myID)
                        selectedCollaborators[user] = false
                    else selectedCollaborators[user] = true
                }
            }
        }
    }


    fun modifyCollaborators(user: User, selected: Boolean) {
        selectedCollaborators.replace(user, selected)
    }

    var isShowing by mutableStateOf(false)
        private set

    fun toggleShow() {
        isShowing = !isShowing
    }

    var isEditingEndDate by mutableStateOf(false)
        private set

    var projectName by mutableStateOf("")
        private set

    fun updateProjectName(name: String) {
        projectName = name
    }

    fun toggleEditEndDate() {
        isEditingEndDate = !isEditingEndDate
    }

    var isEditingEndRepeat by mutableStateOf(false)
        private set

    fun toggleEditEndRepeat() {
        isEditingEndRepeat = !isEditingEndRepeat
    }

    var lifetimeUnit by mutableStateOf("Days")
    var lifetimeError by mutableStateOf("")
    var lifetimeNumbers by mutableStateOf("1")
    var lifetimeNumbersError by mutableStateOf("")
    fun updateLifetimeUnit(u: String) {
        lifetimeUnit = u
    }

    fun updateLifetimeNumbers(n: String) {
        lifetimeNumbers = n
    }

    fun checkLifetime() {
        lifetimeError =
            if (lifetimeUnit.isBlank() || lifetimeNumbersError.isBlank()) "You should set a task lifetime" else ""
    }

    var isAssigningTask by mutableStateOf(false)
        private set

    fun toggleAssignTask() {
        isAssigningTask = !isAssigningTask
    }

    var listUser = mutableStateListOf<Int>()

    fun updateListUser(users: List<Int>) {
        listUser = users.toMutableStateList()
    }

    var taskName by mutableStateOf("")
        private set

    var taskNameError by mutableStateOf("")
        private set

    fun updateTaskName(name: String) {
        taskName = name
    }

    private fun checkName() {
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

    private fun checkDescription() {
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

    private fun checkEndDate() {

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        return try {
            if (taskEndDate.isBlank())
                throw DateTimeException("Task should have a deadline")
            val dateOfTaskEndDate = dateFormat.parse(taskEndDate)

            if (taskRecurringType == RecurringType.Recursive) {
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
    var taskPriorityError by mutableStateOf("")
        private set

    fun updateTaskPriority(priority: TaskPriority) {
        taskPriority = priority
    }

    var isEditingStatusOptions by mutableStateOf(false)
        private set

    fun toggleEditingStatusOptions() {
        isEditingStatusOptions = !isEditingStatusOptions
    }

    var taskStatus by mutableStateOf(TaskStatus.Pending)
        private set
    var taskStatusError by mutableStateOf("")
        private set

    fun updateTaskStatus(status: TaskStatus) {
        taskStatus = status
    }

    var taskRecurringType by mutableStateOf(RecurringType.Fixed)
        private set
    var taskRecurringTypeError by mutableStateOf("")
        private set

    fun toggleRecurringType(switch: Boolean) {
        if (switch)
            taskRecurringType = RecurringType.Recursive
        else {
            taskRecurringType = RecurringType.Fixed
            taskEndRepeatError = ""
            taskRepeatError = ""
        }
    }

    var isEditingRepeatOptions by mutableStateOf(false)
        private set

    fun toggleEditRepeatOptions() {
        isEditingRepeatOptions = !isEditingRepeatOptions
    }

    var taskRepeat by mutableStateOf(Repeat.Daily)
        private set
    var taskRepeatError by mutableStateOf("")
        private set

    fun updateTaskRepeat(repeat: Repeat) {
        taskRepeat = repeat
    }


    var taskEndRepeat by mutableStateOf("")
        private set
    var taskEndRepeatError by mutableStateOf("")
        private set

    fun updateTaskEndRepeat(endRepeat: String) {
        taskEndRepeat = endRepeat
    }

    private fun checkEndRepeat() {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        return try {
            if (taskEndRepeat.isBlank())
                throw DateTimeException("When do you wanna stop the recurrence?")
            val dateOfEndRepeat = dateFormat.parse(taskEndRepeat)

            val futureDate = Calendar.getInstance()
            when (lifetimeUnit) {
                "Days" -> futureDate.add(Calendar.DAY_OF_MONTH, Integer.parseInt(lifetimeNumbers))
                "Months" -> futureDate.add(Calendar.MONTH, Integer.parseInt(lifetimeNumbers))
                "Years" -> futureDate.add(Calendar.YEAR, Integer.parseInt(lifetimeNumbers))
                else -> throw IllegalArgumentException("Lifetime not supported")
            }
            taskEndDate = dateFormat.format(futureDate.time)

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

    private fun checkTag() {
        taskTagError = if (taskTag.isBlank()) {
            "Tag cannot be blank"
        } else ""

    }

    var taskUploadingError by mutableStateOf(false)
        private set

    fun setUploadingError(error: Boolean) {
        taskUploadingError = error
    }

    private fun validate(): Boolean {
        checkName()
        checkDescription()
        if (taskStatus != TaskStatus.Overdue && taskRecurringType != RecurringType.Recursive) {
            checkEndDate()
        }
        checkTag()
        if (taskRecurringType == RecurringType.Recursive) {
            checkEndRepeat()
        }
        return (taskNameError == "" &&
                taskDescriptionError == "" &&
                taskEndDateError == "" &&
                taskPriorityError == "" &&
                taskStatusError == "" &&
                taskTagError == "" &&
                taskEndRepeatError == "" &&
                taskRepeatError == ""
                )
    }

    fun canCreateNewTask(): Boolean {
        return if (validate()) {
            when (taskRecurringType) {
                RecurringType.Fixed -> {
                    val task = Task(
                        taskName = taskName,
                        projectName = projectName,
                        description = taskDescription,
                        creationDate = Timestamp(currentTimeSeconds(), 0),
                        endDate = taskEndDate.toTimestamp(),
                        status = taskStatus,
                        priority = taskPriority,
                        listUser = selectedCollaborators.filter { it.value }.keys.map { it.userId }
                            .toList(),
                        tag = taskTag,
                        recurringType = taskRecurringType,
                        repeat = taskRepeat,
                        endRepeat = taskEndRepeat.toTimestamp(),
                        attachments = listOf(),
                        history = listOf(),
                        comments = listOf()
                    )
                    viewModelScope.launch {
                        taskUploadingError = model.addTask(projectId, task)
                    }
                    toggleShow()
                    reset()
                    true
                }

                RecurringType.Recursive -> {
                    val tasks = mutableListOf<Task>()
                    val current = Instant.now()
                    val currentDateTime: LocalDateTime = LocalDateTime.ofInstant(current, ZoneId.systemDefault())
                    val duration = taskEndDate.toTimestamp().toInstant().epochSecond - currentDateTime.atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
                    var recurringTaskCreationDate = currentDateTime.atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault())
                    do {
                        tasks.add(Task(
                            taskName = taskName,
                            projectName = projectName,
                            description = taskDescription,
                            creationDate = Timestamp(
                                recurringTaskCreationDate.toInstant().epochSecond, 0
                            ),
                            endDate = Timestamp(
                                recurringTaskCreationDate.toInstant().epochSecond + duration, 0
                            ),
                            status = taskStatus,
                            priority = taskPriority,
                            listUser = selectedCollaborators.filter { it.value }.keys.map { it.userId }
                                .toList(),
                            tag = taskTag,
                            recurringType = taskRecurringType,
                            repeat = taskRepeat,
                            endRepeat = taskEndRepeat.toTimestamp(),
                            attachments = listOf(),
                            history = listOf(),
                            comments = listOf()
                        ))
                        when (taskRepeat) {
                            Repeat.Daily -> {
                                recurringTaskCreationDate = recurringTaskCreationDate.plusDays(1)
                            }

                            Repeat.Weekly -> {
                                recurringTaskCreationDate = recurringTaskCreationDate.plusWeeks(1)
                            }

                            Repeat.Monthly -> {
                                recurringTaskCreationDate = recurringTaskCreationDate.plusMonths(1)
                            }

                            Repeat.Yearly -> {
                                recurringTaskCreationDate = recurringTaskCreationDate.plusYears(1)
                            }
                        }
                    } while (recurringTaskCreationDate < LocalDateTime.ofInstant(taskEndRepeat.toTimestamp().toInstant(), ZoneId.systemDefault()).atZone(ZoneId.systemDefault())
                    )

                    viewModelScope.launch {
                        taskUploadingError = model.addRecursiveTasks(projectId, tasks)
                    }
                    toggleShow()
                    reset()
                    true
                }
            }

        } else false
    }


    fun reset() {
        taskName = ""
        taskDescription = ""
        taskEndDate = ""
        taskPriority = TaskPriority.Medium
        taskStatus = TaskStatus.Pending
        taskRecurringType = RecurringType.Fixed
        taskRepeat = Repeat.Daily
        taskEndRepeat = ""
        taskTag = ""
        resetCollaborators()
        lifetimeUnit = "Days"
        lifetimeNumbers = "1"
    }

    fun resetCollaborators() {
        selectedCollaborators = users.toSortedMap(compareBy { it })
            .map { if (it.value.userId != myID) Pair(it.value, false) else Pair(it.value, true) }
            .toMutableStateMap()
    }
}