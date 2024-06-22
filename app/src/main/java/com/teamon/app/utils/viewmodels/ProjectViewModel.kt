package com.teamon.app.utils.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.teamon.app.Model
import com.teamon.app.board.project.feedbacks.FeedbackType
import com.teamon.app.profileViewModel
import com.teamon.app.projectsViewModel
import com.teamon.app.utils.classes.Feedback
import com.teamon.app.utils.classes.Performance
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.asDate
import com.teamon.app.utils.graphics.currentTimeSeconds
import com.teamon.app.utils.graphics.toTimestamp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class ProjectViewModel(val model: Model, val projectId: String) : ViewModel() {
    var project: StateFlow<Project> = model.getProject(projectId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Project())

    /*var tasks = mutableStateListOf<String>() //lista id dei task
    var teams= model.loadTeamsByProjectId(projectId)
    var teamUsers= model.loadTeamUsers(projectId) //map: key= teamId, value= pair(teamName,listUsers)
    var feedbacks= model.loadFeedbacksByProjectId(projectId)
    var performances= model.loadPerformancesByProjectId(projectId)*/

    var tasks = mutableStateListOf<Task>()
        private set

    var feedbacks = mutableStateListOf<Feedback>()
        private set

    var teams = mutableStateListOf<Team>()
    private set

    fun getProjectTeams() = projectsViewModel.getProjectTeams(projectId)

    var members = mutableStateMapOf<String, User>()
    private set

    var performances = mutableStateListOf<Performance>()
    private set


    init {
        viewModelScope.launch {
            model.getProject(projectId).collect {
                projectName = it.projectName
                projectColor = it.projectColor
                projectDescription = it.description
                projectEndDate = it.endDate.asDate()
            }
        }
        viewModelScope.launch {
            projectsViewModel.getProjectTasks(projectId).collect {
                tasks.clear()
                tasks.addAll(it.values.toMutableStateList())
            }
        }

        viewModelScope.launch {
            projectsViewModel.getProjectTeams(projectId).collect {
                teams.clear()
                teams.addAll(it.values.toMutableStateList())
            }
        }

        viewModelScope.launch {
            projectsViewModel.getProjectMembers(projectId).collect {
                members.clear()
                members.putAll(it)
            }
        }
        viewModelScope.launch {
            projectsViewModel.getProjectFeedbacks(projectId).collect {
                feedbacks.clear()
                feedbacks.addAll(it.values.toMutableStateList())
            }
        }
    }

    suspend fun addTask(task: Task) {
        this.model.addTask(task = task, projectId = projectId)

    }


    var projectColor by mutableStateOf(ProjectColors.PURPLE)
        private set

    fun canEditProject() : Boolean {
        return teams.filter { it.users.contains(profileViewModel.userId) }
            .any { it.admin.contains(profileViewModel.userId) }
    }

    var isEditing by mutableStateOf(false)
        private set

    fun toggleEdit() {
        isEditing = !isEditing
    }

    var isEditingTeams by mutableStateOf(false)
        private set

    fun toggleEditTeams() {
        isEditingTeams = !isEditingTeams
    }

    suspend fun removeTeam(teamId: String) = model.removeTeamFromProject(projectId, teamId)

    suspend fun addTeam(teamId: String) = model.addTeamToProject(projectId, teamId)

    var isEditingEndDate by mutableStateOf(false)
        private set

    fun toggleEditEndDate() {
        isEditingEndDate = !isEditingEndDate
    }




    fun updateListUser(user: User) {
        /*if (members.contains(user)) {
            members = members.filter { it != user }
        } else {
            members = members + user
        }*/
    }

    var isWritingFeedback by mutableStateOf(false)
        private set
    fun toggleIsWritingFeedback() {
        isWritingFeedback = !isWritingFeedback
    }

    var newFeedback by mutableStateOf("")
        private set

    fun updateNewFeedback(feedback: String) {
        newFeedback = feedback
    }

    var newFeedbackError by mutableStateOf("")
        private set

    private fun checkNewFeedback() {
        newFeedbackError = if (newFeedback.isBlank()) {
            "Feedback cannot be blank"
        } else ""
    }

    var newFeedbackRating by mutableStateOf(5)
        private set

    fun updateNewFeedbackRating(rating: Float) {
        newFeedbackRating = (rating*10).toInt()
    }

    var isFeedbackAnonymous by mutableStateOf(false)
        private set

    fun toggleIsFeedbackAnonymous() {
        isFeedbackAnonymous = !isFeedbackAnonymous
    }

    fun resetFeedback() {
        newFeedback = ""
        newFeedbackRating = 5
    }

    fun addFeedback() {
        checkNewFeedback()
        if (newFeedbackError == "")
        {

            val feedbackToAdd= Feedback(
                feedbackId = "-1",
                authorId = profileViewModel!!.userId,
                description = newFeedback,
                value = newFeedbackRating,
                anonymous = isFeedbackAnonymous,
                timestamp = Timestamp(currentTimeSeconds(),0)
            )

            newFeedback = ""
            newFeedbackRating = 5
            isWritingFeedback = false

            model.addFeedbackByProjectId(feedbackToAdd,projectId)

        }
    }

    var projectName by mutableStateOf("")
        private set
    var projectNameError by mutableStateOf("")
        private set

    fun updateProjectName(name: String) {
        projectName = name
    }

    private fun checkName() {
        projectNameError = if (projectName.isBlank()) {
            "Project name cannot be blank"
        } else ""
    }

    var projectDescription by mutableStateOf("")
        private set
    var projectDescriptionError by mutableStateOf("")
        private set

    fun updateProjectDescription(description: String) {
        projectDescription = description
    }

    private fun checkDescription() {
        projectDescriptionError = if (projectDescription.isBlank()) {
            "Project name cannot be blank"
        } else ""
    }

    var projectEndDate by mutableStateOf("")
        private set
    var projectEndDateError by mutableStateOf("")
        private set

    fun updateProjectEndDate(endDate: String) {
        projectEndDate = endDate
    }


    private fun checkEndDate() {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        return try {
            if (projectEndDate.isBlank())
                throw DateTimeException("Project should have a deadline")

            val dateOfProjectEndDate = dateFormat.parse(projectEndDate)

            //val latestTaskDate = project.tasks.maxByOrNull { LocalDate.parse(it.endDate, DateTimeFormatter.ofPattern("dd-MM-yyyy")) }?.endDate
            //val dateOfLatestTask = latestTaskDate?.let { dateFormat.parse(it) }

            //if (dateOfLatestTask != null && dateOfProjectEndDate!!.before(dateOfLatestTask))
            //    throw DateTimeException("Project deadline should be after or equal to the latest task deadline")

            val calendar = Calendar.getInstance()
            if (dateOfProjectEndDate!!.before(calendar.time))
                throw DateTimeException("Project deadline should be set to the future")
            projectEndDateError = ""
        } catch (e: Exception) {
            when (e) {
                is DateTimeException -> {
                    projectEndDateError = e.message.toString()
                }

                else -> {
                    projectEndDateError = "Invalid project deadline"
                }
            }
        }
    }



    /* FOR CHAT, FEEDBACKS
    fun addComment(user: User, comment: String) {
        this.model.addComment(taskId, user, comment)
        this.comments = model.loadTask(taskId).comments.toMutableStateList()
    }
     */

    //REMOVE A COMPONENT
    //ADD A COMPONENT


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
        checkEndDate()
        if (
            projectNameError == "" &&
            projectDescriptionError == "" &&
            projectEndDateError == ""
        ) {
            isConfirmDialogShow= true
        }
    }

    fun validate() {
        checkName()
        checkDescription()
        checkEndDate()
        //checkTag() SHOULD NOT BE MODIFIABLE
        if (
            projectNameError == "" &&
            projectDescriptionError == "" &&
            projectEndDateError == ""
        ) {
            /*model.updateProject(
                project.copy(
                    projectName = projectName,
                    description = projectDescription,
                    endDate = projectEndDate.toTimestamp(),
                    //members = members,
                )
            )
            isEditing = false
            project = model.loadProject(projectId)*/
        }

    }

    suspend fun deleteTask(taskId: String) = model.deleteTask(projectId,taskId)
}