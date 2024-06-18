package com.teamon.app.utils.viewmodels

import android.util.Log
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamon.app.Model
import com.teamon.app.profileViewModel
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.classes.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class TeamsViewModel(val model: Model) : ViewModel() {
    lateinit var teams: StateFlow<Map<String, Team>>

    init {
        viewModelScope.launch {
            teams = model.getTeams().stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
        }
    }

    fun getTeams() = model.getTeams()

    fun getTeam(teamId: String) = model.getTeam(teamId)



    fun getTeamMembers(teamId: String): Flow<Map<String, User>> = channelFlow {
        val users = mutableMapOf<String, User>()
        model.getTeam(teamId).collect { team ->
            users.clear()
            team.users.forEach { memberId ->
                launch {
                    model.getUser(memberId).collect { user ->
                        users[user.userId] = user
                        send(users)
                    }
                }
            }
        }
        awaitClose { /* Close resources if needed */ }
    }

    suspend fun addTeamMember(userId: String, teamId: String) = model.addTeamMember(userId, teamId)

    fun uploadTeamImage(teamId: String, file: File) = model.uploadImage(id = teamId, file = file)

    suspend fun updateTeam(teamId: String, team: Team) = model.updateTeam(teamId, team)
}