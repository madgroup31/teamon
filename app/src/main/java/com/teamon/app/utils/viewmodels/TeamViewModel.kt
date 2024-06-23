package com.teamon.app.utils.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.teamon.app.Model
import com.teamon.app.chatsViewModel
import com.teamon.app.profileViewModel
import com.teamon.app.projectsViewModel
import com.teamon.app.tasks.TaskStatus
import com.teamon.app.teamsViewModel
import com.teamon.app.utils.classes.Feedback
import com.teamon.app.utils.classes.Message
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.UploadStatus
import com.teamon.app.utils.graphics.asDate
import com.teamon.app.utils.graphics.currentTimeSeconds
import com.teamon.app.utils.graphics.saveBitmapAsJpeg
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

class TeamViewModel(val model: Model, val teamId: String) : ViewModel()
{

    var admin=  mutableStateListOf<String>()       //list of Id string
    var feedbacks= mutableStateListOf<String>()    //list of Id string
    var users= mutableStateListOf<String>()         //list of Id string
    private var performances= mutableStateListOf<String>()  //list of Id string

    var messages: MutableList<Message> = mutableStateListOf()

    init {
        startCollectingTeam()
        startCollectingMessages(teamId)
    }

    private var updatingMessages : Job? = null
    private var updatingTeam: Job? = null

    private fun startCollectingTeam() {
        updatingTeam = viewModelScope.launch {
            model.getTeam(teamId).collect {
                admin.clear()
                admin.addAll(it.admin)

                teamName= it.name
                teamDescription= it.description
                teamCategory= it.category
                teamImage= it.image.toUri()
                teamImageSource= it.imageSource
                teamCreationDate= it.creationDate.asDate() //as Date
                teamCreationDateTimestamp= it.creationDate
                teamColor= it.color

                feedbacks.clear()
                feedbacks.addAll(it.feedback)

                users.clear()
                users.addAll(it.users)

                performances.clear()
                performances.addAll(it.performance)

            }
        }
    }

    private fun stopCollectingTeam() {
        updatingTeam?.cancel()
        updatingTeam = null
    }

    private fun startCollectingMessages(teamId: String) {
        updatingMessages = viewModelScope.launch {
            chatsViewModel.getTeamChatMessages(teamId).collect { mess ->
                messages.clear()
                messages.addAll(mess)
                mess.filter { it.unread.contains(profileViewModel.userId) }
                    .forEach { message ->
                        model.setMessageRead(message.messageId)
                    }
            }
        }
    }

    private fun stopCollectingMessages() {
        updatingMessages?.cancel()
        updatingMessages = null
    }

    fun getMembersTeam()= model.getMembersByTeamId(teamId)

    fun getFeedbacksTeam()= model.getFeedbacksByTeamId(teamId)
    fun getUsers() = model.getMembersByTeamId(teamId)

    fun getProjectsTeam()= model.getProjectsByTeamId(teamId)



    fun getTeamCompletedProjects(): Flow<Map<String, Project>> = channelFlow {
        val completedProjects = mutableMapOf<String, Project>()
        getProjectsTeam().collect {projects ->
            projects.forEach { project ->
                launch {
                    projectsViewModel.getProjectTasks(project.projectId).collect { tasks ->
                        if(tasks.values.all { task -> task.status == TaskStatus.Completed })
                            completedProjects[project.projectId] = project
                            send(completedProjects)
                    }
                }
            }
        }
        awaitClose {  }
    }

    fun deleteTeam() {
        model.deleteTeam(teamId)
    }




    private var teamCreationDateTimestamp by mutableStateOf(Timestamp.now())



    var teamNameError by mutableStateOf("")
        private set
    var teamDescriptionError by mutableStateOf("")
        private set
    var teamCategoryError by mutableStateOf("")
        private set

    var isEditing by mutableStateOf(false)
        private set

    var isFeedbackAnonymous by mutableStateOf(false)
        private set

    var newFeedback by mutableStateOf("")
        private set

    var newFeedbackError by mutableStateOf("")
        private set


    var newFeedbackRating by mutableIntStateOf(5)
        private set

    var isConfirmDialogShow by mutableStateOf(false)
        private set


    var teamName by mutableStateOf("")
        private set
    var teamDescription by mutableStateOf("")
        private set
    var teamCreationDate by mutableStateOf("")
    var teamCategory by mutableStateOf("")
        private set


    var teamColor by mutableStateOf(ProjectColors.ORANGE)
        private set

    var teamImage: Uri by mutableStateOf(Uri.EMPTY)
    var teamImageSource by mutableStateOf(ImageSource.REMOTE)
        private set

    fun updateName(n:String){teamName= n}
    fun updateDescription(d:String){teamDescription= d}
    fun updateCategory(c:String){teamCategory= c}


    var isWritingFeedback by mutableStateOf(false)
        private set
    fun toggleIsWritingFeedback() {
        isWritingFeedback = !isWritingFeedback
    }


