package com.teamon.app.utils.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamon.app.Model
import com.teamon.app.chatsViewModel
import com.teamon.app.feedbacksViewModel
import com.teamon.app.profileViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Feedback
import com.teamon.app.utils.classes.Message
import com.teamon.app.utils.classes.Team
import com.teamon.app.utils.classes.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(val model: Model, val userId: String, val teamId: String): ViewModel() {

    var messages: MutableList<Message> = mutableStateListOf()

    init {
        startCollectingMessages(userId, teamId)
    }

    private var updatingMessages : Job? = null

    val team = model.getTeam(teamId)

    val addressee = model.getUser(userId)

    private fun startCollectingMessages(userId: String, teamId: String) {
        updatingMessages = viewModelScope.launch {
            chatsViewModel!!.getChatMessages(userId, teamId).collect {
                messages.clear()
                messages.addAll(it)
            }
        }
    }

    fun sendMessage(message: String) {

        this.model.addMessage(
            addresseeId = userId,
            teamId = teamId,
            authorId = profileViewModel!!.userId,
            text = message
        )
    }

    fun deleteChat() {
        this.model.deleteChat(userId, teamId)
    }

}