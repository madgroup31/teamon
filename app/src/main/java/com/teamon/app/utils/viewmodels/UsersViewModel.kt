package com.teamon.app.utils.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamon.app.Model
import com.teamon.app.utils.classes.Project
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.classes.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class UsersViewModel(val model: Model) : ViewModel() {
    lateinit var users: StateFlow<Map<String, User>>

    init {
        viewModelScope.launch {
            users = model.getUsers().stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
        }
    }

    suspend fun exists(userId: String) = model.exists(userId)

    fun getUsers() = model.getUsers()
    fun getUser(userId: String) = model.getUser(userId = userId)
    fun getUserTeams(userId: String) = model.getUserTeams(userId)

    fun getUserProjects(userID: String): Flow<Map<String, Project>> = channelFlow {
        val uProjects = mutableMapOf<String, Project>()

        model.getUserTeams(userID).collect { teams ->
            teams.values.forEach { team ->
                launch {
                    model.getProjectsByTeamId(team.teamId).collect { projects ->
                        uProjects.putAll(projects.map { it.projectId to it }.toMap())
                        send(uProjects)
                    }
                }
            }
        }
        awaitClose { /* Close resources if needed */ }
    }

    suspend fun addUser(user: User) = model.addUser(user)

    suspend fun updateUser(userId: String, user: User) =
        model.updateUser(userId = userId, user = user)

    fun uploadProfileImage(userId: String, file: File) = model.uploadImage(id = userId, file = file)

}