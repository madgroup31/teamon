package com.teamon.app.utils.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamon.app.Model
import com.teamon.app.utils.classes.Team
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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


    suspend fun addTeamMember(userId: String, teamId: String) = model.addTeamMember(userId, teamId)

    fun uploadTeamImage(teamId: String, file: File) = model.uploadImage(id = teamId, file = file)

    suspend fun updateTeam(teamId: String, team: Team) = model.updateTeam(teamId, team)
}