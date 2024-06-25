package com.teamon.app.utils.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.teamon.app.Model
import com.teamon.app.profileViewModel
import com.teamon.app.projectsViewModel
import com.teamon.app.utils.classes.Feedback
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

class ProjectViewModel(val model: Model, val projectId: String) : ViewModel() {
    var project: StateFlow<Project> = model.getProject(projectId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Project())

    var tasks = mutableStateListOf<Task>()
        private set

    var feedbacks = mutableStateListOf<Feedback>()
        private set

    var teams = mutableStateListOf<Team>()
    private set

    fun getProjectTeams() = projectsViewModel.getProjectTeams(projectId)

    var members = mutableStateMapOf<String, User>()
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

    var newFeedbackRating by mutableIntStateOf(5)
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
                authorId = profileViewModel.userId,
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
            "Project description cannot be blank"
        } else ""
    }

    var projectEndDate by mutableStateOf("")
        private set



    var isConfirmDialogShow by mutableStateOf(false)
        private set

    fun toggleConfirmDialog()
    {
        isConfirmDialogShow= !isConfirmDialogShow
    }

    suspend fun updateProject(): Boolean {
        validate()
        Log.d("update", "$projectNameError, $projectDescriptionError")
        if(projectNameError.isBlank() && projectDescriptionError.isBlank()) {
            val project = Project(
                projectId = projectId,
                projectName = projectName,
                projectColor = projectColor,
                description = projectDescription,
                endDate = projectEndDate.toTimestamp(),
                teams = teams.map { it.teamId },
                tasks = tasks.map { it.taskId },
                feedbacks = feedbacks.map { it.feedbackId }
            )
            toggleConfirmDialog()

            if(projectsViewModel.updateProject(projectId, project)) {
                toggleEdit()
                return true
            }
            else
                return false
        }
        else {
            Log.d("update", "here")
            return false
        }

    }

    fun checkAll()
    {
        checkName()
        checkDescription()
        if (
            projectNameError == "" &&
            projectDescriptionError == ""
        ) {
            isConfirmDialogShow= true
        }
    }

    fun validate() {
        checkName()
        checkDescription()
    }

}