    private fun checkNewFeedback() {
        newFeedbackError = if (newFeedback.isBlank()) {
            "Feedback cannot be blank"
        } else ""
    }



    fun updateNewFeedback(feedback: String) {
        newFeedback = feedback
    }


    fun updateNewFeedbackRating(rating: Float) {
        newFeedbackRating = (rating*10).toInt()
    }



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

            model.addFeedbackByTeamId(feedbackToAdd,teamId)
        }


    }

    var uploadStatus: UploadStatus? by mutableStateOf(null)
        private set

    fun setTeamImage(source: ImageSource, uri: Uri = Uri.EMPTY, context: Context, color: ProjectColors) {
        teamImageSource = source
        teamColor = color

        when (source) {
            ImageSource.CAMERA, ImageSource.LIBRARY -> {
                viewModelScope.launch {
                    context.let { stream ->
                        val bitmap: Bitmap?
                        val inputStream = stream.contentResolver.openInputStream(uri)
                        bitmap = BitmapFactory.decodeStream(inputStream)

                        val file = saveBitmapAsJpeg(context, bitmap, "$teamId.jpg")
                        file?.let { jpegFile ->

                            teamsViewModel.uploadTeamImage(teamId = teamId, file = jpegFile).collect {
                                uploadStatus = it
                                if(uploadStatus is UploadStatus.Success) {
                                    teamImage = (it as UploadStatus.Success).downloadUrl.toUri()
                                    teamImageSource = ImageSource.REMOTE
                                }
                            }
                        }

                    }

                }
            }
            else -> {
                /*TODO: delete any old profile picture*/
            }
        }
    }



    fun sendMessage(message: String) {

        this.model.addTeamMessage(
            teamId = teamId,
            users = users,
            text = message
        )

    }





     ///EDIT PART
    fun edit() { isEditing= true; stopCollectingTeam(); error = null }  //TODO: Change to ToggleEdit
    fun clear() { isEditing= false; startCollectingTeam(); error = null }

    fun toggleEdit() {
        isEditing = !isEditing
        if(isEditing) {
            stopCollectingTeam()
        }
        else {
            startCollectingTeam()
        }
    }

    private fun checkTeamName(){
        teamNameError = if (teamName.isBlank()) {
            "Team name cannot be blank"
        } else ""
    }
    private fun checkTeamDescription(){
        teamDescriptionError = if (teamDescription.isBlank()) {
            "Description cannot be blank"
        } else ""
    }

    private fun checkTeamCategory(){
        teamCategoryError = if (teamCategory.isBlank()) {
            "Category cannot be blank"
        } else ""
    }


    fun removeMember(memberIdToRemove: String)
    {
        viewModelScope.launch {
            stopCollectingTeam()
            stopCollectingMessages()
            if(model.removeMemberFromATeam(teamId, memberIdToRemove))
                if(memberIdToRemove != profileViewModel.userId) {
                    startCollectingTeam()
                    startCollectingMessages(teamId)
                }
            else
                error = "An error occurred. Please try again."
        }

    }

    fun promoteToAdmin(memberIdToPromote: String)
    {

        val adminsNew=  admin.toMutableList()
        adminsNew.add(memberIdToPromote)
        admin.clear()
        admin.addAll(adminsNew)

        model.promoteMemberToAdminByTeamId(teamId,admin)


    }

    fun setErrorString(e: String?) {
        error = e
    }

    fun toggleConfirmDialog()
    {
        isConfirmDialogShow= !isConfirmDialogShow
    }
    
    fun checkAll()
    {
        checkTeamName()
        checkTeamDescription()
        checkTeamCategory()
        if( (teamNameError=="")
            && (teamNameError=="")
            &&(teamCategoryError=="") )
        {
            isConfirmDialogShow= true
        }
    }

    fun validate()
    {
        checkTeamName()
        checkTeamDescription()
        checkTeamCategory()
        if( (teamNameError=="")
            && (teamDescriptionError=="")
            &&(teamCategoryError=="") )
        {
            viewModelScope.launch {
                val team = Team(
                    teamId = teamId,
                    name = teamName,
                    description = teamDescription,
                    category = teamCategory,
                    image = teamImage.toString(),
                    imageSource = teamImageSource,
                    creationDate = teamCreationDateTimestamp,
                    color = teamColor,
                    admin = admin,
                    feedback = feedbacks,
                    users = users,
                )
                if(!teamsViewModel.updateTeam(teamId, team)) {
                    error = "An error occurred while updating team. Please try again"
                }
                else {
                    error = null
                    isEditing = false
                    startCollectingTeam()
                }
            }
        }

    }

    var error by mutableStateOf<String?>(null)
    private set

    fun checkOthersAdmins(userIdToRemove: String): Boolean
    {

        val localAdmins= admin
        return localAdmins.toList().any { it != userIdToRemove }
    }



}