package com.teamon.app.utils.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamon.app.Model
import com.teamon.app.profileViewModel
import com.teamon.app.utils.classes.Chat
import com.teamon.app.utils.classes.Message
import com.teamon.app.utils.classes.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class ChatsViewModel(val model: Model) : ViewModel() {

    fun getCorrespondent(chatId: String): Flow<User> = channelFlow {
        model.getChatById(chatId).collect {
            launch {
                val correspondent = it.userIds.firstOrNull { it != profileViewModel.userId }
                if (correspondent != null)
                    model.getUser(correspondent).collect {
                        send(it)
                    }
            }
        }
        awaitClose { /* Close resources if needed */ }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    fun getChats(): Flow<Map<String, Chat>> = channelFlow {
        val chats = mutableMapOf<String, Chat>()
        model.getTeams().collect { teams ->
            chats.clear()
            teams.values.forEach { team ->
                viewModelScope.launch {
                    model.getChats(team.teamId).collect {
                        chats.putAll(it)
                        send(chats)
                    }
                }
            }
        }
        awaitClose { /* Close resources if needed */ }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    fun getChatMessages(userId: String, teamId: String): Flow<List<Message>> = channelFlow {

        model.getChat(userId, teamId).collect { chat ->
            launch {
                model.getChatMessages(chat.chatId).collect { messages ->
                    send(messages)
                    messages.filter { it.unread.contains(profileViewModel.userId) }
                        .forEach { message ->
                            model.setMessageRead(message.messageId)
                        }
                }
            }
        }

        awaitClose { /* Close resources if needed */ }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    fun getTeamChatMessages(teamId: String): Flow<List<Message>> = channelFlow {

        model.getTeamChat(teamId).collect { chat ->
            launch {
                model.getChatMessages(chat.chatId).collect {
                    send(it)
                }
            }
        }

        awaitClose { /* Close resources if needed */ }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    fun getLastChatMessage(chatId: String) = model.getLastChatMessage(chatId)

    fun getUnreadMessagesInChat(chatId: String) = model.getUnreadMessages(chatId)

    fun isMessageRead(messageId: String, userId: String) = model.isMessageRead(messageId, userId)

    fun getUnreadMessages(teamId: String): Flow<Map<String, Int>> = channelFlow<Map<String, Int>> {
        val unreadMessages = mutableMapOf<String, Int>()
        model.getUserChats(teamId).collect { chats ->
            unreadMessages.clear()
            chats.keys.forEach { chatId ->
                launch {
                    model.getUnreadMessages(chatId).collect {
                        unreadMessages[chatId] = it
                        send(unreadMessages)
                    }
                }
            }
        }
        awaitClose { /* Close resources if needed */ }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    fun getUnreadTeamChatMessages(teamId: String): Flow<Int> = channelFlow {
        model.getTeamChat(teamId).collect {
                launch {
                    model.getUnreadMessages(it.chatId).collect {
                        send(it)
                    }
                }
            }
        awaitClose { /* Close resources if needed */ }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)


    fun getLastMessages(teamId: String): Flow<Map<String, Message>> = channelFlow {
            val lastMessages = mutableMapOf<String, Message>()
            model.getUserChats(teamId).collect { chats ->
                lastMessages.clear()
                chats.keys.forEach { chatId ->
                    launch {
                        model.getLastChatMessage(chatId).collect {
                            lastMessages[chatId] = it
                            send(lastMessages)
                        }
                    }
                }
            }
            awaitClose { /* Close resources if needed */ }
        }

    fun deleteMessage(messageId: String) {
        this.model.deleteMessage(messageId)
    }

}