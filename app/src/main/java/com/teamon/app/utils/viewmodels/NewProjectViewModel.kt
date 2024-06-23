package com.teamon.app.utils.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.ProjectImages

class NewProjectViewModel : ViewModel() {

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

    private var listUser = mutableStateListOf<String>()

    var listTeams = mutableStateListOf<String>()

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

    fun updateProjectColor(color: ProjectColors) {
        projectColor = color
    }

    var projectImage by mutableStateOf(ProjectImages.BUILD)
        private set

    fun updateProjectImage(image: ProjectImages) {
        projectImage = image
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