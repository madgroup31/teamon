package com.teamon.app.utils.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamon.app.Model
import com.teamon.app.utils.classes.Feedback
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FeedbacksViewModel(val model: Model) : ViewModel() {
    lateinit var feedbacks: StateFlow<Map<String, Feedback>>

    init {
        viewModelScope.launch {
            feedbacks = model.getFeedbacks().stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
        }
    }


    fun getUserFeedbacks(userId: String): Flow<Map<String, Feedback>> = channelFlow {
        val feedbacks = mutableMapOf<String, Feedback>()

        model.getUser(userId).collect { user ->
            feedbacks.clear()
            user.feedbacks.forEach { feedbackId ->
                launch {
                    model.getFeedback(feedbackId).collect { feedback ->
                        feedbacks[feedback.feedbackId] = feedback
                        send(feedbacks)
                    }
                }
            }
        }

        awaitClose { /* Close resources if needed */ }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

}
