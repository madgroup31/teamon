package com.teamon.app.utils.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.collection.emptyLongSet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.teamon.app.Model
import com.teamon.app.feedbacksViewModel
import com.teamon.app.login.SignInResult
import com.teamon.app.login.SignInState
import com.teamon.app.tasksViewModel
import com.teamon.app.usersViewModel
import com.teamon.app.utils.classes.Feedback
import com.teamon.app.utils.classes.Task
import com.teamon.app.utils.classes.User
import com.teamon.app.utils.graphics.asDate
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.util.Calendar
import java.util.Locale
import com.teamon.app.utils.graphics.ImageSource
import com.teamon.app.utils.graphics.UploadStatus
import com.teamon.app.utils.graphics.saveBitmapAsJpeg
import com.teamon.app.utils.graphics.toTimestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfileViewModel(val model: Model) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    var userId by mutableStateOf("")
        private set

    var emailValue by mutableStateOf("")
        private set
    var emailError by mutableStateOf("")
        private set

    init {
        val auth = Firebase.auth
        if (auth.currentUser != null) {
            userId = auth.currentUser!!.uid
            startCollectingUser(userId)
            startCollectingFeedbacks(userId)
            startCollectingTasks(userId)
            emailValue = auth.currentUser?.email?: ""
        }
    }

    fun onSignInResult(result: SignInResult) {
        val auth = Firebase.auth
        if (result.data != null) {
            userId = result.data
            emailValue = auth.currentUser?.email?: ""
            startCollectingUser(userId)
            startCollectingFeedbacks(userId)
            startCollectingTasks(userId)
        }
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                signInError = result.errorMessage
            )
        }
    }

    var feedbacks: MutableList<Feedback> = mutableStateListOf()

    var tasks: MutableList<Task> = mutableStateListOf()


    private var updatingUser: Job? = null
    private var updatingFeedbacks: Job? = null
    private var updatingTasks: Job? = null


    private fun startCollectingUser(userId: String) {
        updatingUser = viewModelScope.launch {
            usersViewModel.getUser(userId).collect {
                nameValue = it.name
                surnameValue = it.surname
                nicknameValue = it.nickname
                emailValue = it.email
                locationValue = it.location
                birthdateValue = it.birthdate.asDate()
                bioValue = it.biography
                it.profileImage?.let { profileImageUri = it.toUri() }
                lastUpdate = it.lastUpdate
                profileImageSource = it.profileImageSource
                favoritesProjects.clear()
                favoritesProjects.addAll(it.favorites.toMutableStateList())
            }
        }
    }

    private fun stopCollectingUser() {
        updatingUser?.cancel()
        updatingUser = null
    }

    private fun startCollectingFeedbacks(userId: String) {
        updatingFeedbacks = viewModelScope.launch {
            feedbacksViewModel!!.getUserFeedbacks(userId).collect {
                feedbacks.clear()
                feedbacks.addAll(it.values)
            }
        }
    }

    private fun startCollectingTasks(userId: String) {
        updatingTasks = viewModelScope.launch {
            tasksViewModel!!.getUserTasks().collect {
                tasks.clear()
                tasks.addAll(it.values)
            }
        }
    }


    var favoritesProjects = mutableStateListOf<String>()
        private set

    fun setFavorite(projectId: String) {
        if (isFavorite(projectId)) favoritesProjects.remove(projectId)
        else favoritesProjects.add(projectId)
        viewModelScope.launch {
            model.setUserFavorites(userId, favoritesProjects)
        }
    }

    fun isFavorite(projectId: String): Boolean {
        return favoritesProjects.contains(projectId)
    }


    var isEditing by mutableStateOf(false)
        private set

    fun edit() {
        isEditing = true
        stopCollectingUser()
    }

    var uploadStatus by mutableStateOf(Any())
        private set

    suspend fun validate(): Boolean {
        checkName()
        checkSurname()
        checkNickname()
        checkEmail()
        checkLocation()
        checkBirthdate()
        checkBio()
        if (nameError.isBlank() && surnameError.isBlank() && emailError.isBlank() && nicknameError.isBlank() && emailError.isBlank() && locationError.isBlank() && bioError.isBlank() && birthdateError.isBlank()) {

            return viewModelScope.async {
                if (usersViewModel.updateUser(
                        userId,
                        User(
                            name = nameValue,
                            surname = surnameValue,
                            nickname = nicknameValue,
                            email = emailValue,
                            location = locationValue,
                            birthdate = birthdateValue.toTimestamp(),
                            biography = bioValue,
                            profileImage = profileImageUri.toString(),
                            profileImageSource = profileImageSource,
                            lastUpdate = Timestamp.now(),
                            feedbacks = feedbacks.map { it.feedbackId }.toList()
                        )
                    )
                ) {
                    isEditing = false
                    uploadStatus = Any()
                    startCollectingUser(userId)
                    true
                } else {
                    uploadStatus = UploadStatus.Error("An error occurred. Please try again.")
                    isEditing = true
                    stopCollectingUser()
                    false
                }
            }.await()
        } else {
            uploadStatus = Any()
            isEditing = true
            stopCollectingUser()
            return false
        }
    }

    fun signOut() {
        _state.update {
            it.copy(
                isSignInSuccessful = null,
                signInError = null
            )
        }
        val auth = Firebase.auth
        auth.signOut()
    }

    fun deleteAccount() {
        val auth = Firebase.auth
        viewModelScope.launch {
            if(model.deleteUser(userId)) {
                auth.currentUser!!.delete().await()
                signOut()
            }
            else {
                val error = "An error occurred. Please try again."
                Log.d("logout", error)
            }
        }
    }

    var profileImageUri: Uri by mutableStateOf(Uri.EMPTY)
        private set

    var profileImageSource: ImageSource by mutableStateOf(ImageSource.MONOGRAM)
        private set



    fun setProfileImage(source: ImageSource, uri: Uri = Uri.EMPTY, context: Context? = null) {
        profileImageSource = source
        when (source) {
            ImageSource.CAMERA, ImageSource.LIBRARY -> {
                viewModelScope.launch {
                    context?.let {
                        var bitmap: Bitmap? = null
                        val inputStream = it.contentResolver.openInputStream(uri)
                        bitmap = BitmapFactory.decodeStream(inputStream)

                            val file = saveBitmapAsJpeg(context, bitmap, "$userId.jpg")
                            file?.let { jpegFile ->

                                usersViewModel!!.uploadProfileImage(userId = userId, file = jpegFile).collect {
                                       uploadStatus = it
                                        if(uploadStatus is UploadStatus.Success) {
                                            profileImageUri = (it as UploadStatus.Success).downloadUrl.toUri()
                                            profileImageSource = ImageSource.REMOTE
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

    var nameValue by mutableStateOf("")
        private set
    var nameError by mutableStateOf("")
        private set

    fun setName(n: String) {
        nameValue = n
    }

    private fun checkName() {
        nameError = if (nameValue.isBlank()) {
            "Name cannot be blank"
        } else ""
    }

    var surnameValue by mutableStateOf("")
        private set
    var surnameError by mutableStateOf("")
        private set

    fun setSurname(s: String) {
        surnameValue = s
    }

    private fun checkSurname() {
        surnameError = if (surnameValue.isBlank()) {
            "Surname cannot be blank"
        } else ""
    }

    var nicknameValue by mutableStateOf("")
        private set
    var nicknameError by mutableStateOf("")
        private set

    fun setNickname(n: String) {
        nicknameValue = n
    }

    private fun checkNickname() {
        nicknameError = if (nicknameValue.isBlank()) {
            "Nickname cannot be blank"
        } else ""
    }


    private fun checkEmail() {
        emailError = if (emailValue.isBlank()) {
            "Email cannot be blank"
        } else if (!emailValue.contains('@')) {
            "Invalid email address"
        } else ""
    }

    var locationValue by mutableStateOf("")
        private set
    var locationError by mutableStateOf("")
        private set



    fun setLocation(l: String) {
        locationValue = l
    }


    private fun checkLocation() {
        locationError = if (locationValue.isBlank()) {
            "Location cannot be blank"
        } else ""
    }

    var datePickerDialog by mutableStateOf(false)
        private set

    fun setDatePickerDialog() {
        datePickerDialog = !datePickerDialog
    }

    var birthdateValue by mutableStateOf("")
        private set
    var birthdateError by mutableStateOf("")
        private set

    fun setBirthdate(b: String) {
        birthdateValue = b
    }

    private fun checkBirthdate() {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        return try {
            if (birthdateValue.isBlank())
                throw DateTimeException("Birthdate can not be blank")
            val dateOfBirth = dateFormat.parse(birthdateValue)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -18)
            if (!dateOfBirth!!.before(calendar.time))
                throw DateTimeException("User must be at least 18 years old")
            birthdateError = ""
        } catch (e: Exception) {
            when (e) {
                is DateTimeException -> {
                    birthdateError = e.message.toString()
                }

                else -> {
                    birthdateError = "Invalid birthdate date"
                }
            }

        }
    }

    var bioValue by mutableStateOf("")
        private set
    var bioError by mutableStateOf("")
        private set

    fun setBio(d: String) {
        bioValue = d
    }

    private fun checkBio() {
        bioError = if (bioValue.isBlank()) {
            "Biography cannot be blank"
        } else ""
    }


    var lastUpdate by mutableStateOf(Timestamp.now())
        private set

    fun setUpdate(now: Timestamp) {
        lastUpdate = now
    }

}

