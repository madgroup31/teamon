package com.teamon.app.utils.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.teamon.app.Model
import com.teamon.app.board.project.feedbacks.FeedbackType
import com.teamon.app.feedbacksViewModel
import com.teamon.app.profileViewModel
import com.teamon.app.tasksViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Feedback
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.utils.graphics.ProjectColors
import com.teamon.app.utils.graphics.asDate
import com.teamon.app.utils.graphics.currentTimeSeconds
import kotlinx.coroutines.launch
import java.util.UUID

class UserViewModel(val model: Model, userId: String) : ViewModel() {

    var feedbacks = mutableStateListOf<Feedback>()
        private set

    var tasks = mutableStateListOf<Task>()

    init {
        viewModelScope.launch {
            usersViewModel!!.getUser(userId).collect {

                nameValue = it.name
                surnameValue = it.surname
                nicknameValue = it.nickname
                emailValue = it.email
                locationValue = it.location
                birthdateValue = it.birthdate.asDate()
                bioValue = it.biography
                color = it.color
                it.profileImage?.let { profileImage = it }
                lastUpdate = it.lastUpdate
                profileImageSource = it.profileImageSource
            }
        }

        viewModelScope.launch {
            feedbacksViewModel!!.getUserFeedbacks(userId).collect {
                feedbacks.clear()
                feedbacks.addAll(it.values)
            }
        }

        viewModelScope.launch {
            tasksViewModel!!.getUserTasks().collect {
                tasks.clear()
                tasks.addAll(it.values)
            }
        }


    }

    var isWritingFeedback by mutableStateOf(false)
        private set
    fun toggleIsWritingFeedback() {
        isWritingFeedback = !isWritingFeedback
    }

    var newFeedback by mutableStateOf("")
        private set

    fun updateNewFeedback(feedback: String) {
        newFeedback = feedback
    }

    var newFeedbackError by mutableStateOf("")
        private set

    private fun checkNewFeedback() {
        newFeedbackError = if (newFeedback.isBlank()) {
            "Feedback cannot be blank"
        } else ""
    }

    var color by mutableStateOf(ProjectColors.PURPLE)
    private set

    var newFeedbackRating by mutableStateOf(5)
        private set

    fun updateNewFeedbackRating(rating: Float) {
        newFeedbackRating = (rating*10).toInt()
    }

    var isFeedbackAnonymous by mutableStateOf(false)
        private set

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
                authorId = profileViewModel!!.userId,
                description = newFeedback,
                value = newFeedbackRating,
                anonymous = isFeedbackAnonymous,
                timestamp = Timestamp(currentTimeSeconds(),0)
            )

            model.addFeedbackToUser(feedbackToAdd,userId)

            newFeedback = ""
            newFeedbackRating = 5
            isWritingFeedback = false
        }
    }

    var error by mutableStateOf(false)
    private set

    var userId = userId
        private set

    var nameValue by mutableStateOf("")
        private set

    var surnameValue by mutableStateOf("")
        private set

    var nicknameValue by mutableStateOf("")
        private set

    var emailValue by mutableStateOf("")
        private set

    var locationValue by mutableStateOf("")
        private set

    var birthdateValue by mutableStateOf("")
        private set
    var bioValue by mutableStateOf("")
        private set

    var profileImage by mutableStateOf("")
        private set

    var profileImageSource by mutableStateOf(ImageSource.MONOGRAM)
        private set

    var lastUpdate by mutableStateOf(Timestamp.now())
        private set
}