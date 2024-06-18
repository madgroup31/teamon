package com.teamon.app.utils.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamon.app.Model
import com.teamon.app.utils.classes.History
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class HistoryViewModel(val model: Model): ViewModel() {

    fun getTaskHistory(taskId: String): Flow<Map<String, History>> = channelFlow {
        val history = mutableMapOf<String, History>()
        model.getTask(taskId).collect {task ->
            history.clear()
            task.history.forEach {
                launch {
                    model.getHistory(it).collect {
                        history[it.historyId] = it
                        send(history)
                }
                }
            }
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)
}