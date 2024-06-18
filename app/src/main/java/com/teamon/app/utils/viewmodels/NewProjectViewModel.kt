package com.teamon.app.utils.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.ProjectImages

class NewProjectViewModel : ViewModel() {

    /*
val projectId: Int, #Should be added by model
    val projectName: String,
    val projectImage: ImageVector,      TODO: Custom images
    val projectColor: ProjectColors,    TODO: Custom colors
    val description: String,
val favorite: Boolean,
    val endDate: String,
val progress: Float,
val mainParticipantAvatar: Int,
val chatBadge: Boolean,
var tasks: List<Task> = listOf(),
val feedbacks: List<Feedback> = listOf(),   TODO: Add to Notions
val participants: Int,
    val members: List<User> = listOf()
    */
    var isShowing by mutableStateOf(false)
        private set

    fun toggleShow() {
        isShowing = !isShowing
    }
    var isAssigningProject by mutableStateOf(false)
        private set

    fun toggleAssignProject() {
        isAssigningProject = !isAssigningProject
    }

    var isPickingColor by mutableStateOf(false)
        private set

    fun togglePickColor() {
        isPickingColor = !isPickingColor
    }

    var isPickingImage by mutableStateOf(false)
        private set

    fun togglePickImage() {
        isPickingImage = !isPickingImage
    }

    var listUser = mutableStateListOf<String>()

    fun updateListUser(users: List<String>) {
        listUser = users.toMutableStateList()
    }

    var listTeams = mutableStateListOf<String>()
    fun updateTeams(teams: List<String>) {
        listTeams = teams.toMutableStateList()
    }

    fun updateTeam(teamId: String, selected: Boolean) {
        if (selected) {
            listTeams.add(teamId)
        } else {
            listTeams.remove(teamId)
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

    var projectColor by mutableStateOf(ProjectColors.BLUE)
        private set

    var projectColorError by mutableStateOf("")
        private set

    fun updateProjectColor(color: ProjectColors) {
        projectColor = color
    }

    private fun checkProjectColor() {
        //Should be OK by default
    }

    var projectImage by mutableStateOf(ProjectImages.BUILD)
        private set

    var projectImageError by mutableStateOf("")
        private set

    fun updateProjectImage(image: ProjectImages) {
        projectImage = image
    }

    private fun checkProjectImage() {
        //Should be OK by default
    }


    private fun validate(): Boolean {
        checkName()
        checkDescription()
        return (projectNameError == "" &&
                projectDescriptionError == ""
                )
    }

    fun canCreateNewProject(): Boolean {
        return validate()
    }

    fun reset() {
        projectName = ""
        projectDescription = ""
        projectColor = ProjectColors.BLUE
        projectImage = ProjectImages.BUILD
        listUser.clear()
        listTeams.clear()
    }

}