package com.teamon.app.utils.viewmodels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.teamon.app.Model
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.asDate
import com.teamon.app.utils.graphics.toTimestamp
import kotlinx.coroutines.launch

class NewTeamViewModel(val model: Model, userId: String) : ViewModel()
{

    var selectedCollaborators by mutableStateOf<Map<User, Boolean>>(emptyMap())
        private set

    private val myID = userId


    init {
        viewModelScope.launch {
            model.getUsers().collect{ user ->
                selectedCollaborators=  user.toSortedMap(compareBy { it }).
                map{if(it.value.userId != myID) Pair(it.value,false) else Pair(it.value,true) }
                    .toMutableStateMap()
            }
        }
    }


   /*
    init {
        selectedCollaborators = users.toSortedMap(compareBy { it })
            .map{if(it.value.userId != myID) Pair(it.value,false) else Pair(it.value,true) }
            .toMutableStateMap()
    }*/


    fun modifyCollaborators(user: User, selected: Boolean)
    {
        val updatedMap = selectedCollaborators.toMutableMap()
        updatedMap[user] = selected
        selectedCollaborators = updatedMap
    }

    //PROPERTIES
    var teamName by mutableStateOf("")
        private set

    var teamDescription by mutableStateOf("")
        private set

    var teamCreationDate: String = Timestamp.now().asDate()


    var teamCategory by mutableStateOf("")
        private set

    private var teamImage: String by mutableStateOf("https://upload.wikimedia.org/wikipedia/commons/4/49/A_black_image.jpg")
    var teamImageSource by mutableStateOf(ImageSource.MONOGRAM)
        private set

    var teamColor = ProjectColors.PURPLE
        private set

   fun setTeamImage(source: ImageSource, uri: Uri = Uri.EMPTY, color: ProjectColors = ProjectColors.PURPLE) {
        teamImageSource = source
        teamImage = uri.toString()
        teamColor = color
    }



    private fun createNewTeam()
    {

        val listUsersId= selectedCollaborators.filter { it.value }.map { it.key.userId }

        val team= Team(
            teamId= "-1",
            name= teamName,
            description= teamDescription,
            image= teamImage,
            imageSource= teamImageSource,
            admin= listOf(myID),
            users = listUsersId.toList(),
            category = teamCategory,
            creationDate= teamCreationDate.toTimestamp(),
            feedback = emptyList(),
            performance = emptyList(),
            color = teamColor,
        )

        model.addTeam(team)
    }


    var isShowing by mutableStateOf(false)
        private set

    var isMembersDialog by mutableStateOf(false)
        private set
    fun openMembersDialog()
    {
        isMembersDialog= !isMembersDialog
    }

    fun toggleShow()
    {
        isShowing = !isShowing
    }

    var teamNameError by mutableStateOf("")
        private set
    fun editName(n: String){teamName= n}

    var teamDescriptionError by mutableStateOf("")
        private set
    fun editDescription(d: String){teamDescription= d}


    var teamCategoryError by mutableStateOf("")
        private set
    fun editCategory(c: String){teamCategory= c}

    private var teamListUsersError by mutableStateOf("")

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


    fun reset()
    {
        teamName=""
        teamDescription=""
        teamCategory=""
        selectedCollaborators= selectedCollaborators.mapValues { it.key.userId == myID }
        teamNameError=""
        teamDescriptionError=""
        teamCategoryError=""
        teamListUsersError=""
        teamImageSource= ImageSource.REMOTE
        teamImage = "https://www.collierbroderick.ie/wp-content/uploads/2017/06/team-effectiveness.png"
    }


    fun validate()
    {
        checkTeamName()
        checkTeamDescription()
        checkTeamCategory()
        if( (teamNameError=="")
            && (teamDescriptionError=="")
           &&(teamCategoryError=="")
            &&(teamListUsersError==""))
        {
            createNewTeam()
            isShowing= false
            reset()
        }
    }

}